package com.hashslingingflasher.sequencelab

import com.hashslingingflasher.sequence.SequenceContext
import com.hashslingingflasher.sequence.SequenceDefinition
import com.hashslingingflasher.sequence.SequenceMode
import com.hashslingingflasher.sequence.StepExecutionResult

data class SequenceLabUiState(
    val selectedMode: SequenceMode = SequenceMode.ADAPTER_ASCII,
    val currentSequence: SequenceDefinition = SequenceDefinition(
        id = "default-sequence",
        name = "New Sequence"
    ),
    val runtimeContext: SequenceContext = SequenceContext(),
    val runLog: List<StepExecutionResult> = emptyList(),
    val isRunning: Boolean = false,
    val statusMessage: String = "Idle"
)
