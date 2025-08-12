package com.br.amber.jogodastrespistas.ui.screens.home

import androidx.lifecycle.ViewModel
import com.br.amber.jogodastrespistas.data.HomeRepository
import com.br.amber.jogodastrespistas.models.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val repository = HomeRepository()
    private val _waitingRooms = MutableStateFlow<List<Room>>(emptyList())
    val waitingRooms: StateFlow<List<Room>> = _waitingRooms

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _createdRoomId = MutableStateFlow<String?>(null)
    val createdRoomId: StateFlow<String?> = _createdRoomId

    fun createRoom() {
        _isLoading.value = true
        _errorMessage.value = null

        repository.createRoom(
            onSuccess = { id ->
                _createdRoomId.value = id
                _isLoading.value = false
            },
            onError = { exception ->
                _errorMessage.value = exception.message
                _isLoading.value = false
            }
        )
    }

    fun observeWaitingRooms() {
        repository.listenWaitingRooms(
            onUpdate = { rooms -> _waitingRooms.value = rooms },
            onError = { error -> _errorMessage.value = error.message }
        )
    }

    fun clearRoomId() {
        _createdRoomId.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun joinRoomAsGuest(
        roomId: String,
        callback: Unit
    ) {
        repository.joinRoomAsGuest(
            roomId = roomId,
            onSuccess = {
                callback
            },
            onError = { error ->
                // Mostrar mensagem de erro
                println("Erro ao entrar na sala: ${error.message}")
            }
        )
    }




}
