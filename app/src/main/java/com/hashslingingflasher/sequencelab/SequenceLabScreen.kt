package com.hashslingingflasher.sequencelab
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hashslingingflasher.sequence.SequenceMode
import com.hashslingingflasher.sequence.SequenceStep

private val HeaderGray = Color(0xFF5D636B)
private val ScreenWhite = Color(0xFFF3F4F6)
private val PanelWhite = Color(0xFFF8F8FA)
private val SlotGray = Color(0xFFE6E8ED)
private val BorderGray = Color(0xFFD4D7DE)
private val ActiveBlue = Color(0xFF2F6FE4)
private val InactiveGray = Color(0xFFD8DCE3)
private val TextDark = Color(0xFF171A20)
private val TextMuted = Color(0xFF6B7280)

@Composable
fun SequenceLabScreen(
    uiState: SequenceLabUiState,
    onAddPauseStep: () -> Unit,
    onAddAsciiStep: () -> Unit,
    onAddRawStep: () -> Unit,
    onRemoveStep: (String) -> Unit,
    onRunSequence: () -> Unit,
    onStopSequence: () -> Unit,
    onClearLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stagedSteps = uiState.currentSequence.steps.take(6)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenWhite)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderGray)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Text(
                text = "THE LAB",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderGray)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TransportButton(
                    label = "SSM KLINE",
                    selected = uiState.selectedMode == SequenceMode.SSM_KLINE
                )
                TransportButton(
                    label = "SSM CAN",
                    selected = uiState.selectedMode == SequenceMode.SSM_CAN
                )
                TransportButton(
                    label = "RAW PACK.",
                    selected = uiState.selectedMode == SequenceMode.RAW_HEX
                )
                TransportButton(
                    label = "ASCII",
                    selected = uiState.selectedMode == SequenceMode.ADAPTER_ASCII
                )
            }

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val stageWidth = maxWidth
                val libraryWidth = maxWidth

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier.width(stageWidth),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val latestResult = uiState.runLog.lastOrNull()
                    val transportModeText = uiState.runtimeContext.mode.name.replace('_', ' ')
                    val currentBaudText = uiState.runtimeContext.currentBaud?.toString() ?: "-"
                    val targetIdText = uiState.runtimeContext.extractedValues["targetId"] ?: "-"
                    val initStatusText = if (uiState.runtimeContext.initCompleted) "Complete" else "Not initialized"
                    val lastStepText = uiState.runtimeContext.lastStepId ?: "-"
                    val lastResultText = when {
                        latestResult == null -> "-"
                        latestResult.success -> "PASS"
                        else -> "FAIL"
                    }
                    val lastDurationText = latestResult?.durationMs?.toString()?.plus(" ms") ?: "-"
                    val logText = if (uiState.runLog.isEmpty()) {
                        ""
                    } else {
                        uiState.runLog.joinToString("\n\n") { result ->
                            buildString {
                                append(if (result.success) "[PASS] " else "[FAIL] ")
                                append(result.stepId)
                                append("\nHEX: ")
                                append(result.responseHex.ifBlank { "-" })
                                append("\nASCII: ")
                                append(result.responseAscii.ifBlank { "-" })
                                append("\nERROR: ")
                                append(result.errorMessage.ifBlank { "-" })
                                append("\nTIME: ")
                                append(result.durationMs)
                                append(" ms")
                            }
                        }
                    }

                    repeat(6) { index ->
                        val step = stagedSteps.getOrNull(index)

                        SequenceSlotButton(
                            label = slotLabel(index, step),
                            onClick = {
                                if (step != null) {
                                    onRemoveStep(step.id)
                                }
                            }
                        )

                        if (index < 5) {
                            DelayStrip(label = "Delay / Quiet Time")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = onRunSequence,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActiveBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Run Sequence",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmallUtilityButton(
                            text = "Stop",
                            onClick = onStopSequence
                        )
                        SmallUtilityButton(
                            text = "Clear Log",
                            onClick = onClearLog
                        )
                    }

                    DetailGridCard(
                        title = "Session Summary",
                        leftEntries = listOf(
                            "Active adapter" to "OBDLink EX / MX+",
                            "Target ID" to targetIdText,
                            "Session state" to uiState.statusMessage
                        ),
                        rightEntries = listOf(
                            "Transport mode" to transportModeText,
                            "Current baud" to currentBaudText,
                            "Init status" to initStatusText
                        )
                    )

                    DetailGridCard(
                        title = "Communication Details",
                        leftEntries = listOf(
                            "Last response hex" to uiState.runtimeContext.lastResponseHex.ifBlank { "-" },
                            "Last response ascii" to uiState.runtimeContext.lastResponseAscii.ifBlank { "-" },
                            "Last error" to uiState.runtimeContext.lastError.ifBlank { "-" }
                        ),
                        rightEntries = listOf(
                            "Last step" to lastStepText,
                            "Last result" to lastResultText,
                            "Last duration" to lastDurationText
                        )
                    )

                    FixedLogConsoleCard(
                        title = "Logging Console",
                        logText = logText
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                CommandLibraryPanel(
                    modifier = Modifier.width(libraryWidth),
                    onAddAsciiStep = onAddAsciiStep,
                    onAddRawStep = onAddRawStep,
                    onAddPauseStep = onAddPauseStep
                )
            }
            }
        }
    }

}
@Composable
private fun TransportButton(
    label: String,
    selected: Boolean
) {
    Button(
        onClick = { },
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) ActiveBlue else InactiveGray,
            contentColor = if (selected) Color.White else TextMuted
        )
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SequenceSlotButton(
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SlotGray,
            contentColor = TextDark
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "›",
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DelayStrip(
    label: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = PanelWhite),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "+",
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CommandLibraryPanel(
    modifier: Modifier = Modifier,
    onAddAsciiStep: () -> Unit,
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
                text = "Quick Add",
                color = TextDark,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )

            LibraryActionButton(
                title = "Adapter ASCII command",
                subtitle = "Tap to fill next empty slot",
                onClick = onAddAsciiStep
            )

            LibraryActionButton(
                title = "Raw packet command",
                subtitle = "Tap to fill next empty slot",
                onClick = onAddRawStep
            )

            LibraryActionButton(
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

            LibraryInfoCard(
                title = "Known command library",
                subtitle = "Tap a command later to stage it"
            )

            LibraryInfoCard(
                title = "Protocol-specific presets",
                subtitle = "SSM K-Line, CAN, raw packet, ASCII"
            )

            LibraryInfoCard(
                title = "Delay helpers",
                subtitle = "Quiet time, inter-byte, inter-step waits"
            )
        }
    }
}

@Composable
private fun LibraryActionButton(
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
private fun LibraryInfoCard(
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

@Composable
private fun SmallUtilityButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = InactiveGray,
            contentColor = TextDark
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DetailGridCard(
    title: String,
    leftEntries: List<Pair<String, String>>,
    rightEntries: List<Pair<String, String>>
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = TextDark,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val columnWidth = (maxWidth - 10.dp) / 2

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier.width(columnWidth),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        leftEntries.forEach { entry ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = entry.first,
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = entry.second.ifBlank { "-" },
                                    color = TextDark,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.width(columnWidth),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rightEntries.forEach { entry ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = entry.first,
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = entry.second.ifBlank { "-" },
                                    color = TextDark,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FixedLogConsoleCard(
    title: String,
    logText: String
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = TextDark,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101215))
            ) {
                SelectionContainer {
                    Text(
                        text = if (logText.isBlank()) "No log entries yet." else logText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                        color = Color(0xFFE8EAED),
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private fun slotLabel(
    index: Int,
    step: SequenceStep?
): String {
    return if (step == null) {
        "Step ${index + 1}: Choose Command"
    } else {
        "Step ${index + 1}: ${stepTitle(step)}"
    }
}

private fun stepTitle(step: SequenceStep): String {
    return when (step) {
        is SequenceStep.AdapterAsciiStep -> step.title
        is SequenceStep.RawHexStep -> step.title
        is SequenceStep.PauseStep -> step.title
    }
}
