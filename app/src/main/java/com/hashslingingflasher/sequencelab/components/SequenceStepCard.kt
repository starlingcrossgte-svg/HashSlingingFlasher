package com.hashslingingflasher.sequencelab.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hashslingingflasher.sequence.SequenceStep

@Composable
fun SequenceStepCard(
    step: SequenceStep,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.titleMedium
            )

            when (step) {
                is SequenceStep.AdapterAsciiStep -> {
                    Text("Type: Adapter ASCII")
                    Text("Command: ${step.command}")
                    Text("Timeout: ${step.timeoutMs} ms")
                    Text("Ignore echo: ${step.ignoreEcho}")
                    Text("Stop on failure: ${step.stopOnFailure}")
                }
                is SequenceStep.RawHexStep -> {
                    Text("Type: Raw Hex")
                    Text("Payload: ${step.hexPayload}")
                    Text("Timeout: ${step.timeoutMs} ms")
                    Text("Ignore echo: ${step.ignoreEcho}")
                    Text("Stop on failure: ${step.stopOnFailure}")
                }
                is SequenceStep.PauseStep -> {
                    Text("Type: Pause")
                    Text("Duration: ${step.durationMs} ms")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onRemove) {
                    Text("Remove")
                }
            }
        }
    }
}
