package com.hashslingingflasher.sequencelab

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hashslingingflasher.sequence.SequenceMode
import com.hashslingingflasher.sequence.SequenceStep

private val HeaderBlack = Color(0xFF060A18)
private val ScreenWhite = Color(0xFFF4F5F7)
private val PanelWhite = Color(0xFFF8F8FA)
private val SlotGray = Color(0xFFE7E8ED)
private val BorderGray = Color(0xFFD7D9E0)
private val ActiveBlue = Color(0xFF2F6FE4)
private val InactiveGray = Color(0xFFD9DCE3)
private val TextDark = Color(0xFF1C1F26)
private val TextMuted = Color(0xFF6C7280)
private val WarningYellow = Color(0xFFFFD54A)
private val FailRed = Color(0xFFD93025)

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
            .background(HeaderBlack)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderPanel(
            isRunning = uiState.isRunning,
            initCompleted = uiState.runtimeContext.initCompleted,
            lastError = uiState.runtimeContext.lastError
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
            colors = CardDefaults.cardColors(containerColor = ScreenWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Sequence Lab",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.width(250.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                .height(50.dp),
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
                    }

                    CommandLibraryPanel(
                        modifier = Modifier.width(420.dp),
                        onAddAsciiStep = onAddAsciiStep,
                        onAddRawStep = onAddRawStep,
                        onAddPauseStep = onAddPauseStep
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
        }
    }
}

@Composable
private fun HeaderPanel(
    isRunning: Boolean,
    initCompleted: Boolean,
    lastError: String
) {
    val hasFailure = lastError.isNotBlank()
    val boltColor = when {
        hasFailure -> FailRed
        isRunning -> WarningYellow
        initCompleted -> WarningYellow
        else -> WarningYellow.copy(alpha = 0.28f)
    }

    val bolt2Alpha = when {
        hasFailure -> 1f
        isRunning -> 0.9f
        initCompleted -> 0.45f
        else -> 0.18f
    }

    val bolt3Alpha = when {
        hasFailure -> 0.45f
        isRunning -> 0.75f
        initCompleted -> 0.25f
        else -> 0.08f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(HeaderBlack)
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡",
                color = boltColor,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "⚡",
                color = boltColor.copy(alpha = bolt2Alpha),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "⚡",
                color = boltColor.copy(alpha = bolt3Alpha),
                style = MaterialTheme.typography.headlineMedium
            )
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
            .height(42.dp),
        shape = RoundedCornerShape(6.dp),
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
                fontWeight = FontWeight.Medium
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
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = PanelWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = PanelWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
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

            LibrarySection(
                title = "Quick Add",
                entries = listOf(
                    "Adapter ASCII command" to "Tap to fill next empty slot",
                    "Raw packet command" to "Tap to fill next empty slot",
                    "Pause / delay step" to "Tap to fill next empty slot"
                ),
                onFirst = onAddAsciiStep,
                onSecond = onAddRawStep,
                onThird = onAddPauseStep
            )

            LibrarySectionStatic(
                title = "All Commands",
                entries = listOf(
                    "Known command library" to "Tap a command later to stage it",
                    "Protocol-specific presets" to "SSM K-Line, CAN, raw packet, ASCII",
                    "Delay helpers" to "Quiet time, inter-byte, inter-step waits"
                )
            )

            LibrarySectionStatic(
                title = "Notes",
                entries = listOf(
                    "Tap a filled step slot" to "Remove it from the sequence",
                    "Only staged steps will run" to "Sequence ends at the last active slot"
                )
            )
        }
    }
}

@Composable
private fun LibrarySection(
    title: String,
    entries: List<Pair<String, String>>,
    onFirst: () -> Unit,
    onSecond: () -> Unit,
    onThird: () -> Unit
) {
    val actions = listOf(onFirst, onSecond, onThird)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = TextDark,
            fontWeight = FontWeight.Bold
        )

        entries.forEachIndexed { index, entry ->
            Button(
                onClick = actions[index],
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
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
                        text = entry.first,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = entry.second,
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun LibrarySectionStatic(
    title: String,
    entries: List<Pair<String, String>>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = TextDark,
            fontWeight = FontWeight.Bold
        )

        entries.forEach { entry ->
            Card(
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = PanelWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = entry.first,
                        color = TextDark,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = entry.second,
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
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
