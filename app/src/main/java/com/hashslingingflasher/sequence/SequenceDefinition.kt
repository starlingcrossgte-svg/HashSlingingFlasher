package com.hashslingingflasher.sequence

data class SequenceDefinition(
    val id: String,
    val name: String,
    val description: String = "",
    val steps: List<SequenceStep> = emptyList()
)
