package com.hashslingingflasher.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sequence_steps")
data class SequenceStepEntity(
    @PrimaryKey val id: String,
    val sequenceId: String,
    val orderIndex: Int,
    val stepType: String,
    val title: String,
    val enabled: Boolean = true,
    val command: String? = null,
    val hexPayload: String? = null,
    val pauseMs: Long? = null,
    val timeoutMs: Long? = null,
    val ignoreEcho: Boolean = false,
    val stopOnFailure: Boolean = true
)
