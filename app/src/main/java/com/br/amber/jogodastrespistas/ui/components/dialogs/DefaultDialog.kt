package com.br.amber.jogodastrespistas.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.br.amber.jogodastrespistas.ui.theme.DialogBackGround
import com.br.amber.jogodastrespistas.ui.theme.DialogTitle

@Composable
fun DefaultDialog(
    showDialog: Boolean,
    title: String,
    backgroundTransparent: Boolean = false,
    content: @Composable () -> Unit
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            color = DialogBackGround,
                            shape = RoundedCornerShape(16.dp))
                        .verticalScroll(rememberScrollState())
                        .defaultMinSize(minHeight = 400.dp)
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                DialogTitle,
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) )
                            .fillMaxWidth()
                            .padding(16.dp, 16.dp, 16.dp, 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(modifier = Modifier.padding(16.dp)) {
                        content()
                    }
                }
            }
        }
    }
}

/*Como Chamar:
var showDefaultDialog by remember { mutableStateOf(false) }

            Button(
                onClick = { showDefaultDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Testar dialog")
            }

            DefaultDialog(
                showDialog = showDefaultDialog,
                "Teste mostrando Dialog Transparente",
                backgroundTransparent = true,
                content  = {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(10) {
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(50.dp)
                                    .padding(4.dp)
                                    .background(Color.Blue)
                            )
                        }
                    }
                }

            )
 */
