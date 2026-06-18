package com.seyacat.launcheroculto

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

/** Helpers para saber si somos el launcher por defecto y para solicitarlo. */
object DefaultLauncher {

    fun isDefault(ctx: Context): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val res = ctx.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return res?.activityInfo?.packageName == ctx.packageName
    }

    /** Pide al sistema que nos ponga como pantalla de inicio. */
    fun request(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = activity.getSystemService(RoleManager::class.java)
            if (rm != null &&
                rm.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !rm.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                runCatching {
                    activity.startActivityForResult(
                        rm.createRequestRoleIntent(RoleManager.ROLE_HOME), REQUEST_HOME
                    )
                }.onSuccess { return }
            }
        }
        // Fallback (API < 29 o si el rol no está disponible, p. ej. MIUI):
        // abrir los ajustes de "app de inicio".
        runCatching { activity.startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }
    }

    private const val REQUEST_HOME = 1001
}
