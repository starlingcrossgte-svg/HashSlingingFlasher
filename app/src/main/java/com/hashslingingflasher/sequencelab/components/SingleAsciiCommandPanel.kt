package com.hashslingingflasher.sequencelab.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
    commandSlots: List<String>,
    isRunning: Boolean,
    onCommandSlotChange: (Int, String) -> Unit,
    onSendCommandSlot: (Int) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(6) { index ->
            val commandText = commandSlots.getOrElse(index) { "" }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = SlotGray)
            ) {
                BasicTextField(
                    value = commandText,
                    onValueChange = { onCommandSlotChange(index, it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    singleLine = true,
                    enabled = !isRunning,
                    textStyle = TextStyle(
                        color = TextDark,
                        fontWeight = FontWeight.SemiBold
                    ),
                    decorationBox = { innerTextField ->
                        if (commandText.isBlank()) {
                            Text(
                                text = "Command ${index + 1}",
                                color = TextMuted
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Button(
                onClick = {
                    val trimmed = commandText.trim()
                    if (trimmed.isNotEmpty()) {
                        onSendCommandSlot(index)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                enabled = !isRunning && commandText.trim().isNotEmpty(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActiveBlue,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Text(
                    text = "Send Command ${index + 1}",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
