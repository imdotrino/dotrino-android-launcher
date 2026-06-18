package com.seyacat.launcheroculto

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seyacat.launcheroculto.ui.LauncherScreen
import com.seyacat.launcheroculto.ui.theme.LauncherOcultoTheme

class MainActivity : FragmentActivity() {

    private var resetToHome: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LauncherOcultoTheme {
                val vm: LauncherViewModel = viewModel()
                resetToHome = { vm.resetToHome() }
                LauncherScreen(vm)
            }
        }
    }

    /** Tocar HOME estando ya en el launcher: volver al cajón normal. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        resetToHome?.invoke()
    }
}
