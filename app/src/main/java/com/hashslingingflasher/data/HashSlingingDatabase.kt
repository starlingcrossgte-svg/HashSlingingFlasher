package com.hashslingingflasher.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hashslingingflasher.data.dao.SequenceDao
import com.hashslingingflasher.data.entity.SequenceEntity
import com.hashslingingflasher.data.entity.SequenceStepEntity

@Database(
    entities = [
        SequenceEntity::class,
        SequenceStepEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HashSlingingDatabase : RoomDatabase() {
    abstract fun sequenceDao(): SequenceDao
}
