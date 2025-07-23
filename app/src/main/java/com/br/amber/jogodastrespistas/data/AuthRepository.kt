package com.br.amber.jogodastrespistas.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    fun firebaseAuthWithGoogle(idToken: String, onResult: (Result<Boolean>) -> Unit){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    onResult(Result.success(true))
                } else {
                    onResult(Result.failure(it.exception ?: Exception("Erro desconhecido")))
                }
            }
    }

    fun isUsserLoggedIn(): Boolean = auth.currentUser != null
}