package com.br.amber.jogodastrespistas.data

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
        roomsRef.child(roomId).child(path).setValue(points)
    }

    fun updateTurn(roomId: String, isOwnerTurn: Boolean) {
        roomsRef.child(roomId).child("ownerTurn").setValue(isOwnerTurn)
    }

    fun updateStatus(roomId: String, status: String) {
        roomsRef.child(roomId).child("status").setValue(status)
    }

    fun updateCluesShown(roomId: String, count: Int) {
        roomsRef.child(roomId).child("cluesShown").setValue(count)
    }

    fun updateRound(roomId: String, round: Int) {
        roomsRef.child(roomId).child("round").setValue(round)
    }

    fun updatePlayerOnlineStatus(roomId: String, isOwner: Boolean, isOnline: Boolean) {
        val path = if (isOwner) "owner/isOnline" else "guest/isOnline"
        roomsRef.child(roomId).child(path).setValue(isOnline)
    }
}