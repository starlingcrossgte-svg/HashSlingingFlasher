package com.hashslingingflasher.sequencelab

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import com.hashslingingflasher.sequencelab.components.DetailGridCard
import com.hashslingingflasher.sequencelab.components.FixedLogConsoleCard
import com.hashslingingflasher.sequencelab.components.SequenceCommandLibraryPanel
import com.hashslingingflasher.sequencelab.components.SingleAsciiCommandPanel
import com.hashslingingflasher.sequencelab.components.SmallUtilityButton

private val ScreenWhite = Color(0xFFF3F4F6)
private val SlotGray = Color(0xFFE6E8ED)
private val BorderGray = Color(0xFFD4D7DE)
private val ActiveBlue = Color(0xFF2F6FE4)
private val TextDark = Color(0xFF171A20)
private val TextMuted = Color(0xFF6B7280)

@Composable
fun SequenceLabScreen(
    uiState: SequenceLabUiState,
    onAddPauseStep: () -> Unit,
    asciiPresets: List<ObdLinkAsciiPreset>,
    onAddAsciiPreset: (ObdLinkAsciiPreset) -> Unit,
    onAddRawStep: () -> Unit,
    onRemoveStep: (String) -> Unit,
    onUpdateSingleAsciiCommand: (String) -> Unit,
    onSendSingleAsciiCommand: () -> Unit,
    onUpdateCommandSlot: (Int, String) -> Unit,
    onSendCommandSlot: (Int) -> Unit,
    onDiscoverUsb: () -> Unit,
    onRunSequence: () -> Unit,
    onStopSequence: () -> Unit,
    onClearLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenWhite)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                                    append("\nMODE: ")
                                    append(result.modeLabel.ifBlank { "-" })
                                    append("\nREQUEST ASCII: ")
                                    append(result.requestAscii.ifBlank { "-" })
                                    append("\nREQUEST HEX: ")
                                    append(result.requestHex.ifBlank { "-" })
                                    append("\nRESPONSE HEX: ")
                                    append(result.responseHex.ifBlank { "-" })
                                    append("\nRESPONSE ASCII: ")
                                    append(result.responseAscii.ifBlank { "-" })
                                    append("\nERROR: ")
                                    append(result.errorMessage.ifBlank { "-" })
                                    append("\nTIME: ")
                                    append(result.durationMs)
                                    append(" ms")
                                }
                            }
                        }

                        Text(
                            text = "Single Command",
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = SlotGray)
                        ) {
                            BasicTextField(
                                value = uiState.singleAsciiCommand,
                                onValueChange = onUpdateSingleAsciiCommand,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                singleLine = true,
                                enabled = !uiState.isRunning,
                                textStyle = TextStyle(
                                    color = TextDark,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                decorationBox = { innerTextField ->
                                    if (uiState.singleAsciiCommand.isBlank()) {
                                        Text(
                                            text = "Single ASCII command",
                                            color = TextMuted
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        Button(
                            onClick = onSendSingleAsciiCommand,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            enabled = !uiState.isRunning && uiState.singleAsciiCommand.trim().isNotEmpty(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ActiveBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Send Single Command",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Sequence Console",
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )

                        SingleAsciiCommandPanel(
                            commandSlots = uiState.commandSlots,
                            isRunning = uiState.isRunning,
                            onCommandSlotChange = onUpdateCommandSlot,
                            onSendCommandSlot = onSendCommandSlot
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SmallUtilityButton(
                                text = "Discover USB",
                                onClick = onDiscoverUsb,
                                modifier = Modifier.weight(1f)
                            )
                            SmallUtilityButton(
                                text = "Run Sequence",
                                onClick = onRunSequence,
                                modifier = Modifier.weight(1f)
                            )
                            SmallUtilityButton(
                                text = "Stop",
                                onClick = onStopSequence,
                                modifier = Modifier.weight(1f)
                            )
                            SmallUtilityButton(
                                text = "Clear Log",
                                onClick = onClearLog,
                                modifier = Modifier.weight(1f)
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
                        asciiPresets = asciiPresets,
                        onAddAsciiPreset = onAddAsciiPreset,
                        onAddRawStep = onAddRawStep,
                        onAddPauseStep = onAddPauseStep
                    )
                }
            }
        }
    }
}
