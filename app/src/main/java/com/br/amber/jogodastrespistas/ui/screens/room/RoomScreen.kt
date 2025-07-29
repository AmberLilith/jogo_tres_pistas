package com.br.amber.jogodastrespistas.ui.screens.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.br.amber.jogodastrespistas.data.RoomRepository
import com.br.amber.jogodastrespistas.enums.ScoreEnum
import com.br.amber.jogodastrespistas.models.Room
import com.br.amber.jogodastrespistas.models.RoomStatusesEnum
import com.br.amber.jogodastrespistas.ui.components.indicators.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    navController: NavHostController,
    roomId: String
) {
    val repository = remember { RoomRepository() }
    val roomViewModel: RoomViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RoomViewModel(repository) as T
            }
        }
    )

    LaunchedEffect(roomId) {
        roomViewModel.init(roomId)
    }

    val room by roomViewModel.roomState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        text = "JOGO DAS 3 PISTAS",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ){ innerPadding ->
        when {
            room == null -> {
                LoadingIndicator("Carregando sala...")
            }
            room?.id.isNullOrBlank() -> {
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
                    room?.let { safeRoom ->
                        if (safeRoom.status == RoomStatusesEnum.WAITING.status) {
                            LoadingIndicator("Aguardando adversário...")
                        }

                        if (safeRoom.status == RoomStatusesEnum.PLAYING.status) {
                            Text("Round: ${safeRoom.round + 1}", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Vez de: ${if (safeRoom.ownerTurn) safeRoom.owner.nickName else safeRoom.guest.nickName}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            // Pontuação
                            Text("Pontuações:", style = MaterialTheme.typography.titleMedium)
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(safeRoom.owner.nickName, style = MaterialTheme.typography.bodyMedium)
                                    Text("${safeRoom.owner.points} pts", style = MaterialTheme.typography.bodyLarge)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(safeRoom.guest.nickName, style = MaterialTheme.typography.bodyMedium)
                                    Text("${safeRoom.guest.points} pts", style = MaterialTheme.typography.bodyLarge)
                                }
                            }

                            Spacer(modifier = Modifier.padding(8.dp))

                            Clues(safeRoom, roomViewModel)
                        }
                        if(safeRoom.status == RoomStatusesEnum.FINISHED.status){
                            val isLoggedUserOwner = safeRoom.owner.id == roomViewModel.loggedUserId
                            SimpleGameOverDialog(
                                safeRoom,
                                roomViewModel,
                                onRestart = { /*viewModel.restartGame()*/ },
                                onExit = {
                                    roomViewModel.setPlayerOnlineStatus(isLoggedUserOwner, false)
                                    navController.popBackStack() }
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


        if(room.cluesShown > 0){
            Text(
                text = "${ScoreEnum.MEDIAN.points} pts - ${room.drawnWords[room.round].clues[1]}",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }


        if(room.cluesShown > 1){
            Text(
                text = "${ScoreEnum.LOWER.points} pts - ${room.drawnWords[room.round].clues[2]}",
                modifier = Modifier.align(Alignment.End)
            )
        }

        if(
            (roomViewModel.loggedUserId == room.owner.id && room.ownerTurn) ||
            (roomViewModel.loggedUserId == room.guest.id && !room.ownerTurn)
        ){

            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Digite algo") },
                modifier = Modifier.fillMaxWidth()
            )

            // Botão
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
fun SimpleGameOverDialog(room: Room, roomViewModel: RoomViewModel, onRestart: () -> Unit, onExit: () -> Unit) {
    val winner = when {
        room.owner.points > room.guest.points -> if(roomViewModel.loggedUserId == room.owner.id) "Você venceu!" else "${room.owner.nickName} venceu!"
        room.guest.points > room.owner.points -> if(roomViewModel.loggedUserId == room.guest.id) "Você venceu!" else "${room.guest.nickName} venceu!"
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
                        text = if(bothPlayersAreOnline) "Deseja jogar novamente?"  else "Não é possível jogar novamente, pois seu adversário saiu do jogo!",
                        color = if(bothPlayersAreOnline) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
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