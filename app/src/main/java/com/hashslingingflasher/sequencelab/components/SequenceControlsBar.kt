package com.hashslingingflasher.sequencelab.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SequenceControlsBar(
    isRunning: Boolean,
    onDiscoverUsb: () -> Unit,
    onRunSequence: () -> Unit,
    onStopSequence: () -> Unit,
    onClearLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onDiscoverUsb,
                enabled = !isRunning
            ) {
                Text("Discover USB")
            }

            Button(
                onClick = onRunSequence,
                enabled = !isRunning
            ) {
                Text("Run")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
}
