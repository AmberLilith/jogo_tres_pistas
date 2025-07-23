package com.br.amber.jogodastrespistas.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.br.amber.jogodastrespistas.data.AuthRepository

class AuthViewModel: ViewModel(){
    private val authRepository = AuthRepository()

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isLoggedIn by mutableStateOf(false)

    fun loginWithGoogle(idToken: String){
        isLoading = true
        errorMessage = null
        authRepository.firebaseAuthWithGoogle(idToken) { result ->
            isLoading = false
            result.onSuccess {
                isLoggedIn = true
            }.onFailure { error ->
                errorMessage = error.message
            }
        }
    }

    fun isAlreadyLoggedIn(): Boolean = authRepository.isUsserLoggedIn()
}