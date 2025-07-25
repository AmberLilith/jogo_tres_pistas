package com.br.amber.jogodastrespistas.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.br.amber.jogodastrespistas.navigation.RoutesEnum
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.br.amber.jogodastrespistas.models.Room

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {
    val waitingRooms by viewModel.waitingRooms.collectAsState(initial = emptyList())

    // Inicia a escuta das salas esperando atualização
    LaunchedEffect(Unit) {
        viewModel.observeWaitingRooms()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bem-vindo ao Jogo das Três Pistas!",
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = { viewModel.createRoom() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Criar Sala")
        }

        waitingRooms.forEach { room ->
            Text(text = "Sala de ${room.owner.id} - Status: ${room.status}")
        }
    }
}
