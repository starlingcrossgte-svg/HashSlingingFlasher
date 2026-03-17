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

    fun profileCommand(command: String): OpenPortCommandProfile {
        val normalized = command.trim().lowercase()

        val busMode = when {
            normalized == "ata" -> "Bus Mode: None"
            normalized.startsWith("ato6") && normalized.contains("500000") -> "Bus Mode: CAN 500000"
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

        val responseTypeSummary = when {
            transportResult.responseAscii.isNotEmpty() || transportResult.responseHex.isNotEmpty() ->
                "Response Type: OpenPort response"
            else ->
                "Response Type: None"
        }

        val statusSummary = when {
            transportResult.success -> "OpenPort command response received"
            else -> transportResult.statusMessage
        }

        val errorSummary = when {
            transportResult.success -> "Last Error: None"
            else -> "Last Error: ${transportResult.statusMessage}"
        }

        return OpenPortInterrogationResult(
            profile = profile,
            transportResult = transportResult,
            responseTypeSummary = responseTypeSummary,
            statusSummary = statusSummary,
            errorSummary = errorSummary
        )
    }
}
