package com.hashslingingflasher

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

class OpenPortInterrogator(
    private val usbDeviceManager: UsbDeviceManager
) {
    private val responseInterpreter = OpenPortResponseInterpreter()

    fun profileCommand(command: String, selectedMode: String): OpenPortCommandProfile {
        val normalized = command.trim().lowercase()

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

        val sendSequence = when (selectedMode) {
            CommandModeHelper.MODE_ADAPTER_ASCII -> "Send Sequence: direct command only"
            CommandModeHelper.MODE_RAW_PACKET -> "Send Sequence: raw packet lane selected"
            CommandModeHelper.MODE_SUBARU_SSM_KLINE -> "Send Sequence: Subaru SSM / K-line lane selected"
            else -> "Send Sequence: unknown mode selected"
        }

        val interrogationPath = "Interrogation Path: $selectedMode"

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
        return runManualCommand(command, CommandModeHelper.MODE_ADAPTER_ASCII)
    }

    fun runManualCommand(command: String, selectedMode: String): OpenPortInterrogationResult {
        val profile = profileCommand(command, selectedMode)

        return when (selectedMode) {
            CommandModeHelper.MODE_ADAPTER_ASCII -> runAdapterAsciiCommand(command, profile)
            CommandModeHelper.MODE_RAW_PACKET -> buildModeStubResult(
                profile = profile,
                statusMessage = "Raw packet mode not implemented yet"
            )
            CommandModeHelper.MODE_SUBARU_SSM_KLINE -> buildModeStubResult(
                profile = profile,
                statusMessage = "Subaru SSM / K-line mode not implemented yet"
            )
            else -> buildModeStubResult(
                profile = profile,
                statusMessage = "Unknown manual command mode"
            )
        }
    }

    fun endManualSession(): Boolean {
        return usbDeviceManager.endManualSession()
    }

    private fun runAdapterAsciiCommand(
        command: String,
        profile: OpenPortCommandProfile
    ): OpenPortInterrogationResult {
        val transportResult = usbDeviceManager.sendCustomAsciiCommand(command)
        val interpreted = responseInterpreter.interpret(transportResult)

        return OpenPortInterrogationResult(
            profile = profile,
            transportResult = transportResult,
            responseTypeSummary = interpreted.responseTypeSummary,
            statusSummary = interpreted.statusSummary,
            errorSummary = interpreted.errorSummary
        )
    }

    private fun buildModeStubResult(
        profile: OpenPortCommandProfile,
        statusMessage: String
    ): OpenPortInterrogationResult {
        EcuLogger.main(statusMessage)

        val transportResult = TactrixTestResult(
            success = false,
            statusMessage = statusMessage,
            bytesSent = 0,
            bytesReceived = 0,
            responseHex = "",
            responseAscii = ""
        )

        return OpenPortInterrogationResult(
            profile = profile,
            transportResult = transportResult,
            responseTypeSummary = "Response Type: Mode stub",
            statusSummary = statusMessage,
            errorSummary = "Last Error: $statusMessage"
        )
    }
}
