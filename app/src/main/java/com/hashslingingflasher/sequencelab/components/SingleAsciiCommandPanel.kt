package com.hashslingingflasher.sequencelab.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val PanelWhite = Color(0xFFF8F8FA)
private val BorderGray = Color(0xFFD4D7DE)
private val ActiveBlue = Color(0xFF2F6FE4)
private val TextDark = Color(0xFF171A20)
private val TextMuted = Color(0xFF6B7280)

@Composable
fun SingleAsciiCommandPanel(
    modifier: Modifier = Modifier,
    isRunning: Boolean,
    onSendCommand: (String) -> Unit
) {
    var commandText by remember { mutableStateOf("") }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = PanelWhite),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Single ASCII Command",
                color = TextDark,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Send one command immediately without using the sequence lane.",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedTextField(
                value = commandText,
                onValueChange = { commandText = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("ASCII command")
                },
                placeholder = {
                    Text("ATZ")
                }
            )

            Button(
                onClick = {
                    val trimmed = commandText.trim()
                    if (trimmed.isNotEmpty()) {
                        onSendCommand(trimmed)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isRunning,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActiveBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Send Command",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
