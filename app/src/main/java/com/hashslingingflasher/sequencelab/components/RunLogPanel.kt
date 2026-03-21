package com.hashslingingflasher.sequencelab.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hashslingingflasher.sequence.StepExecutionResult

@Composable
fun RunLogPanel(
    runLog: List<StepExecutionResult>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Run Log",
                style = MaterialTheme.typography.titleMedium
            )

            if (runLog.isEmpty()) {
                Text("No step results yet.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(runLog, key = { it.stepId + it.durationMs }) { item ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Step: ${item.stepId}")
                            Text("Success: ${item.success}")
                            Text("Hex: ${item.responseHex.ifBlank { "none" }}")
                            Text("Ascii: ${item.responseAscii.ifBlank { "none" }}")
                            Text("Error: ${item.errorMessage.ifBlank { "none" }}")
                            Text("Duration: ${item.durationMs} ms")
                        }
                    }
                }
            }
        }
    }
}
