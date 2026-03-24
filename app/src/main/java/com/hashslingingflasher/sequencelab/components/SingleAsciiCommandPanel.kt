package com.hashslingingflasher.sequencelab.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val SlotGray = Color(0xFFE6E8ED)
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

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = SlotGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (commandText.isBlank()) {
                    Text(
                        text = "Type single ASCII command",
                        color = TextMuted
                    )
                }

                BasicTextField(
                    value = commandText,
                    onValueChange = { commandText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextDark,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Button(
            onClick = {
                val trimmed = commandText.trim()
                if (trimmed.isNotEmpty()) {
                    onSendCommand(trimmed)
                    commandText = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            enabled = !isRunning,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ActiveBlue,
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Text(
                text = "Send Command",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
