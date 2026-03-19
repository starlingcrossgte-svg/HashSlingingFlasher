package com.hashslingingflasher

import android.content.Context

data class TactrixTestResult(
    val success: Boolean,
    val statusMessage: String,
    val bytesSent: Int,
    val bytesReceived: Int,
    val responseHex: String,
    val responseAscii: String = ""
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
    fun sendSubaruSsmCommand(command: String): TactrixTestResult {
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

        EcuLogger.usb("Subaru SSM command requested: $command")

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
                "Subaru SSM response received"
            } else {
                "No response from Subaru SSM query"
            },
            bytesSent = rawResult.bytesSent,
            bytesReceived = rawResult.bytesReceived,
            responseHex = rawResult.responseHex,
            responseAscii = rawResult.responseAscii
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
    private fun getOrOpenManualSession(): SessionOpenResult {
        val existingSession = manualSession
        if (existingSession != null) {
            EcuLogger.usb("Reusing persistent manual Tactrix session")
            return SessionOpenResult(session = existingSession)
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
}
