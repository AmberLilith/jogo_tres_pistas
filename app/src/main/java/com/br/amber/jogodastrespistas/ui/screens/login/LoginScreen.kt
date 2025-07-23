package com.br.amber.jogodastrespistas.ui.screens.login

import android.app.Activity
import android.content.IntentSender
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.br.amber.jogodastrespistas.R

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    val oneTapClient = remember { Identity.getSignInClient(context) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                // Método está deprecated, mas ainda é o jeito suportado no momento.
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    viewModel.loginWithGoogle(idToken)
                } else {
                    viewModel.errorMessage = "Token de autenticação não encontrado"
                }
            } catch (e: ApiException) {
                viewModel.errorMessage = "Erro na autenticação: ${e.message}"
            }
        } else {
            viewModel.errorMessage = "Login cancelado"
        }
    }

    LaunchedEffect(viewModel.isLoggedIn) {
        if (viewModel.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                val signInRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(context.getString(R.string.default_web_client_id))
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .build()

                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener { result ->
                        try {
                            launcher.launch(
                                IntentSenderRequest.Builder(result.pendingIntent.intentSender)
                                    .build()
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            viewModel.errorMessage = "Erro ao iniciar login: ${e.message}"
                        }
                    }
                    .addOnFailureListener { e ->
                        viewModel.errorMessage = "Falha ao iniciar login: ${e.message}"
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar com Google")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isLoading) {
            CircularProgressIndicator()
        }

        viewModel.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(error, color = Color.Red)
        }
    }
}
