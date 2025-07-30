package com.br.amber.jogodastrespistas.ui.screens.room

import android.util.Log
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
    ) { innerPadding ->
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
                        val isLoggedUserOwner = safeRoom.owner.id == roomViewModel.loggedUserId
                        val isLoggedUserGuest = safeRoom.guest.id == roomViewModel.loggedUserId

                        when(safeRoom.status){
                            RoomStatusesEnum.WAITING.status -> {
                                LoadingIndicator("Aguardando adversário...")
                            }

                            RoomStatusesEnum.PLAYING.status -> {
                                if((isLoggedUserOwner && !safeRoom.guest.online) ||
                                   (isLoggedUserGuest && !safeRoom.owner.online)
                                    ){
                                    SimpleOpponentHasLeft(
                                        safeRoom,
                                        onExit = {
                                            leaveGame(roomViewModel, navController, isLoggedUserOwner)
                                        }
                                    )
                                }else {
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
}