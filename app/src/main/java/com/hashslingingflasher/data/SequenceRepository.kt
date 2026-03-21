package com.hashslingingflasher.data

import com.hashslingingflasher.data.dao.SequenceDao
import com.hashslingingflasher.data.entity.SequenceEntity
import com.hashslingingflasher.data.entity.SequenceStepEntity

class SequenceRepository(
    private val sequenceDao: SequenceDao
) {
    suspend fun getAllSequences(): List<SequenceEntity> {
        return sequenceDao.getAllSequences()
    }

    suspend fun getSequence(sequenceId: String): SequenceEntity? {
        return sequenceDao.getSequenceById(sequenceId)
    }

    suspend fun getSteps(sequenceId: String): List<SequenceStepEntity> {
        return sequenceDao.getStepsForSequence(sequenceId)
    }

    suspend fun saveSequence(
        sequence: SequenceEntity,
        steps: List<SequenceStepEntity>
    ) {
        sequenceDao.insertSequence(sequence)
        sequenceDao.deleteStepsForSequence(sequence.id)
        sequenceDao.insertSteps(steps)
    }

    suspend fun deleteSequence(sequenceId: String) {
        sequenceDao.deleteStepsForSequence(sequenceId)
        sequenceDao.deleteSequence(sequenceId)
    }
}
