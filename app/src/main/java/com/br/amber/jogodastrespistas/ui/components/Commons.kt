package com.br.amber.jogodastrespistas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.br.amber.jogodastrespistas.ui.screens.room.RoomContent
import com.br.amber.jogodastrespistas.ui.theme.ScreenBackGround

@Composable
fun DefaultScreen(content: @Composable () -> Unit){
    Column(
        modifier = Modifier
            .background(ScreenBackGround)
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //pra que o conteudo não seja sobreposto pela camera quando ela fizer parte da tela quando a tela estiver configura para ser fullscreen
        Spacer(modifier = Modifier.height(25.dp))
        content()
    }
}

@Composable
fun CenteredBodyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center, //Só funciona se tiver Modifer.fillMaxWidth()
        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
    )
}

@Composable
fun CenteredTitleText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center, //Só funciona se tiver Modifer.fillMaxWidth()
        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
    )
}

@Composable
fun LeftAlignedText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Start,
        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
    )
}