package com.br.amber.jogodastrespistas.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.br.amber.jogodastrespistas.navigation.RoutesEnum
import com.br.amber.jogodastrespistas.ui.components.CenteredBodyText
import com.br.amber.jogodastrespistas.ui.components.CenteredTitleText
import com.br.amber.jogodastrespistas.ui.components.DefaultButton
import com.br.amber.jogodastrespistas.ui.components.DefaultScreen
import com.br.amber.jogodastrespistas.ui.theme.WaitingGuestCardFirstColor
import com.br.amber.jogodastrespistas.ui.theme.WaitingGuestCardSecondColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel()
) {
    val waitingRooms by homeViewModel.waitingRooms.collectAsState(initial = emptyList())

    val createdRoomId by homeViewModel.createdRoomId.collectAsState()
    val errorMessage by homeViewModel.errorMessage.collectAsState()

    val context = LocalContext.current


    // Inicia a escuta das salas esperando atualização
    LaunchedEffect(Unit) {
        homeViewModel.observeWaitingRooms()
    }

    LaunchedEffect(createdRoomId) {
        if (!createdRoomId.isNullOrEmpty()) {
            navController.navigate(RoutesEnum.roomWithId(createdRoomId!!))
            homeViewModel.clearRoomId()
        }
    }

    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrEmpty()) {
            Toast
                .makeText(context, errorMessage, Toast.LENGTH_SHORT)
                .show()
            homeViewModel.clearErrorMessage()
        }
    }

    DefaultScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {

            DefaultButton(
                text = "Criar Sala",
                fillMaxWidth = true
            ) {
                homeViewModel.createRoom()
            }

            Spacer(modifier = Modifier.padding(8.dp))

            waitingRooms.forEachIndexed { index, room ->
                Card(modifier = Modifier.padding(8.dp)) {
                    Column(
                        modifier = Modifier
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(WaitingGuestCardFirstColor,
                                        WaitingGuestCardSecondColor
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        CenteredTitleText("Sala ${index + 1}", true)

                        HorizontalDivider(thickness = 2.dp)

                        CenteredBodyText("${room.owner.nickName} aguardando adversário!", true, padding = 12)

                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
                        ) {
                            DefaultButton(
                                text = "Entrar",
                                fillMaxWidth = false,

                            ) {
                                homeViewModel.joinRoomAsGuest(// Só navega para a tela da sala quando os dados de guest forem atualizados
                                    room.id
                                ){
                                    navController.navigate(RoutesEnum.roomWithId(room.id))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

