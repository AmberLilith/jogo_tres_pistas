package com.br.amber.jogodastrespistas.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.br.amber.jogodastrespistas.data.AuthRepository

/*
Estender ViewModel() permite que a classe armazene e gerencie dados da interface de forma persistente,
sobrevivendo a recomposições e mudanças de configuração (como rotação de tela). Isso evita perda de estado
 e separa a lógica da UI.
 Se a Lógica for colocada diretamente dentro de uma tela (@Composable), ela pode ser recriada toda vez que a
 tela recompuser — o que seria um problema.
 Com um ViewModel, a instância persiste e mantém o controle do estado.
*/
class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
/*
O by é um delegado (do Kotlin): ele delega o getter e setter da variável isLoading para o mutableStateOf(...).
Isso permite que Compose observe automaticamente quando o valor muda e recomponha a UI se necessário.
mutableStateOf(...) cria um estado observável pelo Compose.
Quando o valor muda (ex: isLoading = true), o Compose detecta a mudança e atualiza a UI que estiver usando isLoading.
by e mutableStates juntos, permitem reatividade automática da UI.
Os principais são: mutableStateOf, mutableStateListOf e mutableStateMapOf
*/
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var isLoggedIn by mutableStateOf(authRepository.isUsserLoggedIn())
        private set

    fun loginWithGoogle(idToken: String) {
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

    fun isUserLoggedIn(): Boolean = authRepository.isUsserLoggedIn()
}
