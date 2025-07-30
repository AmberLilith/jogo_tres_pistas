package com.br.amber.jogodastrespistas.ui.screens.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.br.amber.jogodastrespistas.data.RoomRepository
import com.br.amber.jogodastrespistas.enums.ScoreEnum
import com.br.amber.jogodastrespistas.models.Room
import com.br.amber.jogodastrespistas.models.RoomStatusesEnum
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

    /*
    indexScore será o mesmo valor de room.shownClues
    */
    fun addPoints(isOwner: Boolean, indexScore: Int) {
        val scores = enumValues<ScoreEnum>().toList()
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.updatePoints(roomId, isOwner, scores[indexScore].points)
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

    fun verifyAswer(
        answer: String,
        room: Room
    ){
        val wordToVerify: String = room.drawnWords[room.round].name
        val indexScore: Int = room.cluesShown
        val isOwner: Boolean = room.owner.id == loggedUserId
        val isOwnerTurn: Boolean = room.ownerTurn
        val nextRound: Int = room.round + 1

        if(isAnswerCorrect(wordToVerify, answer)){
            addPoints(isOwner, indexScore)
            if(nextRound == Room.NUMBER_OF_ROUNDS){
                updateStatus(RoomStatusesEnum.FINISHED.status, onSuccess = {})
            }else{
                startNewRound(isOwnerTurn, nextRound)
            }

        }else{
                if(room.cluesShown < 2){
                    changeTurn(
                        isOwnerTurn,
                        onSuccess = {
                            updateCluesShown(room.cluesShown + 1, onSuccess = {})
                        }
                    )

                }else{
                    if(nextRound == Room.NUMBER_OF_ROUNDS){
                        updateStatus(RoomStatusesEnum.FINISHED.status, onSuccess = {})
                    }else{
                        startNewRound(isOwnerTurn, nextRound)
                    }
                }

        }
    }

    fun startNewRound(isOwnerTurn: Boolean, nextRound: Int){
        changeTurn(isOwnerTurn){
            updateCluesShown(0){
                advanceRound(nextRound) {
                    updateWordUsed(nextRound, {})
                }
            }
        }
    }
}