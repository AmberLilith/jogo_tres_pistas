package com.br.amber.jogodastrespistas.ui.screens.room

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.br.amber.jogodastrespistas.enums.ScoreEnum
import com.br.amber.jogodastrespistas.models.Room
import com.br.amber.jogodastrespistas.models.RoomStatusesEnum
import com.br.amber.jogodastrespistas.ui.components.dialogs.DefaultDialog
import com.br.amber.jogodastrespistas.ui.components.indicators.LoadingIndicator

@Composable
fun RoomContent(
    room: Room?,
    innerPadding: PaddingValues,
    navController: NavHostController,
    roomViewModel: RoomViewModel
) {

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
                    var showDialogOpponentHasLeft by remember { mutableStateOf(false) }
                    var showDialogGameOver by remember { mutableStateOf(false) }
                    val isLoggedUserOwner = safeRoom.owner.id == roomViewModel.loggedUserId
                    val isLoggedUserGuest = safeRoom.guest.id == roomViewModel.loggedUserId
                    val loggedUserName = if (isLoggedUserOwner) safeRoom.owner.nickName else safeRoom.guest.nickName
                    val opponentName = if (isLoggedUserOwner) safeRoom.guest.nickName else safeRoom.owner.nickName
                    val bothPlayersAreOnline = room.owner.online && room.guest.online

                    DefaultDialog(
                        showDialog = showDialogOpponentHasLeft,
                        "Jogo encerrado!",
                        backgroundTransparent = false
                    ) {
                        val whoHasLeft =
                            if (!room.owner.online) room.owner.nickName else room.guest.nickName

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

                        Spacer(modifier = Modifier.height(16.dp))


                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(onClick = {
                                roomViewModel.updateStatus(
                                    RoomStatusesEnum.DELETED.status,
                                    onSuccess = {
                                        roomViewModel.deleteRoom(
                                            onSuccess = {
                                                showDialogOpponentHasLeft = false
                                                navController.popBackStack()
                                            },
                                            onFailure = { error ->
                                                Log.e(
                                                    "Firebase",
                                                    "Erro ao deletar a sala: ${error.message}"
                                                )
                                            }
                                        )
                                    })
                            }
                            ) {
                                Text("Sair")
                            }
                        }

                    }

                    DefaultDialog(
                        showDialog = showDialogGameOver,
                        "Jogo Finalizado!",
                        backgroundTransparent = false
                    ) {
                        val winner = when {
                            room.owner.points > room.guest.points -> if (roomViewModel.loggedUserId == room.owner.id) "Você venceu!" else "${room.owner.nickName} venceu!"
                            room.guest.points > room.owner.points -> if (roomViewModel.loggedUserId == room.guest.id) "Você venceu!" else "${room.guest.nickName} venceu!"
                            else -> "Jogo empatado!"
                        }
                        Column {
                            Text(
                                text = "O jogo terminou! $winner",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "${if(isLoggedUserOwner) "Você" else room.owner.nickName} : ${room.owner.points} pts X ${if(isLoggedUserGuest) "Você" else room.guest.nickName})}: ${room.guest.points} pts",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Text(
                                text = if (bothPlayersAreOnline) "Deseja jogar novamente?" else "Não é possível jogar novamente, pois seu adversário saiu do jogo!",
                                color = if (bothPlayersAreOnline) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                            )

                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {},
                                enabled = bothPlayersAreOnline
                            ) {
                                Text("Nova Partida")
                            }

                            Button(
                                onClick = {
                                    if (bothPlayersAreOnline) {
                                        showDialogGameOver = false
                                        leaveGame(
                                            roomViewModel,
                                            navController,
                                            isLoggedUserOwner
                                        )
                                    } else {
                                        roomViewModel.updateStatus(
                                            RoomStatusesEnum.DELETED.status,
                                            onSuccess = {
                                                roomViewModel.deleteRoom(
                                                    onSuccess = {
                                                        showDialogGameOver = false
                                                        navController.popBackStack()
                                                    },
                                                    onFailure = { error ->
                                                        Log.e(
                                                            "Firebase",
                                                            "Erro ao deletar a sala: ${error.message}"
                                                        )
                                                    }
                                                )
                                            })
                                    }
                                }
                            ) {
                                Text("Sair")

                            }
                        }


                    }

                    when (safeRoom.status) {
                        RoomStatusesEnum.WAITING.status -> {
                            LoadingIndicator("Aguardando adversário...")
                        }

                        RoomStatusesEnum.PLAYING.status -> {
                            if ((isLoggedUserOwner && !safeRoom.guest.online) ||
                                (isLoggedUserGuest && !safeRoom.owner.online)
                            ) {
                                showDialogOpponentHasLeft = true
                            } else {
                                StartGame(
                                    safeRoom,
                                    roomViewModel,
                                    navController,
                                    isLoggedUserOwner,
                                    loggedUserName,
                                    opponentName
                                )
                            }
                        }

                        RoomStatusesEnum.FINISHED.status -> {
                            if (bothPlayersAreOnline) {
                                showDialogGameOver = true
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Clues(
    room: Room,
    roomViewModel: RoomViewModel,
    loggedUserName: String,
    opponentName: String,
    updateShowClues: () -> Unit
) {
    if (room.chosenWordIndex > -1) {
        var textInput by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "${ScoreEnum.HIGHER.points} pts - ${room.drawnWords[room.chosenWordIndex].clues[0]}",
                modifier = Modifier.align(Alignment.Start)
            )


            if (room.cluesShown > 0) {
                Text(
                    text = "${ScoreEnum.MEDIAN.points} pts - ${room.drawnWords[room.chosenWordIndex].clues[1]}",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }


            if (room.cluesShown > 1) {
                Text(
                    text = "${ScoreEnum.LOWER.points} pts - ${room.drawnWords[room.chosenWordIndex].clues[2]}",
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
                    onClick = {
                        updateShowClues()
                        roomViewModel.verifyAswer(textInput.toString(), room)
                              },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enviar")
                }

            }else{
                LoadingIndicator("Aguardando a resposta de $opponentName...")
            }

        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StartGame(
    safeRoom: Room,
    roomViewModel: RoomViewModel,
    navController: NavHostController,
    isLoggedUserOwner: Boolean,
    loggedUserName: String,
    opponentName: String
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showChooseWordDialog by remember { mutableStateOf(false) }
    var showClues by remember { mutableStateOf(false) }

    LaunchedEffect(safeRoom.round) {
        if ((isLoggedUserOwner && safeRoom.ownerTurn) || (!isLoggedUserOwner && !safeRoom.ownerTurn)) {
            showChooseWordDialog = true
        }
    }

    LaunchedEffect(safeRoom.cluesShown) {
        if (safeRoom.cluesShown > -1) {
            showClues = true
        }
    }

    Text(
        "Rodada: ${safeRoom.round + 1}",
        style = MaterialTheme.typography.bodyLarge
    )

    Text("Pontuações:", style = MaterialTheme.typography.titleMedium)
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if(isLoggedUserOwner) "Você" else safeRoom.owner.nickName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "${safeRoom.owner.points} pts",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if(!isLoggedUserOwner) "Você" else safeRoom.guest.nickName,
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

    DefaultDialog(
        showDialog = showConfirmDialog,
        "Confirmação!",
        backgroundTransparent = false
    ) {

        Column {
            Text(
                text = "Tem certeza que deseja sair do jogo?",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

        }

        Spacer(modifier = Modifier.height(16.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = {
                showConfirmDialog = false
            }
            ) {
                Text("Cancelar")
            }

            Button(onClick = {
                leaveGame(roomViewModel, navController, isLoggedUserOwner)
                showConfirmDialog = false
            }
            ) {
                Text("Sim")
            }
        }

    }


    DefaultDialog(
        showDialog = showChooseWordDialog,
        "Escolha um cartão!",
        backgroundTransparent = false,
        content = {
            var showProgressbar by remember { mutableStateOf(false) }
            if(!showProgressbar){
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    safeRoom.drawnWords.forEachIndexed { index, word ->
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(50.dp)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!word.used) Color.Blue else Color.Gray)
                                .clickable {
                                    if (!word.used) {
                                        showProgressbar = true
                                        roomViewModel.updateChosenWordIndex(index) {
                                            roomViewModel.updateCluesShown(0) {
                                                roomViewModel.updateWordUsed(index) {
                                                    showChooseWordDialog = false
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if(word.used){
                                Text(word.name,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }else{
                LoadingIndicator("Carregando dicas...")
            }
        }

    )

    Spacer(modifier = Modifier.padding(8.dp))

    if (showClues) {
        Clues(safeRoom, roomViewModel, loggedUserName,opponentName) { showClues = false }
    } else {
        LoadingIndicator("Aguardando $opponentName escolhe a próxima palavra...")
    }
}

internal fun leaveGame(
    roomViewModel: RoomViewModel,
    navController: NavHostController,
    isLoggedUserOwner: Boolean
) {
    roomViewModel.setPlayerOnlineStatus(isLoggedUserOwner, false) {}
    navController.popBackStack()
}