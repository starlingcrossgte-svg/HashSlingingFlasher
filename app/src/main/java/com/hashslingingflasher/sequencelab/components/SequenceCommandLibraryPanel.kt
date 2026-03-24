package com.hashslingingflasher.sequencelab.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hashslingingflasher.sequencelab.ObdLinkAsciiPreset

private val PanelWhite = Color(0xFFF8F8FA)
private val SlotGray = Color(0xFFE6E8ED)
private val BorderGray = Color(0xFFD4D7DE)
private val TextDark = Color(0xFF171A20)
private val TextMuted = Color(0xFF6B7280)

@Composable
fun SequenceCommandLibraryPanel(
    modifier: Modifier = Modifier,
    asciiPresets: List<ObdLinkAsciiPreset>,
    onAddAsciiPreset: (ObdLinkAsciiPreset) -> Unit,
    onAddRawStep: () -> Unit,
    onAddPauseStep: () -> Unit
) {
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
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Search commands...",
                        color = TextMuted
                    )
                }
            )

            Text(
                text = "ASCII Presets",
                color = TextDark,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )

            asciiPresets.forEach { preset ->
                SequenceLibraryCommandButton(
                    title = preset.displayName,
                    rawCommand = preset.rawCommand,
                    description = preset.description,
                    expectedResponse = preset.expectedResponse,
                    onClick = { onAddAsciiPreset(preset) }
                )
            }

            Text(
                text = "Quick Add",
                color = TextDark,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )

            SequenceLibraryActionButton(
                title = "Raw packet command",
                subtitle = "Tap to fill next empty slot",
                onClick = onAddRawStep
            )

            SequenceLibraryActionButton(
                title = "Pause / delay step",
                subtitle = "Tap to fill next empty slot",
                onClick = onAddPauseStep
            )

            Text(
                text = "All Commands",
                color = TextDark,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )

            SequenceLibraryInfoCard(
                title = "Known command library",
                subtitle = "Preset ASCII commands are now staged with their real names"
            )

            SequenceLibraryInfoCard(
                title = "Protocol-specific presets",
                subtitle = "SSM K-Line, CAN, raw packet, ASCII"
            )

            SequenceLibraryInfoCard(
                title = "Delay helpers",
                subtitle = "Quiet time, inter-byte, inter-step waits"
            )
        }
    }
}

@Composable
private fun SequenceLibraryCommandButton(
    title: String,
    rawCommand: String,
    description: String,
    expectedResponse: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SlotGray,
            contentColor = TextDark
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
            SelectionContainer {
                Text(
                    text = rawCommand,
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Expected: $expectedResponse",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SequenceLibraryActionButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SlotGray,
            contentColor = TextDark
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SequenceLibraryInfoCard(
    title: String,
    subtitle: String
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = PanelWhite),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = TextDark,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
