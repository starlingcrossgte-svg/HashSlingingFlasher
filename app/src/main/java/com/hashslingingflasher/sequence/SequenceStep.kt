package com.hashslingingflasher.sequence

sealed class SequenceStep {
    abstract val id: String
    abstract val title: String
    abstract val enabled: Boolean

    data class AdapterAsciiStep(
        override val id: String,
        override val title: String,
        override val enabled: Boolean = true,
        val command: String,
        val timeoutMs: Long = 1000L,
        val ignoreEcho: Boolean = false,
        val stopOnFailure: Boolean = true
    ) : SequenceStep()

    data class RawHexStep(
        override val id: String,
        override val title: String,
        override val enabled: Boolean = true,
        val hexPayload: String,
        val timeoutMs: Long = 1000L,
        val ignoreEcho: Boolean = false,
        val stopOnFailure: Boolean = true
    ) : SequenceStep()

    data class PauseStep(
        override val id: String,
        override val title: String,
        override val enabled: Boolean = true,
        val durationMs: Long
    ) : SequenceStep()
}
