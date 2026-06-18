package com.seyacat.launcheroculto

import androidx.compose.ui.graphics.ImageBitmap

/** Una app lanzable del dispositivo, con su ícono ya listo para Compose. */
data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: ImageBitmap,
)
