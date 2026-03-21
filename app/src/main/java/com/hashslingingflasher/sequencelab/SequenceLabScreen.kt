package com.hashslingingflasher.sequencelab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hashslingingflasher.sequencelab.components.RunLogPanel
import com.hashslingingflasher.sequencelab.components.RuntimeContextPanel
import com.hashslingingflasher.sequencelab.components.SequenceControlsBar
import com.hashslingingflasher.sequencelab.components.SequenceStepCard

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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Sequence Lab",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Status: ${uiState.statusMessage}",
            style = MaterialTheme.typography.bodyMedium
        )

        SequenceControlsBar(
            isRunning = uiState.isRunning,
            onRunSequence = onRunSequence,
            onStopSequence = onStopSequence,
            onClearLog = onClearLog
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onAddAsciiStep) {
                Text("Add ASCII Step")
            }
            Button(onClick = onAddRawStep) {
                Text("Add Raw Step")
            }
            Button(onClick = onAddPauseStep) {
                Text("Add Pause")
            }
        }

        Text(
            text = "Steps",
            style = MaterialTheme.typography.titleMedium
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = uiState.currentSequence.steps,
                key = { it.id }
            ) { step ->
                SequenceStepCard(
                    step = step,
                    onRemove = { onRemoveStep(step.id) }
                )
            }
        }

        RuntimeContextPanel(
            context = uiState.runtimeContext
        )

        RunLogPanel(
            runLog = uiState.runLog
        )
    }
}
