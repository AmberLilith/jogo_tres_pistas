package com.br.amber.jogodastrespistas.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.br.amber.jogodastrespistas.data.RoomRepository
import com.br.amber.jogodastrespistas.models.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = RoomRepository()
    private val _waitingRooms = MutableStateFlow<List<Room>>(emptyList())
    val waitingRooms: StateFlow<List<Room>> = _waitingRooms

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _roomId = MutableStateFlow<String?>(null)
    val roomId: StateFlow<String?> = _roomId

    fun createRoom() {
        _isLoading.value = true
        _errorMessage.value = null

        repository.createRoom(
            onSuccess = { id ->
                _roomId.value = id
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
}
