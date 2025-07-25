package com.br.amber.jogodastrespistas.data

import com.br.amber.jogodastrespistas.models.Player
import com.br.amber.jogodastrespistas.models.Room
import com.br.amber.jogodastrespistas.models.RoomStatusesEnum
import com.br.amber.jogodastrespistas.models.Word
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class RoomRepository {

    private val roomsRef = FirebaseDatabase.getInstance().getReference("rooms")
    private val wordsRef = FirebaseDatabase.getInstance().getReference("words")
    private val auth = FirebaseAuth.getInstance()

    fun createRoom(
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val ownerId = auth.currentUser?.uid
        if (ownerId == null) {
            onError(Exception("Usuário não autenticado"))
            return
        }
        getRandomWords { words ->
            val room = Room(
                owner = Player(id = ownerId, isOnline = true),  // Player criado aqui
                drawnWords = words
            )

            val roomRef = roomsRef.push()
            val roomId = roomRef.key ?: return@getRandomWords onError(Exception("Erro ao gerar ID"))

            roomRef.setValue(room)
                .addOnSuccessListener { onSuccess(roomId) }
                .addOnFailureListener { onError(it) }
        }
    }

    private fun getRandomWords(limit: Int = 10, callback: (List<Word>) -> Unit) {
        wordsRef.get().addOnSuccessListener { snapshot ->
            val wordList = snapshot.children.mapNotNull {
                it.getValue(Word::class.java)
            }.shuffled().take(limit)
            callback(wordList)
        }
    }

    fun listenWaitingRooms(onUpdate: (List<Room>) -> Unit, onError: (Exception) -> Unit) {
        roomsRef.orderByChild("status").equalTo(RoomStatusesEnum.WAITING.status)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rooms = snapshot.children.mapNotNull { roomSnapshot ->
                        roomSnapshot.getValue(Room::class.java)?.let { room ->
                            // Atualiza o ID do owner com a chave do snapshot se necessário
                            if (room.owner.id.isEmpty()) {
                                room.owner.id = roomSnapshot.key ?: ""
                            }
                            room
                        }
                    }
                    onUpdate(rooms)
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error.toException())
                }
            })
    }


}

