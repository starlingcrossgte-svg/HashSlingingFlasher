package com.ecuflasher

object InAppLogStore {

    private const val MAX_LINES = 120
    private val lines = mutableListOf<String>()

    @Synchronized
    fun add(tag: String, message: String) {
        lines.add("[$tag] $message")

        while (lines.size > MAX_LINES) {
            lines.removeAt(0)
        }
    }

    @Synchronized
    fun getAllText(): String {
        return if (lines.isEmpty()) {
            "No log entries yet"
        } else {
            lines.joinToString("\n")
        }
    }

    @Synchronized
    fun clear() {
        lines.clear()
    }
}
