package com.br.amber.jogodastrespistas.ui.screens.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.br.amber.jogodastrespistas.data.RoomRepository

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

    // Tratamento do estado de loading/erro
    when {
        room == null -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text("Carregando sala...", modifier = Modifier.padding(8.dp))
            }
            return
        }
        room?.id.isNullOrBlank() -> {
            Text("Erro ao carregar a sala")
            return
        }
    }

    // Observar mudanças de status (com segurança)
    LaunchedEffect(room?.status) {
        room?.status?.let { status ->
            when (status) {
                //RoomStatusesEnum.ENDED.status -> navController.navigate(RoutesEnum.HOME.route)
                // Outros casos conforme necessário
            }
        }
    }

    // UI principal (room não é mais nulo aqui)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        room?.let { safeRoom ->
            // Informações da sala
            Text("Sala: ${safeRoom.id}", style = MaterialTheme.typography.titleLarge)
            Text("Status: ${safeRoom.status}")
            Text("Round: ${safeRoom.round}")
            Text("Vez do: ${if (safeRoom.ownerTurn) "Dono" else "Convidado"}")

            // Pontuação
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Dono")
                    Text("${safeRoom.owner.points} pts")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Convidado")
                    Text("${safeRoom.guest.points} pts")
                }
            }

            // Ações
            Button(
                onClick = { viewModel.advanceRound() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Próximo Round")
            }

            Button(
                onClick = { viewModel.changeTurn(!safeRoom.ownerTurn) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Trocar Turno")
            }
        }
    }
}