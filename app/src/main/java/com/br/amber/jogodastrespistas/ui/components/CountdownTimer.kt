package com.br.amber.jogodastrespistas.ui.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CountdownTimer(
    start: Int = 5,
    counterColor: Color = Color.Black,
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
            text = "$timeLeft",
            fontSize = 48.sp,
            modifier = Modifier
                .padding(16.dp),
            color = counterColor
        )
    }
}
