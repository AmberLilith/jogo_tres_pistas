package com.br.amber.jogodastrespistas.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(){
    Text(
        text = "Bem-vindo ao Jogo das Três Pistas!",
        modifier = Modifier.padding(16.dp)
    )
}