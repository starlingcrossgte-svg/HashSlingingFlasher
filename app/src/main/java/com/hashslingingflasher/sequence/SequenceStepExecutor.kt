package com.hashslingingflasher.sequence

interface SequenceStepExecutor {
    fun executeAdapterAscii(
        step: SequenceStep.AdapterAsciiStep,
        context: SequenceContext
    ): StepExecutionResult

    fun executeRawHex(
        step: SequenceStep.RawHexStep,
        context: SequenceContext
    ): StepExecutionResult
}
