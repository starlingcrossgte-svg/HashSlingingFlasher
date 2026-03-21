package com.hashslingingflasher.sequence

class SequenceRunner {

    fun run(
        sequence: SequenceDefinition,
        startingContext: SequenceContext
    ): Pair<SequenceContext, List<StepExecutionResult>> {
        var context = startingContext
        val results = mutableListOf<StepExecutionResult>()

        sequence.steps.forEach { step ->
            if (!step.enabled) return@forEach

            val result = when (step) {
                is SequenceStep.AdapterAsciiStep -> {
                    StepExecutionResult(
                        stepId = step.id,
                        success = false,
                        errorMessage = "Adapter ASCII execution not implemented yet"
                    )
                }

                is SequenceStep.RawHexStep -> {
                    StepExecutionResult(
                        stepId = step.id,
                        success = false,
                        errorMessage = "Raw hex execution not implemented yet"
                    )
                }

                is SequenceStep.PauseStep -> {
                    StepExecutionResult(
                        stepId = step.id,
                        success = true,
                        responseAscii = "Paused for ${step.durationMs} ms",
                        durationMs = step.durationMs
                    )
                }
            }

            results += result

            context = context.copy(
                lastStepId = step.id,
                lastResponseHex = result.responseHex,
                lastResponseAscii = result.responseAscii,
                lastError = result.errorMessage
            )
        }

        return context to results
    }
}
