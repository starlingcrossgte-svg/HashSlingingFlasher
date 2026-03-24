package com.hashslingingflasher.sequencelab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashslingingflasher.obdlink.ObdLinkTransport
import com.hashslingingflasher.sequence.SequenceDefinition
import com.hashslingingflasher.sequence.SequenceMode
import com.hashslingingflasher.sequence.SequenceRunner
import com.hashslingingflasher.sequence.SequenceStep
import com.hashslingingflasher.sequence.StepExecutionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SequenceLabViewModel : ViewModel() {

    private val runner = SequenceRunner()
    private var runJob: Job? = null

    private val _uiState = MutableStateFlow(SequenceLabUiState())
    val uiState: StateFlow<SequenceLabUiState> = _uiState.asStateFlow()

    fun attachObdLinkTransport(transport: ObdLinkTransport) {
        runner.attachTransport(transport)
    }

    fun clearObdLinkTransport() {
        runner.clearTransport()
    }

    fun setMode(mode: SequenceMode) {
        _uiState.value = _uiState.value.copy(
            selectedMode = mode,
            runtimeContext = _uiState.value.runtimeContext.copy(mode = mode)
        )
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

    fun sendSingleAsciiCommand(command: String) {
        if (_uiState.value.isRunning) return

        val trimmedCommand = command.trim()
        if (trimmedCommand.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isRunning = false,
                statusMessage = "No single ASCII command entered"
            )
            return
        }

        val singleStep = SequenceStep.AdapterAsciiStep(
            id = "single-ascii-${System.currentTimeMillis()}",
            title = trimmedCommand,
            command = trimmedCommand,
            stopOnFailure = true
        )

        val singleSequence = SequenceDefinition(
            id = "single-ascii-sequence",
            name = "Single ASCII Command",
            steps = listOf(singleStep)
        )

        val startContext = _uiState.value.runtimeContext.copy(mode = _uiState.value.selectedMode)

        _uiState.value = _uiState.value.copy(
            isRunning = true,
            statusMessage = "Sending single ASCII command...",
            runLog = emptyList(),
            runtimeContext = startContext
        )

        runJob = viewModelScope.launch(Dispatchers.Default) {
            val (endingContext, results) = runner.run(singleSequence, startContext)
            val firstFailure = results.firstOrNull { !it.success }

            _uiState.value = _uiState.value.copy(
                runtimeContext = endingContext,
                runLog = results,
                isRunning = false,
                statusMessage = when {
                    firstFailure != null -> "Single command failed at ${firstFailure.stepId}"
                    else -> "Single command complete"
                }
            )
        }
    }

    fun runSequence() {
        if (_uiState.value.isRunning) return

        val sequence = _uiState.value.currentSequence
        val startContext = _uiState.value.runtimeContext.copy(mode = _uiState.value.selectedMode)

        if (sequence.steps.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isRunning = false,
                statusMessage = "No steps to run",
                runLog = emptyList(),
                runtimeContext = startContext
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isRunning = true,
            statusMessage = "Running ${sequence.steps.size} steps...",
            runLog = emptyList(),
            runtimeContext = startContext
        )

        runJob = viewModelScope.launch(Dispatchers.Default) {
            val (endingContext, results) = runner.run(sequence, startContext)
            val firstFailure = results.firstOrNull { !it.success }

            _uiState.value = _uiState.value.copy(
                runtimeContext = endingContext,
                runLog = results,
                isRunning = false,
                statusMessage = when {
                    firstFailure != null -> "Failed at ${firstFailure.stepId}"
                    else -> "Sequence complete"
                }
            )
        }
    }

    fun stopSequence() {
        runJob?.cancel()
        runJob = null
        _uiState.value = _uiState.value.copy(
            isRunning = false,
            statusMessage = "Stop requested"
        )
    }

    fun clearLog() {
        _uiState.value = _uiState.value.copy(runLog = emptyList())
    }

    override fun onCleared() {
        runJob?.cancel()
        runner.clearTransport()
        super.onCleared()
    }
}
