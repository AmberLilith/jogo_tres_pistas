package com.br.amber.jogodastrespistas.data

import android.util.Log
import com.br.amber.jogodastrespistas.models.Room
import com.br.amber.jogodastrespistas.models.Word
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class RoomRepository {
    private val database = FirebaseDatabase.getInstance()
    private val roomsRef = database.getReference("rooms")

    fun getRoomUpdates(roomId: String): Flow<Room> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshot.getValue(Room::class.java)?.copy(id = roomId)
                room?.let { trySend(it) }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        roomsRef.child(roomId).addValueEventListener(listener)

        awaitClose {
            roomsRef.child(roomId).removeEventListener(listener)
        }
    }

    fun setScore(roomId: String, isOwner: Boolean, newScore: Int, onSuccess: () -> Unit) {
        val path = if (isOwner) "owner/score" else "guest/score"
        val pointsRef = roomsRef.child(roomId).child(path)
        pointsRef.setValue(newScore).addOnSuccessListener {
            Log.d("Firebase", "Score atualizado para $newScore com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar o score: ${error.message}")
        }
    }

    fun setTurn(roomId: String, isOwnerTurn: Boolean, onSuccess: () -> Unit) {
        roomsRef.child(roomId).child("ownerTurn").setValue(isOwnerTurn).addOnSuccessListener {
            Log.d("Firebase", "ownerTurn atualizado para $isOwnerTurn com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar o ownerTurn: ${error.message}")
        }
    }

    fun setChosenWordIndex(roomId: String, wordIndex: Int, onSuccess: () -> Unit) {
        roomsRef.child(roomId).child("chosenWordIndex").setValue(wordIndex).addOnSuccessListener {
            Log.d("Firebase", "chosenWordIndex atualizada para $wordIndex com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar chosenWordIndex: ${error.message}")
        }
    }

    fun setStatus(roomId: String, status: String, onSuccess: () -> Unit) {
        roomsRef.child(roomId).child("status").setValue(status).addOnSuccessListener {
            Log.d("Firebase", "Status atualizado para $status com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar o status: ${error.message}")
        }
    }

    fun setCluesShown(roomId: String, nextCluesShown: Int, onSuccess: () -> Unit) {
        roomsRef.child(roomId).child("cluesShown").setValue(nextCluesShown).addOnSuccessListener {
            Log.d("Firebase", "Quantidade de pistas exibidas atualizada para $nextCluesShown com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar a qantidade de pistas exibidas: ${error.message}")
        }
    }

    fun setRound(roomId: String, round: Int, onSuccess: () -> Unit){
        roomsRef.child(roomId).child("round").setValue(round).addOnSuccessListener {
            Log.d("Firebase", "Round atualizado poara $round com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar o round: ${error.message}")
        }
    }

    fun setDrawnWords(roomId: String, words: List<Word>, onSuccess: () -> Unit) {
        roomsRef.child(roomId).child("drawnWords").setValue(words).addOnSuccessListener {
            Log.d("Firebase", "dransWords atualizado com novas palavras com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar dransWords com novas palavras: ${error.message}")
        }
    }
    fun appendUsedWords(roomId: String, currentUsedWords: List<String>, newUsedWords: List<String>, onSuccess: () -> Unit){
        val wordsToupdate = currentUsedWords.plus(newUsedWords)
        roomsRef.child(roomId).child("usedWords").setValue(wordsToupdate).addOnSuccessListener {
            Log.d("Firebase", "usedWords atualizado para $wordsToupdate com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar usedWords: ${error.message}")
        }
    }

    fun setPlayerOnlineStatus(roomId: String, isOwner: Boolean, online: Boolean, onSuccess: () -> Unit) {
        val path = if (isOwner) "owner/online" else "guest/online"
        roomsRef.child(roomId).child(path).setValue(online).addOnSuccessListener {
            Log.d("Firebase", "Status online de ${if (isOwner) "owner" else "guest"} atualizado para $online com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar o status online de ${if (isOwner) "owner" else "guest"}: ${error.message}")
        }
    }

    fun setWordUsed(roomId: String, wordIndex: Int, onSuccess: () -> Unit) {
        val path = "drawnWords/$wordIndex/used"
        roomsRef.child(roomId).child(path).setValue(true).addOnSuccessListener {
            Log.d("Firebase", "Status usado da palavra índice $wordIndex atualizado para true com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar o status usado da palavra índice $wordIndex: ${error.message}")
        }
    }

    fun deleteRoom(roomId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        roomsRef.child(roomId).removeValue()
            .addOnSuccessListener {
                Log.d("Firebase", "Sala $roomId removida com sucesso")
                onSuccess()
            }
            .addOnFailureListener { error ->
                Log.e("Firebase", "Erro ao remover sala $roomId: ${error.message}")
                onFailure(error)
            }
    }

    fun getRandomSetOfWords(limit: Int = Room.NUMBER_OF_ROUNDS, wordsUsed: List<String>, callback: (List<Word>) -> Unit) {
        val wordsRef = FirebaseDatabase.getInstance().getReference("words")
        wordsRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val wordList = snapshot.children.mapNotNull { childSnapshot ->
                    childSnapshot.getValue(Word::class.java)
                }
                    .filterNot { word ->
                        word.name in wordsUsed
                    }
                    .shuffled()
                    .take(limit)

                callback(wordList)
            } else {
                callback(emptyList())
            }
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Erro ao obter palavras: ${exception.message}")
            callback(emptyList())
        }
    }

}