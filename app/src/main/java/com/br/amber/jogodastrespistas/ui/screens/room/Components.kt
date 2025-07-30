package com.br.amber.jogodastrespistas.ui.screens.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.br.amber.jogodastrespistas.enums.ScoreEnum
import com.br.amber.jogodastrespistas.models.Room
import com.br.amber.jogodastrespistas.models.RoomStatusesEnum
import com.br.amber.jogodastrespistas.ui.components.indicators.LoadingIndicator

@Composable
fun RoomContent(room: Room?, innerPadding: PaddingValues, navController: NavHostController, roomViewModel: RoomViewModel){
    when {
        room == null -> {
            LoadingIndicator("Carregando sala...")
        }

        room.id.isBlank() -> {
            Text(
                "Erro ao carregar a sala",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                room.let { safeRoom ->
                    val isLoggedUserOwner = safeRoom.owner.id == roomViewModel.loggedUserId
                    val isLoggedUserGuest = safeRoom.guest.id == roomViewModel.loggedUserId

                    when (safeRoom.status) {
                        RoomStatusesEnum.WAITING.status -> {
                            LoadingIndicator("Aguardando adversário...")
                        }

                        RoomStatusesEnum.PLAYING.status -> {
                            if ((isLoggedUserOwner && !safeRoom.guest.online) ||
                                (isLoggedUserGuest && !safeRoom.owner.online)
                            ) {
                                SimpleOpponentHasLeft(
                                    safeRoom,
                                    onExit = {
                                        leaveGame(
                                            roomViewModel,
                                            navController,
                                            isLoggedUserOwner
                                        )
                                    }
                                )
                            } else {
                                PlayingGame(
                                    safeRoom,
                                    roomViewModel,
                                    navController,
                                    isLoggedUserOwner
                                )
                            }
                        }

                        RoomStatusesEnum.FINISHED.status -> {
                            SimpleGameOverDialog(
                                safeRoom,
                                roomViewModel,
                                onRestart = {},
                                onExit = {
                                    leaveGame(roomViewModel, navController, isLoggedUserOwner)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Clues(room: Room, roomViewModel: RoomViewModel) {
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "${ScoreEnum.HIGHER.points} pts - ${room.drawnWords[room.round].clues[0]}",
            modifier = Modifier.align(Alignment.Start)
        )


        if (room.cluesShown > 0) {
            Text(
                text = "${ScoreEnum.MEDIAN.points} pts - ${room.drawnWords[room.round].clues[1]}",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }


        if (room.cluesShown > 1) {
            Text(
                text = "${ScoreEnum.LOWER.points} pts - ${room.drawnWords[room.round].clues[2]}",
                modifier = Modifier.align(Alignment.End)
            )
        }

        if (
            (roomViewModel.loggedUserId == room.owner.id && room.ownerTurn) ||
            (roomViewModel.loggedUserId == room.guest.id && !room.ownerTurn)
        ) {

            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Digite algo") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { roomViewModel.verifyAswer(textInput.toString(), room) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar")
            }

        }

    }
}

@Composable
fun SimpleGameOverDialog(
    room: Room,
    roomViewModel: RoomViewModel,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    val winner = when {
        room.owner.points > room.guest.points -> if (roomViewModel.loggedUserId == room.owner.id) "Você venceu!" else "${room.owner.nickName} venceu!"
        room.guest.points > room.owner.points -> if (roomViewModel.loggedUserId == room.guest.id) "Você venceu!" else "${room.guest.nickName} venceu!"
        else -> "Jogo empatado!"
    }
    val bothPlayersAreOnline = room.owner.online && room.guest.online
    AlertDialog(
        onDismissRequest = { /* não permite fechar clicando fora */ },
        title = {
            Text("Fim de Jogo!", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column {
                Text(
                    text = "O jogo terminou! $winner",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "${room.owner.nickName}: ${room.owner.points} pts X ${room.guest.nickName}: ${room.guest.points} pts",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = if (bothPlayersAreOnline) "Deseja jogar novamente?" else "Não é possível jogar novamente, pois seu adversário saiu do jogo!",
                    color = if (bothPlayersAreOnline) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )

            }
        },
        confirmButton = {
            Button(
                onClick = onRestart,
                enabled = bothPlayersAreOnline
            ) {
                Text("Nova Partida")
            }
        },
        dismissButton = {
            Button(
                onClick = onExit
            ) {
                Text("Sair")

            }
        }
    )
}

@Composable
fun SimpleOpponentHasLeft(room: Room, onExit: () -> Unit) {
    val whoHasLeft =
        if (!room.owner.online) room.owner.nickName else room.guest.nickName
    AlertDialog(
        onDismissRequest = { /* não permite fechar clicando fora */ },
        title = {
            Text("Jogo encerrado!", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column {
                Text(
                    text = "$whoHasLeft saiu do jogo!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Clique em sair para voltar para a tela inicial",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

            }
        },
        confirmButton = {
            Button(
                onClick = onExit
            ) {
                Text("Sair")

            }
        }
    )
}

@Composable
fun ConfirmActionDialog(dialogTitle: String,dialogText: String, confirmText: String, dismissText: String, onConfirm: () -> Unit, onDismiss: () -> Unit){
    AlertDialog(
        onDismissRequest = { /* não permite fechar clicando fora */ },
        title = {
            Text(dialogTitle, style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column {
                Text(
                    text = dialogText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text(confirmText)

            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(dismissText)

            }
        }
    )
}

@Composable
fun PlayingGame(safeRoom: Room, roomViewModel: RoomViewModel, navController: NavHostController, isLoggedUserOwner: Boolean){
    var showConfirmDialog by remember { mutableStateOf(false) }
    Text(
        "Round: ${safeRoom.round + 1}",
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        "Vez de: ${if (safeRoom.ownerTurn) safeRoom.owner.nickName else safeRoom.guest.nickName}",
        style = MaterialTheme.typography.bodyLarge
    )

    Text("Pontuações:", style = MaterialTheme.typography.titleMedium)
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                safeRoom.owner.nickName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "${safeRoom.owner.points} pts",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                safeRoom.guest.nickName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "${safeRoom.guest.points} pts",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {
                    showConfirmDialog = true
                }
            ) {
                Text("Sair")

            }
        }
    }

    if (showConfirmDialog) {
        ConfirmActionDialog(
            dialogTitle = "Confirmação",
            dialogText = "Tem certeza que deseja sair do jogo?",
            confirmText = "Sim",
            dismissText = "Cancelar",
            onConfirm = {
                leaveGame(roomViewModel, navController, isLoggedUserOwner)
                showConfirmDialog = false
            },
            onDismiss = {
                showConfirmDialog = false
            }
        )
    }

    Spacer(modifier = Modifier.padding(8.dp))

    Clues(safeRoom, roomViewModel)
}

internal fun leaveGame(
    roomViewModel: RoomViewModel,
    navController: NavHostController,
    isLoggedUserOwner: Boolean
) {
    roomViewModel.setPlayerOnlineStatus(isLoggedUserOwner, false) {}
    navController.popBackStack()
}