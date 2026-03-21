package com.hashslingingflasher.sequencelab

import androidx.lifecycle.ViewModel
import com.hashslingingflasher.sequence.SequenceContext
import com.hashslingingflasher.sequence.SequenceDefinition
import com.hashslingingflasher.sequence.SequenceMode
import com.hashslingingflasher.sequence.SequenceStep
import com.hashslingingflasher.sequence.StepExecutionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SequenceLabViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SequenceLabUiState())
    val uiState: StateFlow<SequenceLabUiState> = _uiState.asStateFlow()

    fun setMode(mode: SequenceMode) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }

    fun setSequenceName(name: String) {
        val updatedSequence = _uiState.value.currentSequence.copy(name = name)
        _uiState.value = _uiState.value.copy(currentSequence = updatedSequence)
    }

    fun addStep(step: SequenceStep) {
        val updatedSteps = _uiState.value.currentSequence.steps + step
        val updatedSequence = _uiState.value.currentSequence.copy(steps = updatedSteps)
        _uiState.value = _uiState.value.copy(currentSequence = updatedSequence)
    }

    fun removeStep(stepId: String) {
        val updatedSteps = _uiState.value.currentSequence.steps.filterNot { it.id == stepId }
        val updatedSequence = _uiState.value.currentSequence.copy(steps = updatedSteps)
        _uiState.value = _uiState.value.copy(currentSequence = updatedSequence)
    }

    fun updateRuntimeContext(context: SequenceContext) {
        _uiState.value = _uiState.value.copy(runtimeContext = context)
    }

    fun appendLog(result: StepExecutionResult) {
        _uiState.value = _uiState.value.copy(runLog = _uiState.value.runLog + result)
    }

    fun setRunning(isRunning: Boolean, statusMessage: String) {
        _uiState.value = _uiState.value.copy(
            isRunning = isRunning,
            statusMessage = statusMessage
        )
    }

    fun loadSequence(sequence: SequenceDefinition) {
        _uiState.value = _uiState.value.copy(currentSequence = sequence)
    }

    fun clearLog() {
        _uiState.value = _uiState.value.copy(runLog = emptyList())
    }
}
