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

    fun updateSingleAsciiCommand(command: String) {
        _uiState.value = _uiState.value.copy(singleAsciiCommand = command)
    }

    fun sendSingleAsciiCommand() {
        if (_uiState.value.isRunning) return

        val trimmedCommand = _uiState.value.singleAsciiCommand.trim()
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

    fun updateCommandSlot(index: Int, command: String) {
        if (index !in 0 until _uiState.value.commandSlots.size) return

        val updatedSlots = _uiState.value.commandSlots.toMutableList().apply {
            this[index] = command
        }

        val updatedSteps = updatedSlots.mapIndexedNotNull { slotIndex, slotCommand ->
            val trimmed = slotCommand.trim()
            if (trimmed.isEmpty()) {
                null
            } else {
                SequenceStep.AdapterAsciiStep(
                    id = "slot-${slotIndex + 1}",
                    title = trimmed,
                    command = trimmed,
                    stopOnFailure = true
                )
            }
        }

        val updatedSequence = _uiState.value.currentSequence.copy(steps = updatedSteps)

        _uiState.value = _uiState.value.copy(
            commandSlots = updatedSlots,
            currentSequence = updatedSequence
        )
    }

    fun clearCommandSlot(index: Int) {
        updateCommandSlot(index, "")
    }

    fun sendCommandSlot(index: Int) {
        if (_uiState.value.isRunning) return
        if (index !in 0 until _uiState.value.commandSlots.size) return

        val trimmedCommand = _uiState.value.commandSlots[index].trim()
        if (trimmedCommand.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isRunning = false,
                statusMessage = "Slot ${index + 1} is empty"
            )
            return
        }

        val singleStep = SequenceStep.AdapterAsciiStep(
            id = "slot-send-${index + 1}-${System.currentTimeMillis()}",
            title = trimmedCommand,
            command = trimmedCommand,
            stopOnFailure = true
        )

        val singleSequence = SequenceDefinition(
            id = "slot-send-sequence-${index + 1}",
            name = "Slot ${index + 1} Send",
            steps = listOf(singleStep)
        )

        val startContext = _uiState.value.runtimeContext.copy(mode = _uiState.value.selectedMode)

        _uiState.value = _uiState.value.copy(
            isRunning = true,
            statusMessage = "Sending slot ${index + 1}...",
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
                    firstFailure != null -> "Slot ${index + 1} failed"
                    else -> "Slot ${index + 1} complete"
                }
            )
        }
    }

    fun addStep(step: SequenceStep) {
        val firstEmptyIndex = _uiState.value.commandSlots.indexOfFirst { it.isBlank() }
        if (step is SequenceStep.AdapterAsciiStep && firstEmptyIndex != -1) {
            updateCommandSlot(firstEmptyIndex, step.command)
            return
        }

        val updatedSteps = _uiState.value.currentSequence.steps + step
        val updatedSequence = _uiState.value.currentSequence.copy(steps = updatedSteps)
        _uiState.value = _uiState.value.copy(currentSequence = updatedSequence)
    }

    fun removeStep(stepId: String) {
        val slotIndex = _uiState.value.currentSequence.steps.indexOfFirst { it.id == stepId }
        if (slotIndex in 0 until _uiState.value.commandSlots.size) {
            clearCommandSlot(slotIndex)
            return
        }

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
        val updatedSlots = MutableList(6) { "" }

        sequence.steps.take(6).forEachIndexed { index, step ->
            updatedSlots[index] = when (step) {
                is SequenceStep.AdapterAsciiStep -> step.command
                is SequenceStep.RawHexStep -> step.hexPayload
                is SequenceStep.PauseStep -> step.title
            }
        }

        _uiState.value = _uiState.value.copy(
            currentSequence = sequence,
            singleAsciiCommand = "",
            commandSlots = updatedSlots
        )
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
