package com.hashslingingflasher.sequencelab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hashslingingflasher.sequence.SequenceMode
import com.hashslingingflasher.sequence.SequenceStep
import com.hashslingingflasher.sequence.StepExecutionResult

private val BgBlack = Color(0xFF090A0C)
private val PanelBlack = Color(0xFF14161A)
private val SlotBlack = Color(0xFF1B1E23)
private val AccentOrange = Color(0xFFFF6A00)
private val AccentOrangeDim = Color(0xFF5A3317)
private val TextWhite = Color(0xFFF4F4F4)
private val TextGray = Color(0xFFB8BDC7)
private val PassGreen = Color(0xFF18C964)
private val FailRed = Color(0xFFFF3B30)
private val PendingGray = Color(0xFF4A4F57)

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
    val visibleSteps = uiState.currentSequence.steps.take(6)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "SEQUENCE LAB",
            style = MaterialTheme.typography.headlineMedium,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "STATUS: ${uiState.statusMessage.uppercase()}",
            style = MaterialTheme.typography.bodyLarge,
            color = TextGray
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModeButton(
                label = "SSM\nKLINE",
                selected = uiState.selectedMode == SequenceMode.SSM_KLINE
            )

            ModeButton(
                label = "CANBUS\nRAW",
                selected = uiState.selectedMode == SequenceMode.SSM_CAN ||
                    uiState.selectedMode == SequenceMode.RAW_HEX
            )

            ModeButton(
                label = "ASCII",
                selected = uiState.selectedMode == SequenceMode.ADAPTER_ASCII
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PanelBlack)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.width(156.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(6) { index ->
                        val step = visibleSteps.getOrNull(index)
                        CommandSlotButton(
                            label = stepLabel(step, index),
                            onClick = {
                                when (index % 3) {
                                    0 -> onAddAsciiStep()
                                    1 -> onAddRawStep()
                                    else -> onAddPauseStep()
                                }
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier.width(156.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(6) { index ->
                        val step = visibleSteps.getOrNull(index)
                        val result = step?.let { latestResultForStep(uiState.runLog, it.id) }
                        StatusBox(
                            status = statusLabel(step, result),
                            statusColor = statusColor(step, result)
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PanelBlack)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "RUNTIME SNAPSHOT",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text("Mode: ${uiState.runtimeContext.mode}", color = TextGray)
                Text("Current baud: ${uiState.runtimeContext.currentBaud ?: "unset"}", color = TextGray)
                Text("Init completed: ${uiState.runtimeContext.initCompleted}", color = TextGray)
                Text(
                    "Last response hex: ${uiState.runtimeContext.lastResponseHex.ifBlank { "none" }}",
                    color = TextGray
                )
                Text(
                    "Last response ascii: ${uiState.runtimeContext.lastResponseAscii.ifBlank { "none" }}",
                    color = TextGray
                )
                Text(
                    "Last error: ${uiState.runtimeContext.lastError.ifBlank { "none" }}",
                    color = TextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = onRunSequence,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentOrange,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "START SEQUENCE",
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onStopSequence,
                modifier = Modifier
                    .width(156.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentOrangeDim,
                    contentColor = TextWhite
                )
            ) {
                Text("STOP")
            }

            Button(
                onClick = onClearLog,
                modifier = Modifier
                    .width(156.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SlotBlack,
                    contentColor = TextWhite
                )
            ) {
                Text("CLEAR LOG")
            }
        }
    }
}

@Composable
private fun ModeButton(
    label: String,
    selected: Boolean
) {
    Button(
        onClick = { },
        modifier = Modifier
            .width(104.dp)
            .height(82.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) AccentOrange else SlotBlack,
            contentColor = if (selected) Color.Black else TextWhite
        )
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CommandSlotButton(
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(156.dp)
            .height(58.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SlotBlack,
            contentColor = TextWhite
        )
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusBox(
    status: String,
    statusColor: Color
) {
    Card(
        modifier = Modifier
            .width(156.dp)
            .height(58.dp),
        colors = CardDefaults.cardColors(containerColor = SlotBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = status,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun stepLabel(step: SequenceStep?, index: Int): String {
    if (step == null) return "COMMAND ${index + 1}"
    return when (step) {
        is SequenceStep.AdapterAsciiStep -> step.title.uppercase()
        is SequenceStep.RawHexStep -> step.title.uppercase()
        is SequenceStep.PauseStep -> step.title.uppercase()
    }
}

private fun latestResultForStep(
    runLog: List<StepExecutionResult>,
    stepId: String
): StepExecutionResult? {
    return runLog.lastOrNull { it.stepId == stepId }
}

private fun statusLabel(
    step: SequenceStep?,
    result: StepExecutionResult?
): String {
    if (step == null) return "EMPTY"
    if (result == null) return "WAITING"
    return if (result.success) "PASS" else "FAIL"
}

private fun statusColor(
    step: SequenceStep?,
    result: StepExecutionResult?
): Color {
    if (step == null) return PendingGray
    if (result == null) return PendingGray
    return if (result.success) PassGreen else FailRed
}
