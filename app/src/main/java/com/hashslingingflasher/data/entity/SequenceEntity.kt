package com.hashslingingflasher.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sequences")
data class SequenceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = ""
)
