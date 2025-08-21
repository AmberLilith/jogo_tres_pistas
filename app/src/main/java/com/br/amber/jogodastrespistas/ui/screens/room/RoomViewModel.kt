package com.br.amber.jogodastrespistas.ui.screens.room

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.br.amber.jogodastrespistas.data.RoomRepository
import com.br.amber.jogodastrespistas.enums.PointsEnum
import com.br.amber.jogodastrespistas.models.Room
import com.br.amber.jogodastrespistas.enums.RoomStatusesEnum
import com.br.amber.jogodastrespistas.models.Word
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

    fun setScore(isOwner: Boolean, indexPoints: Int, currentScore: Int, onSuccess: () -> Unit) {
        val points = enumValues<PointsEnum>().toList()
        val newScore = currentScore + points[indexPoints].points
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.setScore(roomId, isOwner, newScore){
                    onSuccess()
                }
            }
        }
    }

    fun setOwnerTurn(isOwnerTurn: Boolean, onSuccess: () -> Unit) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.setTurn(roomId, isOwnerTurn, onSuccess)
            }
        }
    }

    fun setStatus(status: String, onSuccess: () -> Unit) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.setStatus(roomId, status, onSuccess)
            }
        }
    }

    fun setChosenWordIndex(wordIndex: Int, onSuccess: () -> Unit){
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.setChosenWordIndex(roomId, wordIndex, onSuccess)
            }
        }
    }

    fun setCluesShown(nextCluesShown: Int, onSuccess: () -> Unit) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.setCluesShown(roomId, nextCluesShown, onSuccess)
            }
        }
    }

    fun setWordUsed(wordIndex: Int, onSuccess: () -> Unit){
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.setWordUsed(roomId, wordIndex, onSuccess)
            }
        }
    }

    fun setRound(nextRound: Int, onSuccess: () -> Unit) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                _roomState.value?.let { currentRoom ->
                    repository.setRound(roomId, nextRound, onSuccess)
                }
            }
        }
    }

    fun setDrawnWords(words: List<Word>, onSuccess: () -> Unit) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                _roomState.value?.let { currentRoom ->
                    repository.setDrawnWords(roomId, words, onSuccess)
                }
            }
        }
    }

    fun appendUsedWords(currentUsedWords: List<String>, newUsedWords: List<String>, onSuccess: () -> Unit){
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                _roomState.value?.let { currentRoom ->
                    repository.appendUsedWords(roomId,currentUsedWords, newUsedWords, onSuccess)
                }
            }
        }
    }

    fun setPlayerOnlineStatus(isOwner: Boolean, isOnline: Boolean, onSuccess: () -> Unit) {
        currentRoomId?.let { roomId ->
            viewModelScope.launch {
                repository.setPlayerOnlineStatus(roomId, isOwner, isOnline, onSuccess)
            }
        }
    }

    fun isAnswerCorrect(
        wordToVerify: String,
        answer: String
    ): Boolean{
        val normalizedWordToVerify = wordToVerify.normalize()
        val normalizedAnswer = answer.normalize()
        return normalizedWordToVerify == normalizedAnswer

    }

    fun verifyAnswer(
        answer: String,
        room: Room,
        onSuccess: () -> Unit
    ){
        val normalizedWordToVerify: String = room.drawnWords[room.chosenWordIndex].name.normalize()
        val indexPoints: Int = room.cluesShown
        val isOwner: Boolean = room.owner.id == loggedUserId
        Log.d("RoomViewModel", "Verificando resposta normalizedWordToVerify = $normalizedWordToVerify e  answer.normalize() = ${answer.normalize()}")
        if(isAnswerCorrect(normalizedWordToVerify, answer.normalize())){
            val currentScore = if(isOwner) room.owner.score else room.guest.score
            setScore(isOwner, indexPoints, currentScore){
                val nextStatus = getNextStatus(room, true)
                setStatus(nextStatus){}
            }
        }else{
        val nextStatus = getNextStatus(room, false)
        setStatus(nextStatus){}
        }
    }

    fun verifyTimeOut(room: Room, onSuccess: () -> Unit){
        val newStatus = when{
            room.round == Room.NUMBER_OF_ROUNDS && room.cluesShown == 2 -> RoomStatusesEnum.FINISHED.name
            room.round < Room.NUMBER_OF_ROUNDS && room.cluesShown == 2 -> RoomStatusesEnum.ROUND_FINISHED_WITHOUT_ANSWER.name
            else -> RoomStatusesEnum.GOT_NO_ANSWER_OWNER.name
        }
        setStatus(newStatus){
            onSuccess()
        }
    }

    fun getNextStatus(room: Room, isAnswerCorrect: Boolean): String{
        return when{
            room.round == Room.NUMBER_OF_ROUNDS && room.cluesShown == 2 && !isAnswerCorrect -> RoomStatusesEnum.FINISHED.name
            room.round == Room.NUMBER_OF_ROUNDS && isAnswerCorrect -> RoomStatusesEnum.FINISHED.name
            room.round < Room.NUMBER_OF_ROUNDS && room.cluesShown == 2 && isAnswerCorrect -> RoomStatusesEnum.ROUND_FINISHED_WITH_WINNER.name
            room.round < Room.NUMBER_OF_ROUNDS && room.cluesShown == 2 && !isAnswerCorrect -> RoomStatusesEnum.ROUND_FINISHED_WITHOUT_WINNER.name
            isAnswerCorrect -> RoomStatusesEnum.GOT_CORRECT_ANSWER.name
        else -> RoomStatusesEnum.GOT_WRONG_ANSWER.name
        }
    }

    fun startNewGame(nextWordIndex: Int, nextClueShown: Int, nextRound: Int, onSuccess: () -> Unit){
        setChosenWordIndex(nextWordIndex){
            setCluesShown(nextClueShown){
                setWordUsed(nextWordIndex){
                    setRound(nextRound) {
                        setStatus(RoomStatusesEnum.PLAYING.name){
                            onSuccess()
                        }
                    }
                }
            }
        }
    }

    fun startNewRound(nextWordIndex: Int, nextClueShown: Int, nextRound: Int, onSuccess: () -> Unit){
        setChosenWordIndex(nextWordIndex){
            setCluesShown(nextClueShown){
                setWordUsed(nextWordIndex){
                    setRound(nextRound) {
                        setStatus(RoomStatusesEnum.PLAYING.name){
                            onSuccess()
                        }
                    }
                }
            }
        }
    }

    fun passTurn(isOwnerTurn: Boolean, nextCluesShown: Int, onSuccess: () -> Unit){
        setOwnerTurn(!isOwnerTurn){
            setCluesShown(nextCluesShown){
                setStatus(RoomStatusesEnum.PLAYING.name){
                    onSuccess()
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

    fun getRandomSetOfWords(wordsUsed: List<String>, callback: (List<Word>) -> Unit) {
        repository.getRandomSetOfWords(wordsUsed = wordsUsed){ words ->
            callback(words)
        }
    }
}