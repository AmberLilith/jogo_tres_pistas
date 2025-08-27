package com.br.amber.jogodastrespistas.ui.components

import android.annotation.SuppressLint
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@SuppressLint("DefaultLocale")
@Composable
fun CountdownTimer(
    start: Int = 5,
    counterColor: Color = Color.Black,
    fontSize: Int = 48,
    extraText: String = "",
    onFinish: () -> Unit = {}
) {
    var timeLeft by remember { mutableIntStateOf(start) }
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(start) {
        timeLeft = start
        visible = true
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        visible = false
        onFinish()
    }

    // converte segundos -> hh:mm:ss
    val hours = timeLeft / 3600
    val minutes = (timeLeft % 3600) / 60
    val seconds = timeLeft % 60
    val formatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    if (visible) {
        Text(
            text = "$timeLeft $extraText",
            fontSize = fontSize.sp,
            color = counterColor
        )
    }
}


/*@Composable
fun CountdownTimer(
    start: Int = 5,
    counterColor: Color = Color.Black,
    fontSize: Int = 48,
    extraText: String = "",
    onFinish: () -> Unit = {}
) {
    var timeLeft by remember { mutableIntStateOf(start) }
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(start) {
        timeLeft = start
        visible = true
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        visible = false
        onFinish()
    }

    if (visible) {
        Text(
            text = "$timeLeft $extraText",
            fontSize = fontSize.sp,
            *//*modifier = Modifier
                .padding(16.dp),*//*
            color = counterColor
        )
    }
}*/
