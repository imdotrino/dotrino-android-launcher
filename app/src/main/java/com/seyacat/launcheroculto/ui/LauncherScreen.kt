package com.seyacat.launcheroculto.ui

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.seyacat.launcheroculto.DefaultLauncher
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seyacat.launcheroculto.AppInfo
import com.seyacat.launcheroculto.Authenticator
import com.seyacat.launcheroculto.LauncherViewModel
import kotlinx.coroutines.launch

@Composable
fun LauncherScreen(vm: LauncherViewModel) {
    val apps by vm.apps.collectAsStateWithLifecycle()
    val hidden by vm.hidden.collectAsStateWithLifecycle()
    val recents by vm.recents.collectAsStateWithLifecycle()
    val dock by vm.dock.collectAsStateWithLifecycle()
    val manage by vm.manage.collectAsStateWithLifecycle()
    val hiddenView by vm.hiddenView.collectAsStateWithLifecycle()
    val hiddenApp by vm.hiddenApp.collectAsStateWithLifecycle()
    val hiddenName by vm.hiddenName.collectAsStateWithLifecycle()
    val hiddenIconIndex by vm.hiddenIconIndex.collectAsStateWithLifecycle()

    // Pantalla a abrir tras validar el PIN (null = diálogo cerrado).
    var pinTarget by remember { mutableStateOf<PinTarget?>(null) }

    val menuActions = AppMenuActions(
        onToggleHide = vm::toggleHidden,
        onInfo = vm::openAppInfo,
        onUninstall = vm::uninstall,
    )

    // ¿Somos la pantalla de inicio por defecto? Se re-chequea al volver a la app.
    val context = LocalContext.current
    var isDefault by remember { mutableStateOf(DefaultLauncher.isDefault(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDefault = DefaultLauncher.isDefault(context)
                vm.loadApps() // refresca tras instalar/desinstalar apps
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val activity = context as? FragmentActivity

    fun open(target: PinTarget) = when (target) {
        PinTarget.MANAGE -> vm.openManage()
        PinTarget.HIDDEN -> vm.openHiddenView()
    }

    // Pide identidad: biometría/patrón del dispositivo, o PIN propio como respaldo.
    fun requestAccess(target: PinTarget) {
        if (activity != null && Authenticator.canAuthenticate(context)) {
            Authenticator.authenticate(
                activity = activity,
                onSuccess = { open(target) },
                onFallback = { pinTarget = target }
            )
        } else {
            pinTarget = target
        }
    }

    // Scrim semitransparente sobre el wallpaper para que el texto se lea.
    Surface(color = Color.Black.copy(alpha = 0.45f)) {
        when {
            manage -> ManageScreen(
                apps = apps,
                hidden = hidden,
                hiddenName = hiddenName,
                hiddenIconIndex = hiddenIconIndex,
                iconOptions = vm.iconOptions,
                onSetName = vm::setHiddenName,
                onSetIcon = vm::setHiddenIcon,
                onToggle = vm::toggleHidden,
                onBack = vm::closeManage
            )

            hiddenView -> HiddenAppsScreen(
                hiddenApps = apps.filter { it.packageName in hidden },
                onLaunch = vm::launch,
                menuActions = menuActions,
                onBack = vm::closeHiddenView
            )

            else -> DrawerScreen(
                apps = apps,
                hidden = hidden,
                recents = recents,
                dock = dock,
                configApp = vm.configApp,
                hiddenApp = hiddenApp,
                menuActions = menuActions,
                onLaunch = { pkg ->
                    when (pkg) {
                        LauncherViewModel.CONFIG_PKG -> requestAccess(PinTarget.MANAGE)
                        LauncherViewModel.HIDDEN_PKG -> requestAccess(PinTarget.HIDDEN)
                        else -> vm.launch(pkg)
                    }
                },
                showSetDefault = !isDefault,
                onSetDefault = { (context as? Activity)?.let { DefaultLauncher.request(it) } }
            )
        }
    }

    pinTarget?.let { target ->
        PinDialog(
            isSetup = !vm.hasPin(),
            onDismiss = { pinTarget = null },
            onConfirm = { pin ->
                if (vm.hasPin()) {
                    if (vm.checkPin(pin)) {
                        open(target); pinTarget = null; true
                    } else false
                } else {
                    vm.setPin(pin); open(target); pinTarget = null; true
                }
            }
        )
    }
}

private enum class PinTarget { MANAGE, HIDDEN }

/** Acciones del menú contextual (mantener presionada una app). */
private data class AppMenuActions(
    val onToggleHide: (String) -> Unit,
    val onInfo: (String) -> Unit,
    val onUninstall: (String) -> Unit,
)

/** Cajón normal: solo apps visibles. */
@Composable
private fun DrawerScreen(
    apps: List<AppInfo>,
    hidden: Set<String>,
    recents: List<String>,
    dock: List<AppInfo>,
    configApp: AppInfo,
    hiddenApp: AppInfo,
    menuActions: AppMenuActions,
    onLaunch: (String) -> Unit,
    showSetDefault: Boolean,
    onSetDefault: () -> Unit,
) {
    val visible = apps.filter { it.packageName !in hidden }
    // Las pseudo-apps se ordenan alfabéticamente como una más (Configuración -> C, etc.).
    val carousel = remember(visible, configApp, hiddenApp) {
        (listOf(configApp, hiddenApp) + visible).sortedBy { it.label.lowercase() }
    }
    val byPkg = apps.associateBy { it.packageName }
    // Recientes: las últimas abiertas, sin las ocultas, hasta llenar 2 filas.
    val recentApps = recents
        .filter { it !in hidden }
        .mapNotNull { byPkg[it] }
        .take(COLUMNS * RECENT_ROWS)
    val dockVisible = dock.filter { it.packageName !in hidden }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 12.dp)
    ) {
        if (showSetDefault) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .clickable { onSetDefault() }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    "Poner como pantalla de inicio",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Activar",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Dos filas fijas de recientes arriba.
        if (recentApps.isNotEmpty()) {
                Text(
                    "Recientes",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
                recentApps.chunked(COLUMNS).forEach { row ->
                    Row(Modifier.fillMaxWidth()) {
                        row.forEach { app ->
                            Box(Modifier.weight(1f)) {
                                AppCell(app = app, onLaunch = onLaunch, menuActions = menuActions)
                            }
                        }
                        // Rellena la fila si quedó incompleta para mantener alineación.
                        repeat(COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
            // Centro: páginas con swipe (carrusel). Pseudo-apps ordenadas como una más.
            PagedApps(
                apps = carousel,
                onLaunch = onLaunch,
                menuActions = menuActions,
                modifier = Modifier.weight(1f)
            )

        // Dock fijo abajo: Teléfono y Cámara.
        if (dockVisible.isNotEmpty()) {
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 4.dp)
            ) {
                dockVisible.forEach { app ->
                    AppCell(app = app, onLaunch = onLaunch, menuActions = menuActions)
                }
            }
        }
    }
}

private const val COLUMNS = 4
private const val ROWS = 5
private const val RECENT_ROWS = 2
private const val PAGE_SIZE = COLUMNS * ROWS

/** Grilla de apps repartida en páginas que se deslizan horizontalmente. */
@Composable
private fun PagedApps(
    apps: List<AppInfo>,
    onLaunch: (String) -> Unit,
    menuActions: AppMenuActions,
    modifier: Modifier = Modifier,
) {
    val pages = remember(apps) { paginateByLetter(apps, PAGE_SIZE) }
    if (pages.isEmpty()) return

    // Rango de letras de cada página (según las apps reales que contiene).
    val pageLabels = remember(pages) { pages.map { pageLabel(it) } }

    val realCount = pages.size
    val loop = realCount > 1
    // Para el bucle infinito usamos muchísimas páginas virtuales y mapeamos con módulo.
    val virtualCount = if (loop) Int.MAX_VALUE else 1
    val startPage = if (loop) (Int.MAX_VALUE / 2) - (Int.MAX_VALUE / 2 % realCount) else 0
    val pagerState = rememberPagerState(initialPage = startPage, pageCount = { virtualCount })
    val scope = rememberCoroutineScope()
    val current = if (loop) pagerState.currentPage % realCount else 0

    Column(modifier.fillMaxSize()) {
        Row(Modifier.weight(1f)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { virtualPage ->
                val pageIndex = if (loop) virtualPage % realCount else virtualPage
                LazyVerticalGrid(
                    columns = GridCells.Fixed(COLUMNS),
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(pages[pageIndex], key = { it.packageName }) { app ->
                        AppCell(app = app, onLaunch = onLaunch, menuActions = menuActions)
                    }
                }
            }

            // Índice lateral: una entrada por página, botones que llenan la altura
            // y letras apiladas (una debajo de otra) para ocupar menos ancho.
            if (realCount > 1) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(22.dp)
                        .padding(vertical = 4.dp)
                ) {
                    pageLabels.forEachIndexed { i, label ->
                        val selected = i == current
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .then(
                                    if (selected) Modifier.background(Color.White.copy(alpha = 0.18f))
                                    else Modifier
                                )
                                .clickable {
                                    scope.launch {
                                        val v = pagerState.currentPage
                                        val target = if (loop) v - (v % realCount) + i else i
                                        pagerState.animateScrollToPage(target)
                                    }
                                }
                        ) {
                            Text(
                                text = label.toList().joinToString("\n"),
                                color = if (selected) Color.White else Color.White.copy(alpha = 0.55f),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Puntitos indicadores de página.
        if (loop) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                repeat(realCount) { i ->
                    val selected = i == current
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 9.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) Color.White else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }
    }
}

/**
 * Pagina las apps agrupando por inicial: intenta que cada letra entre completa en
 * una sola página. Una letra solo se fracciona si por sí sola no cabe en una página.
 */
private fun paginateByLetter(apps: List<AppInfo>, pageSize: Int): List<List<AppInfo>> {
    if (apps.isEmpty()) return emptyList()
    // apps ya viene ordenado alfabéticamente -> groupBy conserva el orden de las iniciales.
    val groups = apps.groupBy { initialOf(it.label) }.values.toList()

    val pages = mutableListOf<MutableList<AppInfo>>()
    var cur = mutableListOf<AppInfo>()

    for (group in groups) {
        when {
            cur.size + group.size <= pageSize -> cur.addAll(group)
            group.size <= pageSize -> {
                if (cur.isNotEmpty()) pages.add(cur)
                cur = mutableListOf<AppInfo>().apply { addAll(group) }
            }
            else -> {
                // Única excepción: la letra no cabe ni en una página entera -> se fracciona.
                if (cur.isNotEmpty()) { pages.add(cur); cur = mutableListOf() }
                var i = 0
                while (group.size - i > pageSize) {
                    pages.add(group.subList(i, i + pageSize).toMutableList())
                    i += pageSize
                }
                cur.addAll(group.subList(i, group.size))
            }
        }
    }
    if (cur.isNotEmpty()) pages.add(cur)
    return pages
}

/** Etiqueta de rango de una página, p. ej. "A" o "AD" (inicial..final). */
private fun pageLabel(page: List<AppInfo>): String {
    val initials = page.map { initialOf(it.label) }
    if (initials.isEmpty()) return ""
    val first = initials.first()
    val last = initials.last()
    return if (first == last) first.toString() else "$first$last"
}

private fun initialOf(label: String): Char {
    val c = label.trimStart().firstOrNull()?.uppercaseChar() ?: '#'
    return if (c.isLetter()) c else '#'
}

/** Pantalla con las apps ocultas, lanzables (se entra con biometría/PIN). */
@Composable
private fun HiddenAppsScreen(
    hiddenApps: List<AppInfo>,
    onLaunch: (String) -> Unit,
    menuActions: AppMenuActions,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text(
                "Apps ocultas",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (hiddenApps.isEmpty()) {
            Text(
                "No hay apps ocultas. Entrá a Configuración para ocultar alguna.",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(COLUMNS),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(hiddenApps, key = { it.packageName }) { app ->
                    AppCell(app = app, onLaunch = onLaunch, menuActions = menuActions, isHidden = true)
                }
            }
        }
    }
}

/** Celda de una app: ícono + nombre. Toca para abrir; mantené presionado para el menú. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppCell(
    app: AppInfo,
    onLaunch: (String) -> Unit,
    menuActions: AppMenuActions? = null,
    isHidden: Boolean = false,
) {
    val isPseudo = app.packageName == LauncherViewModel.CONFIG_PKG ||
        app.packageName == LauncherViewModel.HIDDEN_PKG
    val menuEnabled = menuActions != null && !isPseudo
    var menu by remember { mutableStateOf(false) }

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .combinedClickable(
                    onClick = { onLaunch(app.packageName) },
                    onLongClick = { if (menuEnabled) menu = true }
                )
                .padding(8.dp)
        ) {
            Image(bitmap = app.icon, contentDescription = app.label, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                text = app.label,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        if (menuActions != null) {
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text(if (isHidden) "Mostrar" else "Ocultar") },
                    onClick = { menu = false; menuActions.onToggleHide(app.packageName) }
                )
                DropdownMenuItem(
                    text = { Text("Información de la app") },
                    onClick = { menu = false; menuActions.onInfo(app.packageName) }
                )
                DropdownMenuItem(
                    text = { Text("Desinstalar") },
                    onClick = { menu = false; menuActions.onUninstall(app.packageName) }
                )
            }
        }
    }
}

/** Menú de administración: camuflaje del atajo + lista con interruptores. */
@Composable
private fun ManageScreen(
    apps: List<AppInfo>,
    hidden: Set<String>,
    hiddenName: String,
    hiddenIconIndex: Int,
    iconOptions: List<ImageBitmap>,
    onSetName: (String) -> Unit,
    onSetIcon: (Int) -> Unit,
    onToggle: (String) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text(
                "Configuración",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Moneda de soporte arriba (fuera del LazyColumn) para que sea lo primero
        // visible y el WebView no quede en blanco al reciclarse en la lista.
        Text(
            "Apoyá el proyecto",
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        SupportCoin(
            lang = "es",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.padding(vertical = 10.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            item {
                Text(
                    "Atajo a apps ocultas (camuflaje)",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                Text(
                    "Icono",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 4.dp)
                ) {
                    iconOptions.forEachIndexed { i, bmp ->
                        val selected = i == hiddenIconIndex
                        Image(
                            bitmap = bmp,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) Color.White else Color.White.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSetIcon(i) }
                        )
                    }
                }
                OutlinedTextField(
                    value = hiddenName,
                    onValueChange = onSetName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    singleLine = true,
                    label = { Text("Nombre visible") }
                )
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                Text(
                    "Elegí qué apps ocultar",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(apps, key = { it.packageName }) { app ->
                val isHidden = app.packageName in hidden
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(app.packageName) }
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                ) {
                    Image(bitmap = app.icon, contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = app.label,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = isHidden, onCheckedChange = { onToggle(app.packageName) })
                }
            }
        }
    }
}

@Composable
private fun PinDialog(
    isSetup: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Boolean,
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isSetup) "Crear PIN" else "Ingresar PIN") },
        text = {
            Column {
                if (isSetup) {
                    Text(
                        "Definí un PIN (4 a 8 dígitos) para entrar al menú de apps ocultas.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 8 && it.all(Char::isDigit)) {
                            pin = it; error = false
                        }
                    },
                    singleLine = true,
                    isError = error,
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                if (error) {
                    Text(
                        "PIN incorrecto",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (pin.length < 4) { error = true; return@TextButton }
                if (!onConfirm(pin)) error = true
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
