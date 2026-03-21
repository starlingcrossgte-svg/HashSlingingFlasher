package com.hashslingingflasher

import android.content.Context

data class TactrixTestResult(
    val success: Boolean,
    val statusMessage: String,
    val bytesSent: Int,
    val bytesReceived: Int,
    val responseHex: String,
    val responseAscii: String
)

class UsbDeviceManager(private val context: Context) {

    companion object {
        private const val TACTRIX_VENDOR_ID = 1027
        private const val TACTRIX_PRODUCT_ID = 52301
    }

    private val sessionManager = OpenPortUsbSessionManager(
        context = context,
        tactrixVendorId = TACTRIX_VENDOR_ID,
        tactrixProductId = TACTRIX_PRODUCT_ID
    )

    private val transport = OpenPortTransport()

    @Volatile
    private var manualSession: OpenPortSession? = null

    fun openTactrixChannel(): TactrixTestResult {
        val sessionResult = sessionManager.openSession("Opening Tactrix connection")
        if (sessionResult.error != null) {
            return sessionResult.error
        }

        val session = sessionResult.session ?: return TactrixTestResult(
            false,
            "Failed to open Tactrix session",
            -1,
            -1,
            "",
            ""
        )

        try {
            val ataResult = transport.sendAsciiCommand(
                connection = session.connection,
                endpointOut = session.endpointOut,
                endpointIn = session.endpointIn,
                commandLabel = "OpenPort ATA command",
                commandString = "ata\r\n"
            )

            if (ataResult.responseAscii.contains("aro", ignoreCase = true)) {
                return TactrixTestResult(
                    false,
                    "OpenPort ATA failed before raw transmit test",
                    ataResult.bytesSent,
                    ataResult.bytesReceived,
                    ataResult.responseHex,
                    ataResult.responseAscii
                )
            }

            val at06Result = transport.sendAsciiCommand(
                connection = session.connection,
                endpointOut = session.endpointOut,
                endpointIn = session.endpointIn,
                commandLabel = "OpenPort AT06 CAN command",
                commandString = "at06 0 500000 0\r\n"
            )

            if (at06Result.responseAscii.contains("aro", ignoreCase = true)) {
                return TactrixTestResult(
                    false,
                    "OpenPort CAN bus open failed before raw transmit test",
                    at06Result.bytesSent,
                    at06Result.bytesReceived,
                    at06Result.responseHex,
                    at06Result.responseAscii
                )
            }

            val rawResult = sendSubaruSsmQuery(
                session = session
            )

            val rawTransmitSent = rawResult.bytesSent > 0

            return TactrixTestResult(
                success = rawTransmitSent,
                statusMessage = if (rawTransmitSent) {
                    "Raw transmit completed"
                } else {
                    "Raw transmit failed"
                },
                bytesSent = rawResult.bytesSent,
                bytesReceived = rawResult.bytesReceived,
                responseHex = rawResult.responseHex,
                responseAscii = rawResult.responseAscii
            )
        } finally {
            sessionManager.closeSession(session)
        }
    }
    @Synchronized
    fun sendCustomAsciiCommand(command: String): TactrixTestResult {
        val sessionResult = getOrOpenManualSession()
        if (sessionResult.error != null) {
            return sessionResult.error
        }

        val session = sessionResult.session ?: return TactrixTestResult(
            false,
            "Failed to open Tactrix session",
            -1,
            -1,
            "",
            ""
        )

        val normalizedCommand = if (command.endsWith("\r\n")) {
            command
        } else {
            "$command\r\n"
        }

        EcuLogger.usb("Manual command path is direct-only; automatic ATA wake disabled")

        val result = transport.sendAsciiCommand(
            connection = session.connection,
            endpointOut = session.endpointOut,
            endpointIn = session.endpointIn,
            commandLabel = "Manual OpenPort command",
            commandString = normalizedCommand
        )

        val gotResponse = result.bytesReceived > 0 &&
            (result.responseAscii.isNotEmpty() || result.responseHex.isNotEmpty())

        return TactrixTestResult(
            success = gotResponse,
            statusMessage = if (gotResponse) {
                "OpenPort response received"
            } else {
                "No response from OpenPort"
            },
            bytesSent = result.bytesSent,
            bytesReceived = result.bytesReceived,
            responseHex = result.responseHex,
            responseAscii = result.responseAscii
        )
    }

    @Synchronized
    fun sendSubaruSsmCanCommand(command: String): TactrixTestResult {
        val sessionResult = getOrOpenManualSession()
        if (sessionResult.error != null) {
            return sessionResult.error
        }

        val session = sessionResult.session ?: return TactrixTestResult(
            false,
            "Failed to open Tactrix session",
            -1,
            -1,
            "",
            ""
        )

        EcuLogger.usb("Subaru SSM CAN command requested: $command")

        val at06Result = transport.sendAsciiCommand(
            connection = session.connection,
            endpointOut = session.endpointOut,
            endpointIn = session.endpointIn,
            commandLabel = "OpenPort AT06 CAN command",
            commandString = "at06 0 500000 0\r\n"
        )

        if (at06Result.responseAscii.contains("aro", ignoreCase = true)) {
            return TactrixTestResult(
                false,
                "OpenPort CAN bus open failed before Subaru SSM query",
                at06Result.bytesSent,
                at06Result.bytesReceived,
                at06Result.responseHex,
                at06Result.responseAscii
            )
        }

        val rawResult = sendSubaruSsmQuery(
            session = session
        )

        val gotResponse = rawResult.bytesReceived > 0 &&
            (rawResult.responseAscii.isNotEmpty() || rawResult.responseHex.isNotEmpty())

        return TactrixTestResult(
            success = gotResponse,
            statusMessage = if (gotResponse) {
                "Subaru SSM CAN response received"
            } else {
                "No response from Subaru SSM CAN query"
            },
            bytesSent = rawResult.bytesSent,
            bytesReceived = rawResult.bytesReceived,
            responseHex = rawResult.responseHex,
            responseAscii = rawResult.responseAscii
        )
    }

    @Synchronized
    fun sendSubaruSsmKlineCommand(command: String): TactrixTestResult {
        val sessionResult = getOrOpenManualSession()
        if (sessionResult.error != null) {
            return sessionResult.error
        }

        val session = sessionResult.session ?: return TactrixTestResult(
            false,
            "Failed to open Tactrix session",
            -1,
            -1,
            "",
            ""
        )

        EcuLogger.usb("Subaru SSM K-line command requested: $command")

        val baudResult = sendKlineAsciiCommand(
            session = session,
            commandLabel = "OpenPort K-line ISO baud command",
            commandString = "atib 48\r\n"
        )
        if (looksLikeAdapterRejection(baudResult.responseAscii)) {
            return TactrixTestResult(
                false,
                "OpenPort rejected K-line ISO baud command",
                baudResult.bytesSent,
                baudResult.bytesReceived,
                baudResult.responseHex,
                baudResult.responseAscii
            )
        }

        val initAddrResult = sendKlineAsciiCommand(
            session = session,
            commandLabel = "OpenPort K-line slow init address",
            commandString = "atiia 33\r\n"
        )
        if (looksLikeAdapterRejection(initAddrResult.responseAscii)) {
            return TactrixTestResult(
                false,
                "OpenPort rejected K-line slow init address",
                initAddrResult.bytesSent,
                initAddrResult.bytesReceived,
                initAddrResult.responseHex,
                initAddrResult.responseAscii
            )
        }

        val keywordResult = sendKlineAsciiCommand(
            session = session,
            commandLabel = "OpenPort K-line keyword mode",
            commandString = "atkw0\r\n"
        )
        if (looksLikeAdapterRejection(keywordResult.responseAscii)) {
            return TactrixTestResult(
                false,
                "OpenPort rejected K-line keyword mode command",
                keywordResult.bytesSent,
                keywordResult.bytesReceived,
                keywordResult.responseHex,
                keywordResult.responseAscii
            )
        }

        val protocolResult = sendKlineAsciiCommand(
            session = session,
            commandLabel = "OpenPort K-line protocol select",
            commandString = "atsp3\r\n"
        )
        if (looksLikeAdapterRejection(protocolResult.responseAscii)) {
            return TactrixTestResult(
                false,
                "OpenPort rejected K-line protocol selection",
                protocolResult.bytesSent,
                protocolResult.bytesReceived,
                protocolResult.responseHex,
                protocolResult.responseAscii
            )
        }

        val slowInitResult = sendKlineAsciiCommand(
            session = session,
            commandLabel = "OpenPort K-line slow initiation",
            commandString = "atsi\r\n"
        )
        if (looksLikeAdapterRejection(slowInitResult.responseAscii)) {
            return TactrixTestResult(
                false,
                "OpenPort rejected K-line slow-init command",
                slowInitResult.bytesSent,
                slowInitResult.bytesReceived,
                slowInitResult.responseHex,
                slowInitResult.responseAscii
            )
        }

        val normalizedCommand = command.trim().uppercase()

        if (normalizedCommand == "SSM_KLINE_INIT") {
            val gotResponse = hasNonAckResponse(slowInitResult)

            return TactrixTestResult(
                success = gotResponse,
                statusMessage = if (gotResponse) {
                    "K-line slow-init response captured"
                } else {
                    "K-line slow-init command sent; adapter acknowledged but no ECU payload captured"
                },
                bytesSent = baudResult.bytesSent +
                    initAddrResult.bytesSent +
                    keywordResult.bytesSent +
                    protocolResult.bytesSent +
                    slowInitResult.bytesSent,
                bytesReceived = baudResult.bytesReceived +
                    initAddrResult.bytesReceived +
                    keywordResult.bytesReceived +
                    protocolResult.bytesReceived +
                    slowInitResult.bytesReceived,
                responseHex = slowInitResult.responseHex,
                responseAscii = slowInitResult.responseAscii
            )
        }

        if (normalizedCommand == "SSM_KLINE_READ") {
            val probePacket = byteArrayOf(
                0x80.toByte(),
                0xF0.toByte(),
                0x28.toByte(),
                0x01.toByte(),
                0xBF.toByte(),
                0x58.toByte()
            )

            EcuLogger.usb("Waiting 125 ms after K-line init before first SSM probe")
            Thread.sleep(125)
            EcuLogger.usb("OpenPort K-line first SSM probe -> ${transport.toHex(probePacket)}")

            val probeResult = transport.sendRawPacket(
                connection = session.connection,
                endpointOut = session.endpointOut,
                endpointIn = session.endpointIn,
                packetLabel = "K-line first SSM probe",
                packet = probePacket,
                noDataMessage = "No data returned",
                timeoutMessage = "Read timed out or no response from device"
            )

            val gotResponse = hasNonAckResponse(probeResult)

            return TactrixTestResult(
                success = gotResponse,
                statusMessage = if (gotResponse) {
                    "K-line init completed and first SSM probe response captured"
                } else {
                    "K-line init completed, but no response to first SSM probe"
                },
                bytesSent = baudResult.bytesSent +
                    initAddrResult.bytesSent +
                    keywordResult.bytesSent +
                    protocolResult.bytesSent +
                    slowInitResult.bytesSent +
                    probeResult.bytesSent,
                bytesReceived = baudResult.bytesReceived +
                    initAddrResult.bytesReceived +
                    keywordResult.bytesReceived +
                    protocolResult.bytesReceived +
                    slowInitResult.bytesReceived +
                    probeResult.bytesReceived,
                responseHex = probeResult.responseHex,
                responseAscii = probeResult.responseAscii
            )
        }

        val normalizedPayload = command.replace(Regex("\\s+"), "")
        val payloadResult = sendKlineAsciiCommand(
            session = session,
            commandLabel = "OpenPort K-line custom payload",
            commandString = "$normalizedPayload\r\n"
        )

        val gotPayloadResponse = hasNonAckResponse(payloadResult)

        return TactrixTestResult(
            success = gotPayloadResponse,
            statusMessage = if (gotPayloadResponse) {
                "K-line init completed and custom payload response captured"
            } else {
                "K-line init completed, but no response to custom K-line payload"
            },
            bytesSent = baudResult.bytesSent +
                initAddrResult.bytesSent +
                keywordResult.bytesSent +
                protocolResult.bytesSent +
                slowInitResult.bytesSent +
                payloadResult.bytesSent,
            bytesReceived = baudResult.bytesReceived +
                initAddrResult.bytesReceived +
                keywordResult.bytesReceived +
                protocolResult.bytesReceived +
                slowInitResult.bytesReceived +
                payloadResult.bytesReceived,
            responseHex = payloadResult.responseHex,
            responseAscii = payloadResult.responseAscii
        )
    }

    @Synchronized
    fun endManualSession(): Boolean {
        val session = manualSession ?: run {
            EcuLogger.usb("No active manual Tactrix session to close")
            return false
        }

        sessionManager.closeSession(session)
        manualSession = null
        EcuLogger.usb("Persistent manual Tactrix session ended")
        return true
    }

    @Synchronized
    private fun getOpenManualSession(): SessionOpenResult {
        val existingSession = manualSession
        if (existingSession != null) {
            EcuLogger.usb("Closing stale persistent manual Tactrix session before opening fresh one")
            sessionManager.closeSession(existingSession)
            manualSession = null
        }

        val sessionResult = sessionManager.openSession("Opening Tactrix connection for manual command")
        if (sessionResult.error != null) {
            return sessionResult
        }

        val session = sessionResult.session ?: return SessionOpenResult(
            error = TactrixTestResult(
                false,
                "Failed to open Tactrix session",
                -1,
                -1,
                "",
                ""
            )
        )

        manualSession = session
        EcuLogger.usb("Persistent manual Tactrix session opened")
        return SessionOpenResult(session = session)
    }

    private fun sendSubaruSsmQuery(
        session: OpenPortSession
    ): OpenPortCommandResult {
        val canFrame = byteArrayOf(
            0x00,
            0x0D,
            0x07,
            0xE0.toByte(),
            0xAA.toByte(),
            0x0D,
            0x00,
            0x00,
            0x00
        )

        EcuLogger.usb("Sending Subaru SSM raw CAN frame")
        EcuLogger.usb("CAN frame hex: ${transport.toHex(canFrame)}")

        return transport.sendRawPacket(
            connection = session.connection,
            endpointOut = session.endpointOut,
            endpointIn = session.endpointIn,
            packetLabel = "Packet",
            packet = canFrame,
            noDataMessage = "No response returned",
            timeoutMessage = "Read timed out after raw transmit"
        )
    }

    private fun sendKlineAsciiCommand(
        session: OpenPortSession,
        commandLabel: String,
        commandString: String
    ): OpenPortCommandResult {
        EcuLogger.usb("$commandLabel -> ${commandString.trim()}")

        return transport.sendAsciiCommand(
            connection = session.connection,
            endpointOut = session.endpointOut,
            endpointIn = session.endpointIn,
            commandLabel = commandLabel,
            commandString = commandString
        )
    }

    private fun hasNonAckResponse(result: OpenPortCommandResult): Boolean {
        if (result.bytesReceived <= 0) {
            return false
        }

        if (looksLikeAdapterRejection(result.responseAscii)) {
            return false
        }

        return !looksLikeAdapterAcknowledgement(result.responseAscii) ||
            result.responseHex.isNotEmpty() && result.responseAscii.trim().isEmpty()
    }

    private fun looksLikeAdapterAcknowledgement(responseAscii: String): Boolean {
        val normalized = responseAscii.trim().lowercase()
        return normalized == "ok" ||
            normalized == "ok>" ||
            normalized == ">" ||
            normalized == "searching..." ||
            normalized.startsWith("ok\r") ||
            normalized.startsWith("ok\n") ||
            normalized.startsWith("are ") ||
            normalized.startsWith("ari ") ||
            normalized.contains("main code version")
    }

    private fun looksLikeAdapterRejection(responseAscii: String): Boolean {
        val normalized = responseAscii.trim().lowercase()
        return normalized.contains("aro") ||
            normalized.contains("error") ||
            normalized.contains("?")
    }
}
