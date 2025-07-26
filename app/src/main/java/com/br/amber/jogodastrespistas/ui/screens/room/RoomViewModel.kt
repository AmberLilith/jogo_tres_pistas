package com.br.amber.jogodastrespistas.ui.screens.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.br.amber.jogodastrespistas.data.RoomRepository
import com.br.amber.jogodastrespistas.models.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RoomViewModel(private val repository: RoomRepository) : ViewModel() {
    private val _roomState = MutableStateFlow<Room?>(null)
    val roomState: StateFlow<Room?> = _roomState

    private var currentRoomId: String? = null

    fun init(roomId: String) {
        if (currentRoomId == roomId) return

        currentRoomId = roomId
        viewModelScope.launch {
            repository.getRoomUpdates(roomId).collectLatest { room ->
                _roomState.value = room.copy(id = roomId)
            }
        }
    }

    fun addPoints(isOwner: Boolean, points: Int) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.updatePoints(roomId, isOwner, points)
            }
        }
    }

    fun changeTurn(isOwnerTurn: Boolean) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.updateTurn(roomId, isOwnerTurn)
            }
        }
    }

    fun updateStatus(status: String) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.updateStatus(roomId, status)
            }
        }
    }

    fun updateCluesShown(count: Int) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.updateCluesShown(roomId, count)
            }
        }
    }

    fun advanceRound() {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                _roomState.value?.let { currentRoom ->
                    repository.updateRound(roomId, currentRoom.round + 1)
                }
            }
        }
    }

    fun setPlayerOnlineStatus(isOwner: Boolean, isOnline: Boolean) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.updatePlayerOnlineStatus(roomId, isOwner, isOnline)
            }
        }
    }
}