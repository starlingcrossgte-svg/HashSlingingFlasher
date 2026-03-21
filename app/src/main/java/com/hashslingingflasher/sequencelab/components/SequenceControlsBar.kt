package com.hashslingingflasher.sequencelab.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SequenceControlsBar(
    isRunning: Boolean,
    onRunSequence: () -> Unit,
    onStopSequence: () -> Unit,
    onClearLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onRunSequence,
            enabled = !isRunning
        ) {
            Text("Run")
        }

        Button(
            onClick = onStopSequence,
            enabled = isRunning
        ) {
            Text("Stop")
        }

        Button(
            onClick = onClearLog
        ) {
            Text("Clear Log")
        }
    }
}
