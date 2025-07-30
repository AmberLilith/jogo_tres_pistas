package com.br.amber.jogodastrespistas.data

import android.util.Log
import com.br.amber.jogodastrespistas.models.Room
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

    suspend fun updateRoom(room: Room) {
        roomsRef.child(room.id).setValue(room)
    }

    fun updatePoints(roomId: String, isOwner: Boolean, points: Int) {
        val path = if (isOwner) "owner/points" else "guest/points"
        val pointsRef = roomsRef.child(roomId).child(path)

        pointsRef.get().addOnSuccessListener { snapshot ->
            val currentPoints = snapshot.getValue(Int::class.java) ?: 0

            val newPoints = currentPoints + points

            pointsRef.setValue(newPoints)
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao ler pontos: ${error.message}")
        }
    }

    fun updateTurn(roomId: String, isOwnerTurn: Boolean, onSuccess: () -> Unit) {
        roomsRef.child(roomId).child("ownerTurn").setValue(!isOwnerTurn).addOnSuccessListener {
            Log.d("Firebase", "Turno atualizado com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar o turno: ${error.message}")
        }
    }

    fun updateStatus(roomId: String, status: String, onSuccess: () -> Unit) {
        roomsRef.child(roomId).child("status").setValue(status).addOnSuccessListener {
            Log.d("Firebase", "Status atualizado com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar o status: ${error.message}")
        }
    }

    fun updateCluesShown(roomId: String, count: Int, onSuccess: () -> Unit) {
        roomsRef.child(roomId).child("cluesShown").setValue(count).addOnSuccessListener {
            Log.d("Firebase", "Quantidade de pistas exibidas atualizada com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar a qantidade de pistas exibidas: ${error.message}")
        }
    }

    fun updateRound(roomId: String, round: Int, onSuccess: () -> Unit){
        roomsRef.child(roomId).child("round").setValue(round).addOnSuccessListener {
            Log.d("Firebase", "Round atualizado com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar o round: ${error.message}")
        }
    }

    fun updatePlayerOnlineStatus(roomId: String, isOwner: Boolean, online: Boolean, onSuccess: () -> Unit) {
        val path = if (isOwner) "owner/online" else "guest/online"
        roomsRef.child(roomId).child(path).setValue(online).addOnSuccessListener {
            Log.d("Firebase", "Status online de ${if (isOwner) "owner" else "guest"} atualizado com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar o status online de ${if (isOwner) "owner" else "guest"}: ${error.message}")
        }
    }

    fun updateWordUsed(roomId: kotlin.String, wordIndex: Int, onSuccess: () -> Unit) {
        val path = "drawnWords/$wordIndex/used"
        roomsRef.child(roomId).child(path).setValue(true).addOnSuccessListener {
            Log.d("Firebase", "Status usado da palavra índice $wordIndex atualizado com sucesso")
            onSuccess()
        }.addOnFailureListener { error ->
            Log.e("Firebase", "Erro ao atualizar o status usado da palavra índice $wordIndex: ${error.message}")
        }
    }
}