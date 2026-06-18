package com.seyacat.launcheroculto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// El launcher dibuja un scrim oscuro sobre el wallpaper y texto blanco, así que
// la UI (campos, diálogos) usa siempre esquema oscuro para ser legible,
// independientemente del modo claro/oscuro del sistema.
private val DarkColors = darkColorScheme()

@Composable
fun LauncherOcultoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
