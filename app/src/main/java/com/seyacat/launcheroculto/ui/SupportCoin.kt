package com.seyacat.launcheroculto.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Moneda de soporte (versión nativa Compose del Web Component compartido
 * <dotrino-support>). Se reimplementa nativa porque un custom element solo
 * puede renderizarse en un WebView y, embebido en una pantalla nativa, la moneda
 * y el popup quedan recortados por los límites del WebView. Misma funcionalidad:
 * Ko-fi (donar), Discord y compartir. Si surgen más apps nativas en el ecosistema,
 * esto debería extraerse a un paquete compartido (p. ej. dotrino-support-android).
 */
private const val KOFI_URL = "https://ko-fi.com/dotrino"
private const val DISCORD_URL = "https://discord.gg/D648uq7cth"
private const val APP_URL = "https://github.com/imdotrino/android-launcher"

@Composable
fun SupportCoin(lang: String, modifier: Modifier = Modifier) {
    val es = lang.startsWith("es")
    var open by remember { mutableStateOf(false) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Moneda: círculo con corazón. Abre el diálogo de soporte.
        Surface(
            color = Color(0xFF3498DB),
            shape = CircleShape,
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .clickable { open = true }
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = if (es) "Apoya el proyecto" else "Support the project",
                tint = Color.White,
                modifier = Modifier.padding(15.dp)
            )
        }
    }

    if (open) {
        SupportDialog(es = es, onDismiss = { open = false })
    }
}

@Composable
private fun SupportDialog(es: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    fun openUrl(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        onDismiss()
    }
    fun share() {
        val text = if (es)
            "Dotrino Launcher: launcher de Android con apps ocultas. $APP_URL"
        else
            "Dotrino Launcher: an Android launcher with hidden apps. $APP_URL"
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        runCatching {
            context.startActivity(
                Intent.createChooser(send, if (es) "Compartir" else "Share")
            )
        }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (es) "Apoya el proyecto" else "Support the project") },
        text = {
            Column {
                Text(
                    if (es)
                        "Dotrino es autohosteado, sin anuncios ni rastreadores. Si te sirve, invitame un café, sumate a la comunidad o difundilo."
                    else
                        "Dotrino is self-hosted, with no ads or trackers. If it helps you, buy me a coffee, join the community or share it.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    OutlinedButton(onClick = { openUrl(KOFI_URL) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (es) "Donar en Ko-fi" else "Donate on Ko-fi")
                    }
                    OutlinedButton(onClick = { openUrl(DISCORD_URL) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Discord")
                    }
                    OutlinedButton(onClick = { share() }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (es) "Compartir" else "Share")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (es) "Cerrar" else "Close")
            }
        }
    )
}
