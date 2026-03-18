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

            val atoResult = transport.sendAsciiCommand(
                connection = session.connection,
                endpointOut = session.endpointOut,
                endpointIn = session.endpointIn,
                commandLabel = "OpenPort AT06 CAN command",
                commandString = "at06 0 500000 0\r\n"
            )

            if (atoResult.responseAscii.contains("aro", ignoreCase = true)) {
                return TactrixTestResult(
                    false,
                    "OpenPort CAN bus open failed before raw transmit test",
                    atoResult.bytesSent,
                    atoResult.bytesReceived,
                    atoResult.responseHex,
                    atoResult.responseAscii
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

    fun sendCustomAsciiCommand(command: String): TactrixTestResult {
        val sessionResult = sessionManager.openSession("Opening Tactrix connection for manual command")
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
            val normalizedCommand = if (command.endsWith("\r\n")) {
                command
            } else {
                "$command\r\n"
            }

            val trimmedLowerCommand = command.trim().lowercase()
            val shouldSendAtaWake = trimmedLowerCommand != "ata"

            if (shouldSendAtaWake) {
                val wakeResult = transport.sendAsciiCommand(
                    connection = session.connection,
                    endpointOut = session.endpointOut,
                    endpointIn = session.endpointIn,
                    commandLabel = "Manual command ATA wake",
                    commandString = "ata\r\n"
                )

                if (wakeResult.responseAscii.contains("aro", ignoreCase = true)) {
                    return TactrixTestResult(
                        false,
                        "OpenPort did not respond to ATA wake",
                        wakeResult.bytesSent,
                        wakeResult.bytesReceived,
                        wakeResult.responseHex,
                        wakeResult.responseAscii
                    )
                }
            } else {
                EcuLogger.usb("Skipping automatic ATA wake because command is ATA")
            }

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
        } finally {
            sessionManager.closeSession(session)
        }
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
