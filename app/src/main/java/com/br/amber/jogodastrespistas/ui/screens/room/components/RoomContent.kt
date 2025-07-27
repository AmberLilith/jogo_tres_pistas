package com.br.amber.jogodastrespistas.ui.screens.room.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.br.amber.jogodastrespistas.models.Room
import com.br.amber.jogodastrespistas.models.RoomStatusesEnum
import com.br.amber.jogodastrespistas.ui.components.indicators.LoadingIndicator

@Composable
fun RoomContent(
    room: Room,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (room.status) {
            RoomStatusesEnum.WAITING.status -> WaitingForOpponent()
            RoomStatusesEnum.PLAYING.status -> GameInProgress(room)
            // ... outros estados
        }
    }
}

@Composable
private fun GameInProgress(room: Room) {
    Text("Round: ${room.round}", style = MaterialTheme.typography.bodyLarge)
    Text("Vez de: ${if (room.ownerTurn) room.owner.nickName else room.guest.nickName}", style = MaterialTheme.typography.bodyLarge)

    PlayerScores(
        ownerName = room.owner.nickName,
        ownerPoints = room.owner.points,
        guestName = room.guest.nickName,
        guestPoints = room.guest.points
    )
}

@Composable
private fun PlayerScores(
    ownerName: String,
    ownerPoints: Int,
    guestName: String,
    guestPoints: Int
) {
    Text("Pontuações:", style = MaterialTheme.typography.titleMedium)
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        PlayerScore(name = ownerName, points = ownerPoints)
        PlayerScore(name = guestName, points = guestPoints)
    }
}

@Composable
fun WaitingForOpponent(
    modifier: Modifier = Modifier
) {
    LoadingIndicator("Aguardando adversario...")
}

@Composable
fun PlayerScore(
    name: String,
    points: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$points pts",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}