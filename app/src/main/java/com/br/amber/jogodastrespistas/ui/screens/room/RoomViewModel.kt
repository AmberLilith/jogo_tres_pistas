package com.br.amber.jogodastrespistas.ui.screens.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.br.amber.jogodastrespistas.data.RoomRepository
import com.br.amber.jogodastrespistas.enums.PointsEnum
import com.br.amber.jogodastrespistas.models.Room
import com.br.amber.jogodastrespistas.enums.RoomStatusesEnum
import com.br.amber.jogodastrespistas.normalize
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RoomViewModel(private val repository: RoomRepository) : ViewModel() {
    private val _roomState = MutableStateFlow<Room?>(null)
    val roomState: StateFlow<Room?> = _roomState
    private val auth = FirebaseAuth.getInstance()
    val loggedUserId = auth.currentUser?.uid

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

    fun setScore(isOwner: Boolean, indexPoints: Int, currentPoints: Int, onSuccess: () -> Unit) {
        val points = enumValues<PointsEnum>().toList()
        val newScore = currentPoints + points[indexPoints].points
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.setScore(roomId, isOwner, newScore){
                    onSuccess()
                }
            }
        }
    }

    fun changeTurn(isOwnerTurn: Boolean, onSuccess: () -> Unit) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.updateTurn(roomId, isOwnerTurn, onSuccess)
            }
        }
    }

    fun updateStatus(status: String, onSuccess: () -> Unit) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.updateStatus(roomId, status, onSuccess)
            }
        }
    }

    fun updateChosenWordIndex(wordIndex: Int, onSuccess: () -> Unit){
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.updateChosenWordIndex(roomId, wordIndex, onSuccess)
            }
        }
    }

    fun updateCluesShown(count: Int, onSuccess: () -> Unit) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.updateCluesShown(roomId, count, onSuccess)
            }
        }
    }

    fun updateWordUsed(wordIndex: Int, onSuccess: () -> Unit){
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.updateWordUsed(roomId, wordIndex, onSuccess)
            }
        }
    }

    fun advanceRound(nextRound: Int, onSuccess: () -> Unit) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                _roomState.value?.let { currentRoom ->
                    repository.updateRound(roomId, nextRound, onSuccess)
                }
            }
        }
    }

    fun setPlayerOnlineStatus(isOwner: Boolean, isOnline: Boolean, onSuccess: () -> Unit) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.updatePlayerOnlineStatus(roomId, isOwner, isOnline, onSuccess)
            }
        }
    }

    fun isAnswerCorrect(
        wordToVerify: String,
        answer: String
    ): Boolean{
        val normalizedWordToVerify = wordToVerify.normalize()
        val normalizedAnswer = answer.normalize()
        println("normalizedWordToVerify = $normalizedWordToVerify e normalizedAnswer = $normalizedAnswer")
        return normalizedWordToVerify == normalizedAnswer

    }

    fun verifyAnswer(
        answer: String,
        room: Room
    ){
        val normalizedWordToVerify: String = room.drawnWords[room.chosenWordIndex].name.normalize()
        val indexPoints: Int = room.cluesShown
        val isOwner: Boolean = room.owner.id == loggedUserId
        val isOwnerTurn: Boolean = room.ownerTurn
        val nextRound: Int = room.round + 1
        if(isAnswerCorrect(normalizedWordToVerify, answer.normalize())){
            val currentPoints = if(isOwner) room.owner.score else room.guest.score
            setScore(isOwner, indexPoints, currentPoints){}
        }
    }

    fun getNextStatus(room: Room): String{
        return when{
            room.round == Room.NUMBER_OF_ROUNDS && room.cluesShown == 2 -> RoomStatusesEnum.FINISHED.name
        else -> RoomStatusesEnum.GOT_WRONG_ANSWER.name
        }
    }

    fun startNewRound(isOwnerTurn: Boolean, nextRound: Int){
        changeTurn(isOwnerTurn){
            updateChosenWordIndex(-1){
                updateCluesShown(-1){
                    advanceRound(nextRound) {}
                }
            }
        }
    }

    fun deleteRoom(onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.deleteRoom(roomId, onSuccess, onFailure)
            }
        }
    }
}