package com.hashslingingflasher.sequence

data class SequenceContext(
    val mode: SequenceMode = SequenceMode.ADAPTER_ASCII,
    val currentBaud: Int? = null,
    val initCompleted: Boolean = false,
    val lastResponseHex: String = "",
    val lastResponseAscii: String = "",
    val lastError: String = "",
    val lastStepId: String? = null,
    val extractedValues: Map<String, String> = emptyMap()
)
