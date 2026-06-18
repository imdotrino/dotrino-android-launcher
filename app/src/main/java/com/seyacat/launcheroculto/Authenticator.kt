package com.seyacat.launcheroculto

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Autenticación con la seguridad del dispositivo: huella / rostro (BIOMETRIC_WEAK)
 * o el patrón / PIN del teléfono (DEVICE_CREDENTIAL). Si el equipo no tiene ningún
 * método configurado, la app cae al PIN propio.
 */
object Authenticator {

    private const val AUTHENTICATORS = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    fun canAuthenticate(ctx: Context): Boolean =
        BiometricManager.from(ctx).canAuthenticate(AUTHENTICATORS) ==
            BiometricManager.BIOMETRIC_SUCCESS

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFallback: () -> Unit = {},
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // Si la biometría falla/no está disponible en runtime, ofrecer PIN.
                    if (errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT ||
                        errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE ||
                        errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS ||
                        errorCode == BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL
                    ) {
                        onFallback()
                    }
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Apps ocultas")
            .setSubtitle("Verificá tu identidad")
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()
        prompt.authenticate(info)
    }
}
