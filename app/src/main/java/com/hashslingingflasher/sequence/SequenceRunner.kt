package com.hashslingingflasher.sequence

import com.hashslingingflasher.obdlink.ObdLinkTransport

class SequenceRunner {

    private var transport: ObdLinkTransport? = null

    fun attachTransport(transport: ObdLinkTransport) {
        this.transport = transport
    }

    fun clearTransport() {
        transport = null
    }

    fun run(
        sequence: SequenceDefinition,
        startingContext: SequenceContext
    ): Pair<SequenceContext, List<StepExecutionResult>> {
        var context = startingContext
        val results = mutableListOf<StepExecutionResult>()

        for (step in sequence.steps) {
            if (!step.enabled) continue

            val startedAt = System.currentTimeMillis()

            val result = when (step) {
                is SequenceStep.AdapterAsciiStep -> {
                    val activeTransport = transport
                    if (activeTransport == null) {
                        StepExecutionResult(
                            stepId = step.id,
                            success = false,
                            errorMessage = "No OBDLink transport attached"
                        )
                    } else {
                        val commandResult = activeTransport.sendAdapterAscii(
                            command = step.command,
                            timeoutMs = step.timeoutMs
                        )
                        StepExecutionResult(
                            stepId = step.id,
                            success = commandResult.success,
                            responseHex = commandResult.responseHex,
                            responseAscii = commandResult.responseAscii,
                            errorMessage = commandResult.errorMessage
                        )
                    }
                }

                is SequenceStep.RawHexStep -> {
                    val activeTransport = transport
                    if (activeTransport == null) {
                        StepExecutionResult(
                            stepId = step.id,
                            success = false,
                            errorMessage = "No OBDLink transport attached"
                        )
                    } else {
                        val commandResult = activeTransport.sendRawHex(
                            hexPayload = step.hexPayload,
                            timeoutMs = step.timeoutMs
                        )
                        StepExecutionResult(
                            stepId = step.id,
                            success = commandResult.success,
                            responseHex = commandResult.responseHex,
                            responseAscii = commandResult.responseAscii,
                            errorMessage = commandResult.errorMessage
                        )
                    }
                }

                is SequenceStep.PauseStep -> {
                    try {
                        Thread.sleep(step.durationMs)
                    } catch (_: InterruptedException) {
                    }

                    StepExecutionResult(
                        stepId = step.id,
                        success = true,
                        responseAscii = "Paused for ${step.durationMs} ms"
                    )
                }
            }

            val finishedAt = System.currentTimeMillis()
            val finalizedResult = result.copy(
                durationMs = finishedAt - startedAt
            )

            results += finalizedResult

            context = context.copy(
                lastStepId = step.id,
                lastResponseHex = finalizedResult.responseHex,
                lastResponseAscii = finalizedResult.responseAscii,
                lastError = finalizedResult.errorMessage
            )

            val shouldStop = when (step) {
                is SequenceStep.AdapterAsciiStep -> !finalizedResult.success && step.stopOnFailure
                is SequenceStep.RawHexStep -> !finalizedResult.success && step.stopOnFailure
                is SequenceStep.PauseStep -> false
            }

            if (shouldStop) {
                break
            }
        }

        return context to results
    }
}
