package com.hashslingingflasher.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hashslingingflasher.data.entity.SequenceEntity
import com.hashslingingflasher.data.entity.SequenceStepEntity

@Dao
interface SequenceDao {

    @Query("SELECT * FROM sequences ORDER BY name ASC")
    suspend fun getAllSequences(): List<SequenceEntity>

    @Query("SELECT * FROM sequences WHERE id = :sequenceId LIMIT 1")
    suspend fun getSequenceById(sequenceId: String): SequenceEntity?

    @Query("SELECT * FROM sequence_steps WHERE sequenceId = :sequenceId ORDER BY orderIndex ASC")
    suspend fun getStepsForSequence(sequenceId: String): List<SequenceStepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSequence(sequence: SequenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<SequenceStepEntity>)

    @Query("DELETE FROM sequence_steps WHERE sequenceId = :sequenceId")
    suspend fun deleteStepsForSequence(sequenceId: String)

    @Query("DELETE FROM sequences WHERE id = :sequenceId")
    suspend fun deleteSequence(sequenceId: String)
}
