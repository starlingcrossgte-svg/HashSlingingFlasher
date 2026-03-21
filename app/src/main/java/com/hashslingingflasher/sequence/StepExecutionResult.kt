package com.hashslingingflasher.sequence

data class StepExecutionResult(
    val stepId: String,
    val success: Boolean,
    val responseHex: String = "",
    val responseAscii: String = "",
    val errorMessage: String = "",
    val durationMs: Long = 0L
)
