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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hashslingingflasher.sequence.SequenceMode
import com.hashslingingflasher.sequence.SequenceStep
import com.hashslingingflasher.sequencelab.components.LightningProgressBar
import com.hashslingingflasher.sequencelab.components.LightningProgressState

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

    val boltState = when {
        uiState.runtimeContext.lastError.isNotBlank() -> LightningProgressState.FAILED
        uiState.isRunning -> LightningProgressState.RUNNING
        uiState.runtimeContext.initCompleted -> LightningProgressState.READY
        else -> LightningProgressState.IDLE
    }

    val boltProgress = when {
        uiState.runtimeContext.lastError.isNotBlank() -> 0.45f
        uiState.isRunning -> 0.65f
        uiState.runtimeContext.initCompleted -> 0.28f
        else -> 0f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Sequence Lab",
            style = MaterialTheme.typography.headlineLarge,
            color = TextDark,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.SemiBold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderGray)
        )

        LightningProgressBar(
            state = boltState,
            progress = boltProgress,
            modifier = Modifier.fillMaxWidth()
        )

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
            val stageWidth = maxWidth * 0.78f
            val libraryWidth = maxWidth * 0.94f

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    modifier = Modifier.width(stageWidth),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                }

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
