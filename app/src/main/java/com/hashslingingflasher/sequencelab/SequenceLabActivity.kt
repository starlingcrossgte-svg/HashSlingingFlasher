package com.hashslingingflasher.sequencelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import com.hashslingingflasher.sequence.SequenceStep

class SequenceLabActivity : ComponentActivity() {

    private lateinit var viewModel: SequenceLabViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[SequenceLabViewModel::class.java]

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            val sequenceLabColors = darkColorScheme(
                primary = Color(0xFFFF6A00),
                secondary = Color(0xFFFF6A00),
                tertiary = Color(0xFFFF6A00),
                background = Color(0xFF090A0C),
                surface = Color(0xFF14161A),
                onPrimary = Color.Black,
                onSecondary = Color.Black,
                onTertiary = Color.Black,
                onBackground = Color(0xFFF4F4F4),
                onSurface = Color(0xFFF4F4F4)
            )

            MaterialTheme(
                colorScheme = sequenceLabColors
            ) {
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
