package com.br.amber.jogodastrespistas.ui.screens.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
    val viewModel: RoomViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RoomViewModel(repository) as T
            }
        }
    )

    LaunchedEffect(roomId) {
        viewModel.init(roomId)
    }

    val room by viewModel.roomState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        text = "Sala ${room?.id ?: ""}",
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
                            Text("Round: ${safeRoom.round}", style = MaterialTheme.typography.bodyLarge)
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

                            Tips(safeRoom)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Tips(room: Room) {
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "10 pts - ${room.drawnWords[room.round].clues[0]}",
            modifier = Modifier.align(Alignment.Start)
        )


        if(room.cluesShown > 1){
            Text(
                text = "9 pts - ${room.drawnWords[room.round].clues[1]}",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }


        if(room.cluesShown > 2){
            Text(
                text = "8 pts - ${room.drawnWords[room.round].clues[2]}",
                modifier = Modifier.align(Alignment.End)
            )
        }

        // Campo de entrada (TextField)
        TextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Digite algo") },
            modifier = Modifier.fillMaxWidth()
        )

        // Botão
        Button(
            onClick = { /* Ação do botão */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar")
        }
    }
}