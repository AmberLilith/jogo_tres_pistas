package com.br.amber.jogodastrespistas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.br.amber.jogodastrespistas.navigation.AppNavHost
import com.br.amber.jogodastrespistas.ui.theme.JogoDasTrêsPistasTheme
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.br.amber.jogodastrespistas.ui.screens.login.AuthViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel by viewModels<AuthViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JogoDasTrêsPistasTheme {
                AppNavHost(authViewModel = authViewModel)
            }
        }
    }
}