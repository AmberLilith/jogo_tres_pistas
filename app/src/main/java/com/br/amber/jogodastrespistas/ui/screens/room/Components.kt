package com.br.amber.jogodastrespistas.ui.screens.room

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.br.amber.jogodastrespistas.enums.PointsEnum
import com.br.amber.jogodastrespistas.models.Room
import com.br.amber.jogodastrespistas.enums.RoomStatusesEnum
import com.br.amber.jogodastrespistas.models.Word
import com.br.amber.jogodastrespistas.ui.components.CenteredBodyText
import com.br.amber.jogodastrespistas.ui.components.CenteredTitleText
import com.br.amber.jogodastrespistas.ui.components.CountdownTimer
import com.br.amber.jogodastrespistas.ui.components.dialogs.DefaultDialog
import com.br.amber.jogodastrespistas.ui.components.indicators.LoadingIndicator
import com.br.amber.jogodastrespistas.ui.theme.DialogTitle
import com.br.amber.jogodastrespistas.ui.theme.ScoreCardFirstColor
import com.br.amber.jogodastrespistas.ui.theme.ScoreCardSecondColor
import com.br.amber.jogodastrespistas.ui.theme.WordCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoomContent(
    room: Room?,
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
                    .padding(16.dp)
            )
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            )
            {
                var showDialogConfirmExit by remember { mutableStateOf(false) }
                var text by remember { mutableStateOf("") }





                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Grupo da ESQUERDA
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {}//inserir se precisar

                    // Grupo da DIREITA
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        /*IconButton(onClick = {}) {
                            Icon(Icons.Default.Favorite, contentDescription = "Favorito")
                        }*/
                        Button(onClick = {
                            showDialogConfirmExit = true
                        }) {
                            Text("Sair")
                        }
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
                    val isLoggedUserTurn = (room.owner.id == roomViewModel.loggedUserId && room.ownerTurn) || (room.guest.id == roomViewModel.loggedUserId && !room.ownerTurn)
                    val loggedUserIsOnline = (room.owner.id == roomViewModel.loggedUserId && room.owner.online) || (room.guest.id == roomViewModel.loggedUserId && room.guest.online)

                    val whoAnswered = if (isLoggedUserTurn) loggedUserName else opponentName

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

                    var showGotNoAnswerToWhoseIsTurn by remember { mutableStateOf(false) }
                    var showGotNoAnswerToWhoWait by remember { mutableStateOf(false) }

                    var showRoundFinishedWithoutAnswerToWhoseIsTurn by remember { mutableStateOf(false) }
                    var showRoundFinishedWithoutAnswerToWhoWait by remember { mutableStateOf(false) }

                    var showGotRoundFinishWithWrongAnswerToWhoAnswered by remember { mutableStateOf(false) }
                    var showGotRoundFinishWithWrongAnswerToWhoWait by remember { mutableStateOf(false) }

                    var showDialogPlayAgain by remember { mutableStateOf(false) }
                    var showDialogWaitingPlayAgainAcceptance by remember { mutableStateOf(false) }

                    LaunchedEffect(room.owner.online, room.guest.online) {
                        showDialogOpponentHasLeft = (isLoggedUserOwner && !room.guest.online && room.guest.id.isNotBlank()) || (isLoggedUserGuest && !room.owner.online)
                    }


                    LaunchedEffect(room.status) {
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        if((room.status == RoomStatusesEnum.GUEST_JOINED.name || room.status == RoomStatusesEnum.STARTING_NEW_GAME.name) && isLoggedUserTurn){
                            showStartingNewGameDialog = true
                            delay(Room.DIALOGS_MILLISECONDS_DELAY)
                            roomViewModel.setOwnerTurn(!room.ownerTurn){} //Isso é para servir tanto para inicio do primeiro jogo quanto um novo jogo. isso garente que o proximo jogador sempre vai ser diferente do último que jogou
                        }
                        showWaitingStartNewGameDialog = (room.status == RoomStatusesEnum.GUEST_JOINED.name || room.status == RoomStatusesEnum.STARTING_NEW_GAME.name) && !isLoggedUserTurn
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/


                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        if(room.status == RoomStatusesEnum.VERIFYING_ANSWER.name && isLoggedUserTurn){
                            showVerifyingAnswerToWhoAnswered = true
                            delay(Room.DIALOGS_MILLISECONDS_DELAY)
                            roomViewModel.verifyAnswer(text, room){
                                showVerifyingAnswerToWhoAnswered = false
                            }
                        }
                        showVerifyingAnswerToWhoWait = room.status == RoomStatusesEnum.VERIFYING_ANSWER.name && !isLoggedUserTurn
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/


                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        if(room.status == RoomStatusesEnum.GOT_WRONG_ANSWER.name && isLoggedUserTurn){
                            showGotWrongAnswerToWhoAnswered = true
                            delay(Room.DIALOGS_MILLISECONDS_DELAY)
                            roomViewModel.passTurn(room.ownerTurn, room.cluesShown + 1){
                                showGotWrongAnswerToWhoAnswered = false
                            }
                        }
                        showGotWrongAnswerToWhoWait = room.status == RoomStatusesEnum.GOT_WRONG_ANSWER.name && !isLoggedUserTurn
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/

                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        if((room.status == RoomStatusesEnum.GOT_CORRECT_ANSWER.name || room.status == RoomStatusesEnum.ROUND_FINISHED_WITH_WINNER.name) && isLoggedUserTurn){
                            showGotCorrectAnswerToWhoAnswered = true
                            delay(Room.DIALOGS_MILLISECONDS_DELAY)
                            roomViewModel.setOwnerTurn(!room.ownerTurn){
                                roomViewModel.setStatus(RoomStatusesEnum.CHOOSING_WORD_NEXT_ROUND.name){
                                    showGotCorrectAnswerToWhoAnswered = false
                                }
                            }
                        }
                        showGotCorrectAnswerToWhoWait = (room.status == RoomStatusesEnum.GOT_CORRECT_ANSWER.name || room.status == RoomStatusesEnum.ROUND_FINISHED_WITH_WINNER.name) && !isLoggedUserTurn
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/


                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        if((room.status == RoomStatusesEnum.GOT_NO_ANSWER_OWNER.name) && isLoggedUserTurn){
                            showGotNoAnswerToWhoseIsTurn = true
                            delay(Room.DIALOGS_MILLISECONDS_DELAY)
                            roomViewModel.passTurn(room.ownerTurn, room.cluesShown + 1){
                                showGotNoAnswerToWhoseIsTurn = false
                            }
                        }
                        /*TODO verificar essa regra pois em alguns momentos ownerTurn está modudando e
                        enquanto status permanece == GOT_NO_ANSWER_OWNER, essa tela acaba aparecendo para o outro jogador mesmo que por alguns milissegundos*/
                        showGotNoAnswerToWhoWait = (room.status == RoomStatusesEnum.GOT_NO_ANSWER_OWNER.name) && !isLoggedUserTurn
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/


                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        if((room.status == RoomStatusesEnum.ROUND_FINISHED_WITHOUT_ANSWER.name) && isLoggedUserTurn){
                            showRoundFinishedWithoutAnswerToWhoseIsTurn = true
                            delay(Room.DIALOGS_MILLISECONDS_DELAY)
                            roomViewModel.setOwnerTurn(!room.ownerTurn){
                                roomViewModel.setStatus(RoomStatusesEnum.CHOOSING_WORD_NEXT_ROUND.name){
                                    showRoundFinishedWithoutAnswerToWhoseIsTurn = false
                                }
                            }
                        }
                        /*TODO verificar essa regra pois em alguns momentos ownerTurn está modudando e
                        enquanto status permanece == GOT_NO_ANSWER_OWNER, essa tela acaba aparecendo para o outro jogador mesmo que por alguns milissegundos*/
                        showRoundFinishedWithoutAnswerToWhoWait = (room.status == RoomStatusesEnum.ROUND_FINISHED_WITHOUT_ANSWER.name) && !isLoggedUserTurn
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/

                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        showGotRoundFinishWithWrongAnswerToWhoAnswered = room.status == RoomStatusesEnum.ROUND_FINISHED_WITHOUT_WINNER.name && isLoggedUserTurn
                        showGotRoundFinishWithWrongAnswerToWhoWait = room.status == RoomStatusesEnum.ROUND_FINISHED_WITHOUT_WINNER.name && !isLoggedUserTurn

                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/


                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        showChooseWordDialogNextRound = room.status == RoomStatusesEnum.CHOOSING_WORD_NEXT_ROUND.name && isLoggedUserTurn
                        showWaitingWordChoiceDialogNextRound = room.status == RoomStatusesEnum.CHOOSING_WORD_NEXT_ROUND.name && !isLoggedUserTurn
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/


                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        if(room.status == RoomStatusesEnum.CHOOSING_WORD_NEW_GAME.name  && isLoggedUserTurn) {
                            showChooseWordDialogNewGame = true
                        }else{
                            showChooseWordDialogNewGame = false
                            showDialogWaitingPlayAgainAcceptance = false
                            showDialogPlayAgain = false

                        }

                        if(room.status == RoomStatusesEnum.CHOOSING_WORD_NEW_GAME.name && !isLoggedUserTurn){
                            showWaitingWordChoiceDialogNewGame = true
                        }else{
                            showWaitingWordChoiceDialogNewGame = false
                            showDialogWaitingPlayAgainAcceptance = false
                            showDialogPlayAgain = false
                        }
                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/


                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/
                        if(room.status == RoomStatusesEnum.OWNER_WANTS_PLAY_AGAIN.name){
                            if(isLoggedUserOwner){
                                showDialogWaitingPlayAgainAcceptance = true
                            }else{
                                showDialogPlayAgain = true
                            }
                        }

                        if(room.status == RoomStatusesEnum.GUEST_WANTS_PLAY_AGAIN.name){
                            if(isLoggedUserGuest){
                                showDialogWaitingPlayAgainAcceptance = true
                            }else{
                                showDialogPlayAgain = true
                            }
                        }


                        /*----------------------------------------------------------------------------------------------------------------------------------------------------------*/

                        showDialogGameOver = room.status == RoomStatusesEnum.FINISHED.name && loggedUserIsOnline
                    }


                    //***************************Dialogs Novo Jogo***************************
                    DefaultDialog(
                        showDialog = showStartingNewGameDialog,
                        "Iniciando novo jogo!"
                    )
                    {
                        CenteredBodyText("Um novo jogo vai começar após a contagem regressiva!")
                        CountdownTimer((Room.DIALOGS_MILLISECONDS_DELAY / 1000).toInt(), Color.Red) {
                            roomViewModel.setStatus(RoomStatusesEnum.CHOOSING_WORD_NEW_GAME.name){
                                showStartingNewGameDialog = false
                            }
                        }
                    }

                    DefaultDialog(
                        showDialog = showWaitingStartNewGameDialog,
                        "Iniciando novo jogo!"
                    )
                    {
                        CenteredBodyText("Um novo jogo vai começar após a contagem regressiva!")
                        CountdownTimer((Room.DIALOGS_MILLISECONDS_DELAY / 1000).toInt(),  Color.Red) {
                            showWaitingStartNewGameDialog = false
                        }
                    }
                    //*********************************************************************************




                    //***************************Dialogs Verificando resposta***************************
                    DefaultDialog(
                        showDialog = showVerifyingAnswerToWhoAnswered,
                        "Resposta obtida for who answered!"
                    )
                    {
                        LoadingIndicator("Verificando a resposta obtida...")

                            if(room.status != RoomStatusesEnum.VERIFYING_ANSWER.name){
                                showVerifyingAnswerToWhoAnswered = false
                            }

                    }

                    DefaultDialog(
                        showDialog = showVerifyingAnswerToWhoWait,
                        "Resposta obtida for who wait!"
                    )
                    {
                        LoadingIndicator("Verificando a resposta obtida...")
                    }
                    //*********************************************************************************


                    //------------------------Dialogs Resposta Errada----------------------------------
                    DefaultDialog(
                        showDialog = showGotWrongAnswerToWhoAnswered,
                        "Resposta Errada!"
                    )
                    {

                        CenteredBodyText("Você errou a palavra!")
                    }

                    DefaultDialog(
                        showDialog = showGotWrongAnswerToWhoWait,
                        "Resposta errada!"
                    )
                    {
                        LoadingIndicator("$whoAnswered errou a palavra. A seguir será sua vez de responder!")
                    }
                    //------------------------------------------------------------------------------------


                    //====================Dialogs Resposta Correta (Final round ou não)=======================
                    DefaultDialog(
                        showDialog = showGotCorrectAnswerToWhoAnswered,
                        "Resposta correta!"
                    )
                    {

                        CenteredBodyText("Você acertou a palavra.")

                        LoadingIndicator("Iniciando uma nova rodada! Será a vez de $opponentName responder...")

                    }

                    DefaultDialog(
                        showDialog = showGotCorrectAnswerToWhoWait,
                        "Resposta correta!"
                    )
                    {

                        CenteredBodyText("$whoAnswered acertou a palavra.")

                        CenteredBodyText("A palavra era ${room.drawnWords[room.chosenWordIndex].name}")

                        LoadingIndicator("A seguir será sua vez de responder!")
                    }
                    //==============================================================================

                    //------------------------Dialogs Sem Resposta----------------------------------
                    DefaultDialog(
                        showDialog = showGotNoAnswerToWhoseIsTurn || showRoundFinishedWithoutAnswerToWhoseIsTurn,
                        "Sem resposta!"
                    )
                    {

                        CenteredBodyText("Você não respondeu a tempo!")
                        LoadingIndicator("Passando a vez. Aguarde...")
                    }

                    DefaultDialog(
                        showDialog = showGotNoAnswerToWhoWait || showRoundFinishedWithoutAnswerToWhoWait,
                        "Sem resposta!"
                    )
                    {
                        LoadingIndicator("$whoAnswered não respondeu a tempo. A seguir será sua vez de responder!")
                    }
                    //------------------------------------------------------------------------------------


                    //==========================Dialogs Final Round Com Resposta Errada==========================
                    DefaultDialog(
                        showDialog = showGotRoundFinishWithWrongAnswerToWhoAnswered,
                        "Resposta errada!"
                    )
                    {
                        CenteredBodyText("Você errou a palavra.")

                        LoadingIndicator("Iniciando uma nova rodada! Será a vez de $opponentName responder...")

                        roomViewModel.setStatus(RoomStatusesEnum.CHOOSING_WORD_NEXT_ROUND.name){}


                    }

                    DefaultDialog(
                        showDialog = showGotRoundFinishWithWrongAnswerToWhoWait,
                        "Resposta errada!"
                    )
                    {
                        CenteredBodyText("$whoAnswered errou a palavra.")

                        CenteredBodyText("A palavra era ${room.drawnWords[room.chosenWordIndex].name}")

                        LoadingIndicator("A seguir será sua vez de responder!")
                    }
                    //==============================================================================



                    //¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬Dialogs Escolher Palavra Proximo Round¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬
                    DefaultDialog(
                        showDialog = showChooseWordDialogNextRound,
                        "Escolha um cartão!"

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
                                    WordCard(word, index, room.round, roomViewModel) {
                                        showProgressbar = true
                                    }
                                }
                            }
                        }else{
                            LoadingIndicator("Carregando dicas...")
                        }
                    }

                    DefaultDialog(
                        showDialog = showWaitingWordChoiceDialogNextRound,
                        "Palavra sendo escolhida!"
                    )
                    {
                        LoadingIndicator("Aguarde escolha da palavra!")
                    }

                    //¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬


                    //¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬Dialogs Escolher Palavra Novo Jogo¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬
                    DefaultDialog(
                        showDialog = showChooseWordDialogNewGame,
                        "Escolha um cartão!"

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
                                    WordCard(word, index, room.round, roomViewModel) {
                                        showProgressbar = true
                                    }
                                }
                            }
                        }else{
                            LoadingIndicator("Carregando dicas...")
                        }
                    }

                    DefaultDialog(
                        showDialog = showWaitingWordChoiceDialogNewGame,
                        "Palavra sendo escolhida!"
                    )
                    {
                        LoadingIndicator("Aguarde escolha da palavra!")
                    }

                    //¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬

                    //+++++++++++++++++++++++++Dialogs Jogar Novamente+++++++++++++++++++++++++++++++++++
                    DefaultDialog(
                        showDialog = showDialogPlayAgain,
                        "Jogar Novamente!"
                    )
                    {
                        var progressBarText = ""
                        var showProgressWaitingAcceptance by remember { mutableStateOf(false) }

                        Column {

                            if(showProgressWaitingAcceptance){
                                LoadingIndicator(progressBarText)
                            }else{
                                Text(
                                    text = if (bothPlayersAreOnline) "Seu adversário deseja jogar novamente!" else "Não é possível jogar novamente, pois seu adversário saiu do jogo!",
                                    color = if (bothPlayersAreOnline) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                                )
                            }

                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    progressBarText = "Gerando novo jogo. Aguarde!"
                                    showProgressWaitingAcceptance = true
                                    roomViewModel.getRandomSetOfWords(room.usedWords){ newDrawnWords ->
                                        val newUsedWords = newDrawnWords.map{ word -> word.name}
                                        roomViewModel.appendUsedWords(room.usedWords,newUsedWords){
                                            roomViewModel.setDrawnWords(newDrawnWords){
                                                roomViewModel.setRound(0){
                                                    roomViewModel.setStatus(RoomStatusesEnum.STARTING_NEW_GAME.name){
                                                        showDialogPlayAgain = false
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                enabled = bothPlayersAreOnline
                            ) {
                                Text("Aceitar")
                            }

                            Button(
                                onClick = {
                                    progressBarText  = "Saindo da sala. Aguarde!"
                                    showProgressWaitingAcceptance = true
                                    if (bothPlayersAreOnline) {
                                        roomViewModel.setPlayerOnlineStatus(isLoggedUserOwner, false) {
                                            CoroutineScope(Dispatchers.Main).launch{  //Usar com CoroutineScope(Dispatchers.Main).launch quando nao estiver dentro de uma coroutine como LaunchedEffect. caso contrário, basta usar delay()
                                                delay(2000)
                                                showDialogPlayAgain = false
                                                navController.popBackStack()
                                            }
                                        }
                                    } else {
                                        roomViewModel.deleteRoom(
                                            onSuccess = {
                                                showDialogPlayAgain = false
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
                        showDialog = showDialogWaitingPlayAgainAcceptance,
                        "Jogar Novamente!"
                    )
                    {
                        LoadingIndicator(if(bothPlayersAreOnline) "Aguardando seu adiversário aceitar novo jogo..." else "Saindo da sala. Aguarde...")

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    if (bothPlayersAreOnline) {
                                        roomViewModel.setPlayerOnlineStatus(isLoggedUserOwner, false) {
                                            CoroutineScope(Dispatchers.Main).launch{  //Usar com CoroutineScope(Dispatchers.Main).launch quando nao estiver dentro de uma coroutine como LaunchedEffect. caso contrário, basta usar delay()
                                                delay(2000)
                                                showDialogWaitingPlayAgainAcceptance = false
                                                navController.popBackStack()
                                            }
                                        }
                                    } else {
                                        roomViewModel.deleteRoom(
                                            onSuccess = {
                                                showDialogWaitingPlayAgainAcceptance = false
                                                navController.popBackStack()
                                            },
                                            onFailure = { error ->
                                                Log.e(
                                                    "Firebase",
                                                    "Erro ao deletar a sala: ${error.message}"
                                                )
                                                showDialogWaitingPlayAgainAcceptance = false
                                                navController.popBackStack()
                                            }
                                        )
                                    }
                                },
                                enabled = bothPlayersAreOnline
                            ) {
                                Text("Sair")

                            }
                        }
                    }
                    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


                    DefaultDialog(
                        showDialog = showDialogOpponentHasLeft,
                        "Jogo abandonado!"
                    )
                    {
                        val whoHasLeft = if (!room.owner.online) room.owner.nickName else room.guest.nickName

                        CenteredBodyText("$whoHasLeft saiu do jogo!")
                        CenteredBodyText(text = "Clique em sair para voltar para a tela inicial")

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

                    //+++++++++++++++++++++++++Dialog Fim do Jogo+++++++++++++++++++++++++++++++++++++++
                    DefaultDialog(
                        showDialog = showDialogGameOver,
                        "Jogo Finalizado!"
                    )
                    {
                        var showProgressLeaving by remember { mutableStateOf(false) }
                        val winner = when {
                            room.owner.score > room.guest.score -> if (roomViewModel.loggedUserId == room.owner.id) "Você venceu!" else "${room.owner.nickName} venceu!"
                            room.guest.score > room.owner.score -> if (roomViewModel.loggedUserId == room.guest.id) "Você venceu!" else "${room.guest.nickName} venceu!"
                            else -> "Jogo empatado!"
                        }
                        CenteredBodyText("O jogo terminou! $winner")

                        CenteredBodyText("${if(isLoggedUserOwner) "Você linda maravilhosa do mundo" else room.owner.nickName} : ${room.owner.score} pts X ${if(isLoggedUserGuest) "Você" else room.guest.nickName}: ${room.guest.score} pts")

                        if(showProgressLeaving){
                            LoadingIndicator("Voltando para a tela inicial...")
                        }else{
                            Text(
                                text = if (bothPlayersAreOnline) "Deseja jogar novamente?" else "Não é possível jogar novamente, pois seu adversário saiu do jogo!",
                                color = if (bothPlayersAreOnline) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    val status = if(isLoggedUserOwner) RoomStatusesEnum.OWNER_WANTS_PLAY_AGAIN.name else RoomStatusesEnum.GUEST_WANTS_PLAY_AGAIN.name
                                    roomViewModel.setStatus(status){
                                        showDialogGameOver = false
                                    }
                                },
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
                    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

                    DefaultDialog(
                        showDialog = showDialogConfirmExit,
                        "Confirmação!"
                    )
                    {

                        Column {
                            CenteredBodyText("Tem certeza que deseja sair do jogo?")

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

                            Button(onClick = { //TODO excluir sala se owner ainda estiver aguardando guest
                                roomViewModel.setPlayerOnlineStatus(isLoggedUserOwner, false){
                                    navController.popBackStack()
                                    showDialogConfirmExit = false
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
                            showDialogWaitingPlayAgainAcceptance = false //TODO procurar melhor lugar para alterar essas variaveis
                            showDialogPlayAgain = false
                            ShowGame(
                                safeRoom,
                                roomViewModel,
                                isLoggedUserOwner,
                                loggedUserName,
                                text = text,
                                onTextChange = { text = it }
                            )
                        }
                    }
                }

                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShowGame(
    safeRoom: Room,
    roomViewModel: RoomViewModel,
    isLoggedUserOwner: Boolean,
    loggedUserName: String,
    text: String,
    onTextChange: (String) -> Unit
)
{
    Column {
        CenteredTitleText("Rodada: ${safeRoom.round}")
        ScoreBoard(isLoggedUserOwner, safeRoom)
        Spacer(modifier = Modifier.padding(50.dp))
        Clues(safeRoom, roomViewModel, loggedUserName,text,onTextChange)
    }



}

@Composable
fun Clues(
    room: Room,
    roomViewModel: RoomViewModel,
    opponentName: String,
    text: String,
    onTextChange: (String) -> Unit
)
{
    Column(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(8.dp))
            .padding(4.dp), // ← Padding DEPOIS da borda
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){

            if(room.cluesShown > -1){
                PointsAndClue(PointsEnum.HIGHER.points.toString(), room.drawnWords[room.chosenWordIndex].clues[0], Arrangement.Start)
                /*Text(
                    text = "${PointsEnum.HIGHER.points} pts - ${room.drawnWords[room.chosenWordIndex].clues[0]}",
                    modifier = Modifier.align(Alignment.Start)
                )*/


                if (room.cluesShown > 0) {
                    PointsAndClue(PointsEnum.MEDIAN.points.toString(), room.drawnWords[room.chosenWordIndex].clues[1],
                        Arrangement.Center)
                    /*Text(
                        text = "${PointsEnum.MEDIAN.points} pts - ${room.drawnWords[room.chosenWordIndex].clues[1]}",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )*/
                }


                if (room.cluesShown > 1) {
                    PointsAndClue(PointsEnum.LOWER.points.toString(), room.drawnWords[room.chosenWordIndex].clues[2],
                        Arrangement.End)
                    /*Text(
                        text = "${PointsEnum.LOWER.points} pts - ${room.drawnWords[room.chosenWordIndex].clues[2]}",
                        modifier = Modifier.align(Alignment.End)
                    )*/
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
    }

@Composable
fun PointsAndClue(points: String, clue: String, arrangement: Arrangement.Horizontal){
    Row(
        horizontalArrangement = arrangement,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = points,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(Color(red = 80, green = 90, blue = 248, alpha = 255))
                .widthIn(min = 50.dp)
                .border(2.dp, Color(45, 135, 241, 255))
                .padding(12.dp)
        )

        Text(
            text = clue,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .background(Color(red = 91, green = 31, blue = 152))
                .border(1.dp, Color(45, 135, 241, 255))
                .padding(12.dp)
        )
    }
}


@Composable
fun WordCard(word: Word, index: Int, round: Int, roomViewModel: RoomViewModel, callback: () -> Unit){
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(150.dp)
            .height(100.dp)
            .background(Color.Transparent)
            .padding(2.dp)
            .shadow(8.dp, RoundedCornerShape(8.dp))
            .clickable {
                if (!word.used) {
                    callback()
                    roomViewModel.startNewRound(index, 0, round + 1) {}
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (!word.used) WordCard else Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            if (word.used) {
                Text(
                    text = word.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,)
            }
        }
    }
}

@Composable
fun ScoreBoard(isLoggedUserOwner: Boolean, room: Room){
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // sombra
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp) // opcional, só pra afastar do resto da tela
    )
    {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ScoreCardFirstColor,
                            ScoreCardSecondColor
                        ), // Cores do gradiente
                        start = Offset(0.5f, 0f),    // 🔥 Topo center (x=50%, y=0%)
                        end = Offset(0.5f, 1f)       // Bottom center (x=50%, y=100%)
                    ))
                .fillMaxWidth()
                .height(100.dp)
                .padding(12.dp)// espaço interno dentro do Card
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if(isLoggedUserOwner) "Você" else room.owner.nickName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "${room.owner.score} pts",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if(!isLoggedUserOwner) "Você" else room.guest.nickName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "${room.guest.score} pts",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37)
                )
            }
        }
    }
}
