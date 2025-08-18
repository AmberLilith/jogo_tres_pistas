package com.br.amber.jogodastrespistas.ui.screens.room

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.br.amber.jogodastrespistas.enums.PointsEnum
import com.br.amber.jogodastrespistas.models.Room
import com.br.amber.jogodastrespistas.enums.RoomStatusesEnum
import com.br.amber.jogodastrespistas.ui.components.CountdownTimer
import com.br.amber.jogodastrespistas.ui.components.dialogs.DefaultDialog
import com.br.amber.jogodastrespistas.ui.components.indicators.LoadingIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoomContent(
    room: Room?,
    innerPadding: PaddingValues,
    navController: NavHostController,
    roomViewModel: RoomViewModel
)
{

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
                var showDialogConfirmExit by remember { mutableStateOf(false) }
                var text by remember { mutableStateOf("") }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = {
                            showDialogConfirmExit = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sair")

                    }
                }
                room.let { safeRoom ->
                    var showDialogOpponentHasLeft by remember { mutableStateOf(false) }
                    var showDialogGameOver by remember { mutableStateOf(false) }
                    val isLoggedUserOwner = safeRoom.owner.id == roomViewModel.loggedUserId
                    val isLoggedUserGuest = safeRoom.guest.id == roomViewModel.loggedUserId
                    val loggedUserName = if (isLoggedUserOwner) safeRoom.owner.nickName else safeRoom.guest.nickName
                    val opponentName = if (isLoggedUserOwner) safeRoom.guest.nickName else safeRoom.owner.nickName
                    val bothPlayersAreOnline = room.owner.online && room.guest.online

                    var showStartingNewGameDialog by remember { mutableStateOf(false) }
                    var showWaitingStartNewGameDialog by remember { mutableStateOf(false) }

                    var showChooseWordDialogNextRound by remember { mutableStateOf(false) }
                    var showWaitingWordChoiceDialogNextRound by remember { mutableStateOf(false) }

                    var showChooseWordDialogNewGame by remember { mutableStateOf(false) }
                    var showWaitingWordChoiceDialogNewGame by remember { mutableStateOf(false) }

                    var showVerifyingAnswerToWhoAnswered by remember { mutableStateOf(false) }
                    var showVerifyingAnswerToWhoWait by remember { mutableStateOf(false) }

                    var showGotWrongAnswerToWhoAnswered  by remember { mutableStateOf(false) }
                    var showGotWrongAnswerToWhoWait by remember { mutableStateOf(false) }

                    var showGotCorrectAnswerToWhoAnswered by remember { mutableStateOf(false) }
                    var showGotCorrectAnswerToWhoWait by remember { mutableStateOf(false) }

                    var showGotNoAnswerToWhoAnswered by remember { mutableStateOf(false) }
                    var showGotNoAnswerToWhoWait by remember { mutableStateOf(false) }

                    var showGotRoundFinishWithWrongAnswerToWhoAnswered by remember { mutableStateOf(false) }
                    var showGotRoundFinishWithWrongAnswerToWhoWait by remember { mutableStateOf(false) }

                    val loggedUserIsWhoAnswered = (room.owner.id == roomViewModel.loggedUserId && room.ownerTurn) || (room.guest.id == roomViewModel.loggedUserId && !room.ownerTurn)

                    val loggedUserIsOnline = (room.owner.id == roomViewModel.loggedUserId && room.owner.online) || (room.guest.id == roomViewModel.loggedUserId && room.guest.online)

                    val whoAnswered = if (loggedUserIsWhoAnswered) loggedUserName else opponentName


                    LaunchedEffect(room.status) {

                        showDialogOpponentHasLeft = room.status == RoomStatusesEnum.ABANDONED.name && loggedUserIsOnline

                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/


                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        if(room.status == RoomStatusesEnum.GUEST_JOINED.name && loggedUserIsWhoAnswered){
                            showStartingNewGameDialog = true
                            delay(Room.DIALOGS_MILLISECONDS_DELAY)
                            roomViewModel.setOwnerTurn(!room.ownerTurn){} //Isso é para servir tanto para inicio do primeiro jogo quanto um novo jogo. isso garente que o proximo jogador sempre vai ser diferente do último que jogou
                        }
                        showWaitingStartNewGameDialog = room.status == RoomStatusesEnum.GUEST_JOINED.name && !loggedUserIsWhoAnswered
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/


                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        if(room.status == RoomStatusesEnum.VERIFYING_ANSWER.name && loggedUserIsWhoAnswered){
                            showVerifyingAnswerToWhoAnswered = true
                            delay(Room.DIALOGS_MILLISECONDS_DELAY)
                            roomViewModel.verifyAnswer(text, room){
                                showVerifyingAnswerToWhoAnswered = false
                            }
                        }
                        showVerifyingAnswerToWhoWait = room.status == RoomStatusesEnum.VERIFYING_ANSWER.name && !loggedUserIsWhoAnswered
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/


                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        if(room.status == RoomStatusesEnum.GOT_WRONG_ANSWER.name && loggedUserIsWhoAnswered){
                            showGotWrongAnswerToWhoAnswered = true
                            delay(Room.DIALOGS_MILLISECONDS_DELAY)
                            roomViewModel.passTurn(room.ownerTurn, room.cluesShown + 1){
                                showGotWrongAnswerToWhoAnswered = false
                            }
                        }
                        showGotWrongAnswerToWhoWait = room.status == RoomStatusesEnum.GOT_WRONG_ANSWER.name && !loggedUserIsWhoAnswered
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/

                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        if((room.status == RoomStatusesEnum.GOT_CORRECT_ANSWER.name || room.status == RoomStatusesEnum.ROUND_FINISHED_WITH_WINNER.name) && loggedUserIsWhoAnswered){
                            showGotCorrectAnswerToWhoAnswered = true
                            delay(Room.DIALOGS_MILLISECONDS_DELAY)
                            roomViewModel.setOwnerTurn(!room.ownerTurn){
                                roomViewModel.setStatus(RoomStatusesEnum.CHOOSING_WORD_NEXT_ROUND.name){
                                    showGotCorrectAnswerToWhoAnswered = false
                                }
                            }
                        }
                        showGotCorrectAnswerToWhoWait = (room.status == RoomStatusesEnum.GOT_CORRECT_ANSWER.name || room.status == RoomStatusesEnum.ROUND_FINISHED_WITH_WINNER.name) && !loggedUserIsWhoAnswered
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/


                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        if((room.status == RoomStatusesEnum.GOT_NO_ANSWER.name || room.status == RoomStatusesEnum.ROUND_FINISHED_WITHOUT_ANSWER.name) && loggedUserIsWhoAnswered){
                            showGotNoAnswerToWhoAnswered = true
                            delay(Room.DIALOGS_MILLISECONDS_DELAY)
                            roomViewModel.setOwnerTurn(!room.ownerTurn){
                                roomViewModel.setStatus(RoomStatusesEnum.CHOOSING_WORD_NEXT_ROUND.name){
                                    showGotNoAnswerToWhoAnswered = false
                                }
                            }
                        }
                        showGotNoAnswerToWhoWait = (room.status == RoomStatusesEnum.GOT_NO_ANSWER.name || room.status == RoomStatusesEnum.ROUND_FINISHED_WITHOUT_ANSWER.name) && !loggedUserIsWhoAnswered
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/

                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        showGotRoundFinishWithWrongAnswerToWhoAnswered = room.status == RoomStatusesEnum.ROUND_FINISHED_WITHOUT_WINNER.name && loggedUserIsWhoAnswered
                        showGotRoundFinishWithWrongAnswerToWhoWait = room.status == RoomStatusesEnum.ROUND_FINISHED_WITHOUT_WINNER.name && !loggedUserIsWhoAnswered

                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/


                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        showChooseWordDialogNextRound = room.status == RoomStatusesEnum.CHOOSING_WORD_NEXT_ROUND.name && loggedUserIsWhoAnswered
                        showWaitingWordChoiceDialogNextRound = room.status == RoomStatusesEnum.CHOOSING_WORD_NEXT_ROUND.name && !loggedUserIsWhoAnswered
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/


                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        showChooseWordDialogNewGame = room.status == RoomStatusesEnum.CHOOSING_WORD_NEW_GAME.name  && loggedUserIsWhoAnswered
                        showWaitingWordChoiceDialogNewGame = room.status == RoomStatusesEnum.CHOOSING_WORD_NEW_GAME.name && !loggedUserIsWhoAnswered
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                    }


                    //***************************Dialogs Verificando resposta***************************
                    DefaultDialog(
                        showDialog = showStartingNewGameDialog,
                        "Iniciando novo jogo!",
                        backgroundTransparent = false
                    )
                    {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Um novo jogo vai começar após a contagem regressiva!",style = MaterialTheme.typography.bodyMedium)
                            CountdownTimer((Room.DIALOGS_MILLISECONDS_DELAY / 1000).toInt(), Color.Red) {
                                roomViewModel.setStatus(RoomStatusesEnum.CHOOSING_WORD_NEW_GAME.name){
                                    showStartingNewGameDialog = false
                                }
                            }
                        }


                    }

                    DefaultDialog(
                        showDialog = showWaitingStartNewGameDialog,
                        "Iniciando novo jogo!",
                        backgroundTransparent = false
                    )
                    {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Um novo jogo vai começar após a contagem regressiva!",style = MaterialTheme.typography.bodyMedium)
                            CountdownTimer((Room.DIALOGS_MILLISECONDS_DELAY / 1000).toInt(),  Color.Red) {
                                showWaitingStartNewGameDialog = false
                            }
                        }
                    }
                    //*********************************************************************************




                    //***************************Dialogs Verificando resposta***************************
                    DefaultDialog(
                        showDialog = showVerifyingAnswerToWhoAnswered,
                        "Resposta obtida for who answered!",
                        backgroundTransparent = false
                    )
                    {
                        LoadingIndicator("Verificando a resposta obtida...")

                            if(room.status != RoomStatusesEnum.VERIFYING_ANSWER.name){
                                showVerifyingAnswerToWhoAnswered = false
                            }

                    }

                    DefaultDialog(
                        showDialog = showVerifyingAnswerToWhoWait,
                        "Resposta obtida for who wait!",
                        backgroundTransparent = false
                    )
                    {
                        LoadingIndicator("Verificando a resposta obtida...")
                    }
                    //*********************************************************************************


                    //------------------------Dialogs Resposta Errada----------------------------------
                    DefaultDialog(
                        showDialog = showGotWrongAnswerToWhoAnswered,
                        "Resposta Errada!",
                        backgroundTransparent = false
                    )
                    {

                        Column {
                            Text(
                                text = "Você errou a palavra!",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    DefaultDialog(
                        showDialog = showGotWrongAnswerToWhoWait,
                        "Resposta errada!",
                        backgroundTransparent = false
                    )
                    {
                        LoadingIndicator("$whoAnswered errou a palavra. A seguir será sua vez de responder!")
                    }
                    //------------------------------------------------------------------------------------


                    //====================Dialogs Resposta Correta (Final round ou não)=======================
                    DefaultDialog(
                        showDialog = showGotCorrectAnswerToWhoAnswered,
                        "Resposta correta!",
                        backgroundTransparent = false
                    )
                    {

                        Column {
                            Text(
                                text = "Você acertou a palavra.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        LoadingIndicator("Iniciando uma nova rodada! Será a vez de $opponentName responder...")

                    }

                    DefaultDialog(
                        showDialog = showGotCorrectAnswerToWhoWait,
                        "Resposta correta!",
                        backgroundTransparent = false
                    )
                    {

                        Column {
                            Text(
                                text = "$whoAnswered acertou a palavra.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Text(
                                text = "A palavra era ${room.drawnWords[room.chosenWordIndex].name}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LoadingIndicator("A seguir será sua vez de responder!")
                        }
                    }
                    //==============================================================================

                    //------------------------Dialogs Sem Resposta----------------------------------
                    DefaultDialog(
                        showDialog = showGotNoAnswerToWhoAnswered,
                        "Sem resposta!",
                        backgroundTransparent = false
                    )
                    {

                        Column {
                            Text(
                                text = "Você não respondeu a tempo!",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    DefaultDialog(
                        showDialog = showGotNoAnswerToWhoWait,
                        "Sem resposta!",
                        backgroundTransparent = false
                    )
                    {
                        LoadingIndicator("$whoAnswered não respondeu a tempo. A seguir será sua vez de responder!")
                    }
                    //------------------------------------------------------------------------------------


                    //==========================Dialogs Final Round Com Resposta Errada==========================
                    DefaultDialog(
                        showDialog = showGotRoundFinishWithWrongAnswerToWhoAnswered,
                        "Resposta errada!",
                        backgroundTransparent = false
                    )
                    {
                        Column {
                            Text(
                                text = "Você errou a palavra.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        LoadingIndicator("Iniciando uma nova rodada! Será a vez de $opponentName responder...")

                        roomViewModel.setStatus(RoomStatusesEnum.CHOOSING_WORD_NEXT_ROUND.name){}


                    }

                    DefaultDialog(
                        showDialog = showGotRoundFinishWithWrongAnswerToWhoWait,
                        "Resposta errada!",
                        backgroundTransparent = false
                    )
                    {
                        Column {
                            Text(
                                text = "$whoAnswered errou a palavra.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Text(
                                text = "A palavra era ${room.drawnWords[room.chosenWordIndex].name}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LoadingIndicator("A seguir será sua vez de responder!")
                        }
                    }
                    //==============================================================================



                    //¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬Dialogs Escolher Palavra Proximo Round¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬
                    DefaultDialog(
                        showDialog = showChooseWordDialogNextRound,
                        "Escolha um cartão!",
                        backgroundTransparent = false

                    )
                    {
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
                                                    roomViewModel.startNewRound(index, 0, room.round + 1){}
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

                    DefaultDialog(
                        showDialog = showWaitingWordChoiceDialogNextRound,
                        "Palavra sendo escolhida!",
                        backgroundTransparent = false
                    )
                    {
                        LoadingIndicator("Aguarde escolha da palavra!")
                    }

                    //¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬


                    //¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬Dialogs Escolher Palavra Novo Jogo¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬
                    DefaultDialog(
                        showDialog = showChooseWordDialogNewGame,
                        "Escolha um cartão!",
                        backgroundTransparent = false

                    )
                    {
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
                                                    roomViewModel.startNewGame(index, 0, 1){}
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

                    DefaultDialog(
                        showDialog = showWaitingWordChoiceDialogNewGame,
                        "Palavra sendo escolhida!",
                        backgroundTransparent = false
                    )
                    {
                        LoadingIndicator("Aguarde escolha da palavra!")
                    }

                    //¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬


                    DefaultDialog(
                        showDialog = showDialogOpponentHasLeft,
                        "Jogo abandonado!",
                        backgroundTransparent = false
                    )
                    {
                        val whoHasLeft = if (!room.owner.online) room.owner.nickName else room.guest.nickName

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
                                        showDialogOpponentHasLeft = false
                                        navController.popBackStack()
                                    }
                                )
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
                    )
                    {
                        var showProgressLeaving by remember { mutableStateOf(false) }
                        val winner = when {
                            room.owner.score > room.guest.score -> if (roomViewModel.loggedUserId == room.owner.id) "Você venceu!" else "${room.owner.nickName} venceu!"
                            room.guest.score > room.owner.score -> if (roomViewModel.loggedUserId == room.guest.id) "Você venceu!" else "${room.guest.nickName} venceu!"
                            else -> "Jogo empatado!"
                        }
                        Column {
                            Text(
                                text = "O jogo terminou! $winner",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "${if(isLoggedUserOwner) "Você" else room.owner.nickName} : ${room.owner.score} pts X ${if(isLoggedUserGuest) "Você" else room.guest.nickName})}: ${room.guest.score} pts",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            if(showProgressLeaving){
                                LoadingIndicator("Voltando para a tela inicial...")
                            }else{
                                Text(
                                    text = if (bothPlayersAreOnline) "Deseja jogar novamente?" else "Não é possível jogar novamente, pois seu adversário saiu do jogo!",
                                    color = if (bothPlayersAreOnline) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                )
                            }

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
                                        showProgressLeaving = true
                                        roomViewModel.setPlayerOnlineStatus(isLoggedUserOwner, false) {
                                                CoroutineScope(Dispatchers.Main).launch{  //Usar com CoroutineScope(Dispatchers.Main).launch quando nao estiver dentro de uma coroutine como LaunchedEffect. caso contrário, basta usar delay()
                                                delay(2000)
                                                showDialogGameOver = false
                                                navController.popBackStack()
                                            }
                                            }
                                    } else {
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
                                                        showDialogGameOver = false
                                                        navController.popBackStack()
                                                    }
                                                )
                                    }
                                }
                            ) {
                                Text("Sair")

                            }
                        }


                    }

                    DefaultDialog(
                        showDialog = showDialogConfirmExit,
                        "Confirmação!",
                        backgroundTransparent = false
                    )
                    {

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
                                showDialogConfirmExit = false
                            }
                            ) {
                                Text("Cancelar")
                            }

                            Button(onClick = {
                                roomViewModel.setPlayerOnlineStatus(isLoggedUserOwner, false){
                                    roomViewModel.setStatus(RoomStatusesEnum.ABANDONED.name){
                                        navController.popBackStack()
                                        showDialogConfirmExit = false
                                    }
                                }


                            }
                            ) {
                                Text("Sim")
                            }
                        }

                    }


                    when (safeRoom.status) {
                        RoomStatusesEnum.WAITING_GUEST.name -> {
                            LoadingIndicator("Aguardando adversário...")
                        }

                        RoomStatusesEnum.PLAYING.name -> {
                            StartGame(
                                safeRoom,
                                roomViewModel,
                                isLoggedUserOwner,
                                loggedUserName,
                                opponentName,
                                text = text,
                                onTextChange = { text = it }
                            )
                        }

                        RoomStatusesEnum.FINISHED.name,-> {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StartGame(
    safeRoom: Room,
    roomViewModel: RoomViewModel,
    isLoggedUserOwner: Boolean,
    loggedUserName: String,
    opponentName: String,
    text: String,
    onTextChange: (String) -> Unit
)
{

    var showClues by remember { mutableStateOf(false) }



    LaunchedEffect(safeRoom.cluesShown) {
        if (safeRoom.cluesShown > -1) {
            showClues = true
        }
    }

    Text(
        "Rodada: ${safeRoom.round}",
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
                "${safeRoom.owner.score} pts",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if(!isLoggedUserOwner) "Você" else safeRoom.guest.nickName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "${safeRoom.guest.score} pts",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }



    Spacer(modifier = Modifier.padding(8.dp))

    if (showClues) {
        Clues(safeRoom, roomViewModel, loggedUserName,text,onTextChange) { showClues = false }
    } else {
        LoadingIndicator("Aguardando $opponentName escolhe a próxima palavra...")
    }
}

@Composable
fun Clues(
    room: Room,
    roomViewModel: RoomViewModel,
    opponentName: String,
    text: String,
    onTextChange: (String) -> Unit,
    updateShowClues: () -> Unit
)
{
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(8.dp)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "${PointsEnum.HIGHER.points} pts - ${room.drawnWords[room.chosenWordIndex].clues[0]}",
                modifier = Modifier.align(Alignment.Start)
            )


            if (room.cluesShown > 0) {
                Text(
                    text = "${PointsEnum.MEDIAN.points} pts - ${room.drawnWords[room.chosenWordIndex].clues[1]}",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }


            if (room.cluesShown > 1) {
                Text(
                    text = "${PointsEnum.LOWER.points} pts - ${room.drawnWords[room.chosenWordIndex].clues[2]}",
                    modifier = Modifier.align(Alignment.End)
                )
            }

            if (
                (roomViewModel.loggedUserId == room.owner.id && room.ownerTurn) ||
                (roomViewModel.loggedUserId == room.guest.id && !room.ownerTurn)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CountdownTimer(
                        Room.ANSWER_TIMEOUT_SECONDS,
                        Color.Red,
                        fontSize = 12,
                        extraText = " segundos para responder..."
                    ) {
                        roomViewModel.verifyTimeOut(room) {}
                    }
                }

                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    label = { Text("Digite algo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        updateShowClues()
                        roomViewModel.setStatus(RoomStatusesEnum.VERIFYING_ANSWER.name){}
                              },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enviar")
                }

            }else{
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CountdownTimer(
                        Room.ANSWER_TIMEOUT_SECONDS,
                        Color.Red,
                        fontSize = 12,
                        extraText = " segundos para $opponentName responder..."
                    ) {}
                }
            }

        }
    }
