package com.br.amber.jogodastrespistas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.br.amber.jogodastrespistas.ui.screens.home.HomeScreen
import com.br.amber.jogodastrespistas.ui.screens.login.LoginScreen
import com.br.amber.jogodastrespistas.ui.theme.JogoDasTrêsPistasTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JogoDasTrêsPistasTheme {
                var isLoggedIn by remember { mutableStateOf(false) }

                if (isLoggedIn) {
                    // Aqui você renderizaria a tela principal do app após o login
                    HomeScreen()
                } else {
                    LoginScreen(
                        onLoginSuccess = {
                            isLoggedIn = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val database = Firebase.database
    val myRef = database.getReference("teste")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Hello $name!")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            myRef.setValue("valor de teste")
        }) {
            Text("Salvar no Firebase")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JogoDasTrêsPistasTheme {
        Greeting("Android")
    }
}