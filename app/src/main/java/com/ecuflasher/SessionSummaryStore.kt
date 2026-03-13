package com.ecuflasher

object SessionSummaryStore {

    @Volatile
    var lastOpenPortCommand: String = "None"
        private set

    @Volatile
    var lastBusMode: String = "None"
        private set

    @Volatile
    var lastEcuQuery: String = "None"
        private set

    @Volatile
    var lastResponseType: String = "None"
        private set

    @Volatile
    var lastError: String = "None"
        private set

    @Synchronized
    fun setOpenPortCommand(value: String) {
        lastOpenPortCommand = value
    }

    @Synchronized
    fun setBusMode(value: String) {
        lastBusMode = value
    }

    @Synchronized
    fun setEcuQuery(value: String) {
        lastEcuQuery = value
    }

    @Synchronized
    fun setResponseType(value: String) {
        lastResponseType = value
    }

    @Synchronized
    fun setError(value: String) {
        lastError = value
    }

    @Synchronized
    fun clear() {
        lastOpenPortCommand = "None"
        lastBusMode = "None"
        lastEcuQuery = "None"
        lastResponseType = "None"
        lastError = "None"
    }
}
