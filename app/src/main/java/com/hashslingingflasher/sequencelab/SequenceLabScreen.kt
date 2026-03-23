package com.hashslingingflasher.sequencelab
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hashslingingflasher.sequence.SequenceMode
import com.hashslingingflasher.sequence.SequenceStep
import com.hashslingingflasher.sequencelab.components.SequenceCommandLibraryPanel
import com.hashslingingflasher.sequencelab.components.FixedLogConsoleCard
import com.hashslingingflasher.sequencelab.components.SmallUtilityButton
import com.hashslingingflasher.sequencelab.components.DetailGridCard
import com.hashslingingflasher.sequencelab.components.TransportButton
import com.hashslingingflasher.sequencelab.components.SequenceSlotButton
import com.hashslingingflasher.sequencelab.components.DelayStrip
import com.hashslingingflasher.sequencelab.components.SequenceLabHeader

private val ScreenWhite = Color(0xFFF3F4F6)
private val BorderGray = Color(0xFFD4D7DE)
private val ActiveBlue = Color(0xFF2F6FE4)

@Composable
fun SequenceLabScreen(
    uiState: SequenceLabUiState,
    onAddPauseStep: () -> Unit,
    onAddAsciiStep: () -> Unit,
    onAddRawStep: () -> Unit,
    onRemoveStep: (String) -> Unit,
    onDiscoverUsb: () -> Unit,
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
        SequenceLabHeader()

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
                text = "Discover USB",
                onClick = onDiscoverUsb
            )
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

                SequenceCommandLibraryPanel(
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
