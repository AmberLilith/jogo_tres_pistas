package com.br.amber.jogodastrespistas.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalLayoutApi::class)
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
                    .background(Color(0x0D000000))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(color = Color.White)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .defaultMinSize(minWidth = 200.dp)
                ) {
                    Text(title, style = MaterialTheme.typography.titleLarge, color = if (backgroundTransparent) Color.Transparent else Color.White)
                    Spacer(modifier = Modifier.height(8.dp))

                    content()

                    Spacer(modifier = Modifier.height(16.dp))
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
