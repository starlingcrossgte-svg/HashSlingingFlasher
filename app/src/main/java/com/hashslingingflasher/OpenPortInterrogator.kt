package com.hashslingingflasher

import android.content.Context

data class OpenPortCommandProfile(
    val normalizedCommand: String,
    val displayCommand: String,
    val interrogationPath: String,
    val busModeSummary: String,
    val commandFamilySummary: String,
    val sendSequenceSummary: String
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
        val skipAutoAtaWake = shouldSkipAutoAtaWake(normalized)

        val busMode = when {
            normalized == "ata" -> "Bus Mode: None"
            normalized.startsWith("at06") && normalized.contains("500000") -> "Bus Mode: CAN 500000"
            normalized.startsWith("atsp") -> "Bus Mode: CAN protocol select"
            normalized.startsWith("ati") -> "Bus Mode: Adapter info"
            normalized.startsWith("atrv") -> "Bus Mode: Adapter voltage query"
            normalized.startsWith("at") -> "Bus Mode: OpenPort adapter command"
            else -> "Bus Mode: Manual OpenPort"
        }

        val commandFamily = when {
            normalized == "ata" -> "Command Family: Adapter wake"
            normalized.startsWith("ati") -> "Command Family: Adapter identification"
            normalized.startsWith("atrv") -> "Command Family: Adapter voltage"
            normalized.startsWith("atsp") -> "Command Family: Protocol selection"
            normalized.startsWith("at06") -> "Command Family: CAN bus setup"
            normalized.startsWith("at") -> "Command Family: OpenPort adapter command"
            else -> "Command Family: Manual payload"
        }

        val sendSequence = when {
            normalized == "ata" ->
                "Send Sequence: direct ATA only"
            skipAutoAtaWake ->
                "Send Sequence: direct command only (ATA wake bypassed)"
            normalized.startsWith("at") ->
                "Send Sequence: auto ATA wake, then adapter command"
            else ->
                "Send Sequence: auto ATA wake, then manual payload"
        }

        val interrogationPath = "Interrogation Path: OpenPort ASCII"

        return OpenPortCommandProfile(
            normalizedCommand = normalized,
            displayCommand = command.trim(),
            interrogationPath = interrogationPath,
            busModeSummary = busMode,
            commandFamilySummary = commandFamily,
            sendSequenceSummary = sendSequence
        )
    }

    fun runManualCommand(command: String): OpenPortInterrogationResult {
        val profile = profileCommand(command)
        val skipAutoAtaWake = shouldSkipAutoAtaWake(profile.normalizedCommand)
        val transportResult = UsbDeviceManager(context).sendCustomAsciiCommand(
            command = command,
            skipAutoAtaWake = skipAutoAtaWake
        )
        val interpreted = responseInterpreter.interpret(transportResult)

        return OpenPortInterrogationResult(
            profile = profile,
            transportResult = transportResult,
            responseTypeSummary = interpreted.responseTypeSummary,
            statusSummary = interpreted.statusSummary,
            errorSummary = interpreted.errorSummary
        )
    }

    private fun shouldSkipAutoAtaWake(normalizedCommand: String): Boolean {
        return normalizedCommand == "ata" ||
            normalizedCommand.startsWith("ati") ||
            normalizedCommand.startsWith("at06")
    }
}
