package com.br.amber.jogodastrespistas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.br.amber.jogodastrespistas.navigation.AppNavHost
import com.br.amber.jogodastrespistas.ui.screens.login.AuthViewModel
import com.br.amber.jogodastrespistas.ui.theme.JogoDasTrêsPistasTheme

class MainActivity : ComponentActivity() {
    private val authViewModel by viewModels<AuthViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Remove a barra de status (IMERSIVO)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars()) // ← Esconde barra de status
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            JogoDasTrêsPistasTheme {
                AppNavHost(authViewModel = authViewModel)
            }
        }
    }
}