package com.br.amber.jogodastrespistas.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.br.amber.jogodastrespistas.navigation.RoutesEnum
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Bem-vindo ao Jogo das Três Pistas!")
                }
            )
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Button(
                onClick = { homeViewModel.createRoom() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Criar Sala")
            }

            Spacer(modifier = Modifier.padding(8.dp))

            waitingRooms.forEachIndexed { index, room ->
                Card {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Sala ${index + 1}",
                            fontSize = 18.sp,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                        )

                        HorizontalDivider(thickness = 2.dp)

                        Text(
                            text = "${room.owner.nickName} aguardando adversário!"
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
                        ) {
                            Button(
                                onClick = {
                                    homeViewModel.joinRoomAsGuest(// Só navega para a tela da sala quando os dados de guest forem atualizados
                                        room.id
                                    ){
                                        navController.navigate(RoutesEnum.roomWithId(room.id))
                                    }
                                }
                            ) {
                                Text(
                                    text = "Entrar"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(8.dp))

            }
        }
    }
}

