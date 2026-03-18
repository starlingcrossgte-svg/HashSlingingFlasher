package com.hashslingingflasher

import android.content.Context

data class OpenPortCommandProfile(
    val normalizedCommand: String,
    val displayCommand: String,
    val interrogationPath: String,
    val busModeSummary: String
)

data class OpenPortInterrogationResult(
    val profile: OpenPortCommandProfile,
    val transportResult: TactrixTestResult,
    val responseTypeSummary: String,
    val statusSummary: String,
    val errorSummary: String
)

class OpenPortInterrogator(private val context: Context) {

    private val responseInterpreter = OpenPortResponseInterpreter()

    fun profileCommand(command: String): OpenPortCommandProfile {
        val normalized = command.trim().lowercase()

        val busMode = when {
            normalized == "ata" -> "Bus Mode: None"
            normalized.startsWith("at06") && normalized.contains("500000") -> "Bus Mode: CAN 500000"
            normalized.startsWith("atsp") -> "Bus Mode: CAN"
            else -> "Bus Mode: Manual OpenPort"
        }

        return OpenPortCommandProfile(
            normalizedCommand = normalized,
            displayCommand = command.trim(),
            interrogationPath = "Interrogation Path: OpenPort ASCII",
            busModeSummary = busMode
        )
    }

    fun runManualCommand(command: String): OpenPortInterrogationResult {
        val profile = profileCommand(command)
        val transportResult = UsbDeviceManager(context).sendCustomAsciiCommand(command)
        val interpreted = responseInterpreter.interpret(transportResult)

        return OpenPortInterrogationResult(
            profile = profile,
            transportResult = transportResult,
            responseTypeSummary = interpreted.responseTypeSummary,
            statusSummary = interpreted.statusSummary,
            errorSummary = interpreted.errorSummary
        )
    }
}
