package com.hashslingingflasher.sequence

data class StepExecutionResult(
    val stepId: String,
    val success: Boolean,
    val requestAscii: String = "",
    val requestHex: String = "",
    val responseHex: String = "",
    val responseAscii: String = "",
    val modeLabel: String = "",
    val errorMessage: String = "",
    val durationMs: Long = 0L
)
