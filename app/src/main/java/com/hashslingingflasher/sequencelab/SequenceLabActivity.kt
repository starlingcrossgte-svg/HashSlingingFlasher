package com.hashslingingflasher.sequencelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModelProvider
import com.hashslingingflasher.sequence.SequenceStep

class SequenceLabActivity : ComponentActivity() {

    private lateinit var viewModel: SequenceLabViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[SequenceLabViewModel::class.java]

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            MaterialTheme {
                SequenceLabScreen(
                    uiState = uiState,
                    onAddPauseStep = {
                        viewModel.addStep(
                            SequenceStep.PauseStep(
                                id = "pause-${System.currentTimeMillis()}",
                                title = "Pause",
                                durationMs = 500L
                            )
                        )
                    },
                    onAddAsciiStep = {
                        viewModel.addStep(
                            SequenceStep.AdapterAsciiStep(
                                id = "ascii-${System.currentTimeMillis()}",
                                title = "Adapter ASCII",
                                command = "atz"
                            )
                        )
                    },
                    onAddRawStep = {
                        viewModel.addStep(
                            SequenceStep.RawHexStep(
                                id = "raw-${System.currentTimeMillis()}",
                                title = "Raw Hex",
                                hexPayload = "80 18 F0 01 BF 48"
                            )
                        )
                    },
                    onRemoveStep = { stepId ->
                        viewModel.removeStep(stepId)
                    },
                    onRunSequence = {
                        viewModel.setRunning(true, "Sequence runner not wired yet")
                    },
                    onStopSequence = {
                        viewModel.setRunning(false, "Stopped")
                    },
                    onClearLog = {
                        viewModel.clearLog()
                    }
                )
            }
        }
    }
}
