package com.br.amber.jogodastrespistas.data

import com.br.amber.jogodastrespistas.models.Player
import com.br.amber.jogodastrespistas.models.Room
import com.br.amber.jogodastrespistas.models.RoomStatusesEnum
import com.br.amber.jogodastrespistas.models.Word
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.random.Random

class RoomRepository {

    private val roomsRef = FirebaseDatabase.getInstance().getReference("rooms")
    private val wordsRef = FirebaseDatabase.getInstance().getReference("words")
    private val auth = FirebaseAuth.getInstance()

    fun createRoom(
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val ownerId = auth.currentUser?.uid
        val ownerName = auth.currentUser?.displayName?.split(" ")?.get(0) ?: "Anônimo_${Random.nextInt(100, 9999)}"
        if (ownerId == null) {
            onError(Exception("Usuário não autenticado"))
            return
        }
        getRandomWords { words ->
            val roomRef = roomsRef.push()
            val roomId = roomRef.key ?: return@getRandomWords onError(Exception("Erro ao gerar ID"))

            val room = Room(
                id = roomId,
                owner = Player(id = ownerId, nickName = ownerName, isOnline = true),
                drawnWords = words
            )

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

