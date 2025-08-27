package com.br.amber.jogodastrespistas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.br.amber.jogodastrespistas.ui.theme.DefaultButtonColor
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
        Spacer(modifier = Modifier.height(30.dp))
        content()
    }
}

@Composable
fun CenteredBodyText(
    text: String,
    color: Color = Color.White,
    padding: Int = 0) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center, //Só funciona se tiver Modifer.fillMaxWidth()
        color = color,
        modifier = Modifier
            .padding(padding.dp)
            .fillMaxWidth()
    )
}

@Composable
fun CenteredTitleText(
    text: String,
    color: Color = Color.White,
    padding: Int = 0) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,//Só funciona se tiver Modifer.fillMaxWidth()
        color = color,
        modifier = Modifier
            .padding(padding.dp)
            .fillMaxWidth()
    )

}

@Composable
fun LeftAlignedText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .padding(bottom = 8.dp)
    )
}

@Composable
fun DefaultButton(
    text: String,
    fillMaxWidth: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .background(DefaultButtonColor, shape = RoundedCornerShape(10.dp))
            .height(60.dp)
            .then(
                if (fillMaxWidth) Modifier.fillMaxWidth()
                else Modifier.widthIn(min = 50.dp)
            )
    ) {
        Text(text, color = Color.White)
    }
}