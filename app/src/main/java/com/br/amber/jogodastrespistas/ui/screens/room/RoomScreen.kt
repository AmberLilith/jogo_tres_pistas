package com.br.amber.jogodastrespistas.ui.screens.room

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.br.amber.jogodastrespistas.data.RoomRepository
import com.br.amber.jogodastrespistas.ui.components.indicators.LoadingIndicator
import com.br.amber.jogodastrespistas.ui.components.scaffolds.GameScaffold
import com.br.amber.jogodastrespistas.ui.screens.room.components.RoomContent

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

    GameScaffold(
        title = "Sala ${room?.id ?: ""}",
        onBackClick = { navController.popBackStack() }
    ) { innerPadding ->
        when {
            room == null -> LoadingIndicator("Carregando sala...")

            room?.id.isNullOrBlank() -> {
                Text(
                    "Erro ao carregar a sala",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                )
            }

            else -> RoomContent(
                room = room!!,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}