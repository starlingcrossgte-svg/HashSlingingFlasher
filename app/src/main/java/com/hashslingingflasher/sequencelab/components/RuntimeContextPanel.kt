package com.hashslingingflasher.sequencelab.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hashslingingflasher.sequence.SequenceContext

@Composable
fun RuntimeContextPanel(
    context: SequenceContext,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Runtime Context",
                style = MaterialTheme.typography.titleMedium
            )
            Text("Mode: ${context.mode}")
            Text("Current baud: ${context.currentBaud ?: "unset"}")
            Text("Init completed: ${context.initCompleted}")
            Text("Last response hex: ${context.lastResponseHex.ifBlank { "none" }}")
            Text("Last response ascii: ${context.lastResponseAscii.ifBlank { "none" }}")
            Text("Last error: ${context.lastError.ifBlank { "none" }}")
            Text("Last step id: ${context.lastStepId ?: "none"}")
            Text("Extracted values: ${if (context.extractedValues.isEmpty()) "none" else context.extractedValues}")
        }
    }
}
