package com.seyacat.launcheroculto

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class LauncherViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("launcher_oculto", Context.MODE_PRIVATE)

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _hidden = MutableStateFlow(prefs.getStringSet(KEY_HIDDEN, emptySet()).orEmpty().toSet())
    val hidden: StateFlow<Set<String>> = _hidden.asStateFlow()

    private val _recents = MutableStateFlow(loadRecents())
    val recents: StateFlow<List<String>> = _recents.asStateFlow()

    /** Dock fijo: Teléfono y Cámara (apps por defecto del sistema). */
    private val _dock = MutableStateFlow<List<AppInfo>>(emptyList())
    val dock: StateFlow<List<AppInfo>> = _dock.asStateFlow()

    /** Pseudo-app que abre la configuración (logo Dotrino + candado). */
    val configApp: AppInfo = pseudoApp("Configuración", CONFIG_PKG, R.drawable.ic_config_app)

    private fun pseudoApp(label: String, pkg: String, resId: Int): AppInfo {
        val d = ContextCompat.getDrawable(getApplication(), resId)!!
        return AppInfo(label, pkg, d.toBitmap(144, 144).asImageBitmap())
    }

    // ---- Pseudo-app de apps ocultas: nombre e icono camuflables ----

    /** Vista previa de los iconos disponibles para elegir (en orden de DISGUISE_ICONS). */
    val iconOptions: List<androidx.compose.ui.graphics.ImageBitmap> =
        DISGUISE_ICONS.map { resId ->
            ContextCompat.getDrawable(getApplication(), resId)!!.toBitmap(144, 144).asImageBitmap()
        }

    private val _hiddenName = MutableStateFlow(prefs.getString(KEY_PSEUDO_NAME, DEFAULT_PSEUDO_NAME)!!)
    val hiddenName: StateFlow<String> = _hiddenName.asStateFlow()

    private val _hiddenIconIndex = MutableStateFlow(
        prefs.getInt(KEY_PSEUDO_ICON, 0).coerceIn(0, DISGUISE_ICONS.lastIndex)
    )
    val hiddenIconIndex: StateFlow<Int> = _hiddenIconIndex.asStateFlow()

    /** Pseudo-app que abre la pantalla de apps ocultas (camuflada). */
    private val _hiddenApp = MutableStateFlow(buildHiddenApp())
    val hiddenApp: StateFlow<AppInfo> = _hiddenApp.asStateFlow()

    private fun buildHiddenApp(): AppInfo =
        pseudoApp(_hiddenName.value.ifBlank { DEFAULT_PSEUDO_NAME }, HIDDEN_PKG, DISGUISE_ICONS[_hiddenIconIndex.value])

    fun setHiddenName(name: String) {
        _hiddenName.value = name
        prefs.edit().putString(KEY_PSEUDO_NAME, name).apply()
        _hiddenApp.value = buildHiddenApp()
    }

    fun setHiddenIcon(index: Int) {
        val i = index.coerceIn(0, DISGUISE_ICONS.lastIndex)
        _hiddenIconIndex.value = i
        prefs.edit().putInt(KEY_PSEUDO_ICON, i).apply()
        _hiddenApp.value = buildHiddenApp()
    }

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** false = cajón normal; true = menú de administración (elegir qué ocultar). */
    private val _manage = MutableStateFlow(false)
    val manage: StateFlow<Boolean> = _manage.asStateFlow()

    /** true = pantalla con las apps ocultas (para abrirlas). */
    private val _hiddenView = MutableStateFlow(false)
    val hiddenView: StateFlow<Boolean> = _hiddenView.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                val ctx = getApplication<Application>()
                val pm = ctx.packageManager
                val myPkg = ctx.packageName
                val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
                pm.queryIntentActivities(intent, 0)
                    .asSequence()
                    .map { it.activityInfo.packageName }
                    .distinct()
                    .filter { it != myPkg }
                    .mapNotNull { pkg ->
                        runCatching {
                            val ai = pm.getApplicationInfo(pkg, 0)
                            val label = pm.getApplicationLabel(ai).toString()
                            val icon = pm.getApplicationIcon(ai).toBitmap(144, 144).asImageBitmap()
                            AppInfo(label, pkg, icon)
                        }.getOrNull()
                    }
                    .sortedBy { it.label.lowercase() }
                    .toList()
            }
            _apps.value = loaded
            _dock.value = resolveDock(loaded)
        }
    }

    /** Resuelve las apps por defecto de Teléfono y Cámara y las mapea a la lista cargada. */
    private fun resolveDock(loaded: List<AppInfo>): List<AppInfo> {
        val pm = getApplication<Application>().packageManager
        val byPkg = loaded.associateBy { it.packageName }
        val phone = resolvePackage(pm, Intent(Intent.ACTION_DIAL))
        val camera = resolvePackage(pm, Intent("android.media.action.STILL_IMAGE_CAMERA"))
        return listOfNotNull(phone, camera).distinct().mapNotNull { byPkg[it] }
    }

    private fun resolvePackage(pm: PackageManager, intent: Intent): String? =
        pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
            ?.takeIf { it != "android" }

    fun setQuery(q: String) {
        _query.value = q
    }

    fun launch(pkg: String) {
        val ctx = getApplication<Application>()
        ctx.packageManager.getLaunchIntentForPackage(pkg)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            recordRecent(pkg)
            ctx.startActivity(it)
        }
    }

    /** Abre la pantalla de "Información de la app" del sistema. */
    fun openAppInfo(pkg: String) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$pkg")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        start(intent, "abrir info de $pkg")
    }

    /** Lanza el diálogo del sistema para desinstalar la app. */
    fun uninstall(pkg: String) {
        @Suppress("DEPRECATION")
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, Uri.parse("package:$pkg"))
            .putExtra(Intent.EXTRA_RETURN_RESULT, false)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        start(intent, "desinstalar $pkg")
    }

    private fun start(intent: Intent, what: String) {
        val ctx = getApplication<Application>()
        try {
            ctx.startActivity(intent)
        } catch (e: Exception) {
            Log.e("LauncherOculto", "No se pudo $what", e)
            Toast.makeText(ctx, "No se pudo $what: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun recordRecent(pkg: String) {
        val updated = (listOf(pkg) + _recents.value.filter { it != pkg }).take(MAX_RECENTS)
        _recents.value = updated
        prefs.edit().putString(KEY_RECENTS, updated.joinToString("\n")).apply()
    }

    private fun loadRecents(): List<String> =
        prefs.getString(KEY_RECENTS, "").orEmpty()
            .split("\n")
            .filter { it.isNotBlank() }

    /** Marca/desmarca una app como oculta (usado desde el menú de administración). */
    fun toggleHidden(pkg: String) {
        val cur = _hidden.value.toMutableSet()
        if (!cur.add(pkg)) cur.remove(pkg)
        _hidden.value = cur
        prefs.edit().putStringSet(KEY_HIDDEN, cur).apply()
    }

    fun resetToHome() {
        _manage.value = false
        _hiddenView.value = false
        _query.value = ""
    }

    // ---- Pantallas protegidas por PIN ----

    fun openManage() {
        _query.value = ""
        _hiddenView.value = false
        _manage.value = true
    }

    fun closeManage() {
        _query.value = ""
        _manage.value = false
    }

    fun openHiddenView() {
        _manage.value = false
        _hiddenView.value = true
    }

    fun closeHiddenView() {
        _hiddenView.value = false
    }

    // ---- PIN ----

    fun hasPin(): Boolean = prefs.getString(KEY_PIN, null) != null

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN, hash(pin)).apply()
    }

    fun checkPin(pin: String): Boolean = hash(pin) == prefs.getString(KEY_PIN, null)

    private fun hash(s: String): String =
        MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
            .joinToString("") { "%02x".format(it) }

    companion object {
        /** Paquetes ficticios que identifican a las pseudo-apps. */
        const val CONFIG_PKG = "__dotrino_config__"
        const val HIDDEN_PKG = "__dotrino_hidden__"

        /** Iconos disponibles para camuflar la pseudo-app de apps ocultas. */
        val DISGUISE_ICONS = listOf(
            R.drawable.ic_d_football,
            R.drawable.ic_d_music,
            R.drawable.ic_d_weather,
            R.drawable.ic_d_notes,
            R.drawable.ic_d_game,
            R.drawable.ic_d_fitness,
        )
        private const val DEFAULT_PSEUDO_NAME = "Fútbol"

        private const val KEY_HIDDEN = "hidden_packages"
        private const val KEY_PIN = "pin_hash"
        private const val KEY_RECENTS = "recent_packages"
        private const val KEY_PSEUDO_NAME = "pseudo_name"
        private const val KEY_PSEUDO_ICON = "pseudo_icon"
        private const val MAX_RECENTS = 8
    }
}
