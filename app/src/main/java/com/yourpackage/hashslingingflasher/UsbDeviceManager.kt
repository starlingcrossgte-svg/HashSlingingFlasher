package com.hashslingingflasher

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import java.nio.charset.Charset

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
        private const val READ_TIMEOUT_MS = 4000
        private const val WRITE_TIMEOUT_MS = 3000
    }

    private val usbManager: UsbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    fun openTactrixChannel(): TactrixTestResult {
        val sessionResult = openSession("Opening Tactrix connection")
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
            val ataResult = sendAsciiCommand(
                connection = session.connection,
                endpointOut = session.endpointOut,
                endpointIn = session.endpointIn,
                commandLabel = "OpenPort ATA command",
                commandString = "ata\r\n"
            )

            if (!ataResult.responseAscii.contains("aro", ignoreCase = true)) {
                return TactrixTestResult(
                    false,
                    "OpenPort ATA failed before raw transmit test",
                    ataResult.bytesSent,
                    ataResult.bytesReceived,
                    ataResult.responseHex,
                    ataResult.responseAscii
                )
            }

            val atoResult = sendAsciiCommand(
                connection = session.connection,
                endpointOut = session.endpointOut,
                endpointIn = session.endpointIn,
                commandLabel = "OpenPort ATO CAN command",
                commandString = "ato6 0 500000 0\r\n"
            )

            if (!atoResult.responseAscii.contains("aro", ignoreCase = true)) {
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
                connection = session.connection,
                endpointOut = session.endpointOut,
                endpointIn = session.endpointIn
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
            closeConnectionSafely(session.connection, session.usbInterface)
        }
    }

    fun sendCustomAsciiCommand(command: String): TactrixTestResult {
        val sessionResult = openSession("Opening Tactrix connection for manual command")
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
                val wakeResult = sendAsciiCommand(
                    connection = session.connection,
                    endpointOut = session.endpointOut,
                    endpointIn = session.endpointIn,
                    commandLabel = "Manual command ATA wake",
                    commandString = "ata\r\n"
                )

                if (!wakeResult.responseAscii.contains("aro", ignoreCase = true)) {
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

            val result = sendAsciiCommand(
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
            closeConnectionSafely(session.connection, session.usbInterface)
        }
    }

    private data class OpenPortSession(
        val device: UsbDevice,
        val connection: UsbDeviceConnection,
        val usbInterface: UsbInterface,
        val endpointOut: UsbEndpoint,
        val endpointIn: UsbEndpoint
    )

    private data class SessionOpenResult(
        val session: OpenPortSession? = null,
        val error: TactrixTestResult? = null
    )

    private data class CommandResult(
        val bytesSent: Int,
        val bytesReceived: Int,
        val responseHex: String,
        val responseAscii: String,
        val responseBytes: ByteArray
    )

    private fun openSession(openLogMessage: String): SessionOpenResult {
        val device = usbManager.deviceList.values.firstOrNull {
            it.vendorId == TACTRIX_VENDOR_ID && it.productId == TACTRIX_PRODUCT_ID
        }

        if (device == null) {
            EcuLogger.usb("Tactrix device not found")
            return SessionOpenResult(
                error = TactrixTestResult(false, "Tactrix device not detected", -1, -1, "", "")
            )
        }

        EcuLogger.usb(openLogMessage)

        val connection = usbManager.openDevice(device)
        if (connection == null) {
            EcuLogger.error("Failed to open Tactrix USB device")
            return SessionOpenResult(
                error = TactrixTestResult(false, "Failed to open Tactrix USB device", -1, -1, "", "")
            )
        }

        val usbInterface = findBulkCommunicationInterface(device)
        if (usbInterface == null) {
            EcuLogger.error("No bulk communication interface found")
            closeConnectionSafely(connection, null)
            return SessionOpenResult(
                error = TactrixTestResult(false, "No bulk communication interface found", -1, -1, "", "")
            )
        }

        if (!connection.claimInterface(usbInterface, true)) {
            EcuLogger.error("Failed to claim Tactrix interface")
            closeConnectionSafely(connection, usbInterface)
            return SessionOpenResult(
                error = TactrixTestResult(false, "Failed to claim Tactrix interface", -1, -1, "", "")
            )
        }

        EcuLogger.usb("Tactrix interface claimed successfully")

        val endpointOut = findBulkOutEndpoint(usbInterface)
        val endpointIn = findBulkInEndpoint(usbInterface)

        if (endpointOut == null || endpointIn == null) {
            EcuLogger.error("Bulk endpoints not found")
            closeConnectionSafely(connection, usbInterface)
            return SessionOpenResult(
                error = TactrixTestResult(false, "Bulk endpoints not found", -1, -1, "", "")
            )
        }

        EcuLogger.usb("Bulk OUT endpoint address: ${endpointOut.address}")
        EcuLogger.usb("Bulk IN endpoint address: ${endpointIn.address}")

        return SessionOpenResult(
            session = OpenPortSession(
                device = device,
                connection = connection,
                usbInterface = usbInterface,
                endpointOut = endpointOut,
                endpointIn = endpointIn
            )
        )
    }

    private fun sendAsciiCommand(
        connection: UsbDeviceConnection,
        endpointOut: UsbEndpoint,
        endpointIn: UsbEndpoint,
        commandLabel: String,
        commandString: String
    ): CommandResult {
        val packet = commandString.toByteArray(Charset.forName("US-ASCII"))

        EcuLogger.usb("Sending $commandLabel")
        EcuLogger.usb(
            "Command text: ${commandString.replace("\r", "\\r").replace("\n", "\\n")}"
        )

        return sendRawPacket(
            connection = connection,
            endpointOut = endpointOut,
            endpointIn = endpointIn,
            packetLabel = "Packet",
            packet = packet,
            noDataMessage = "No data returned",
            timeoutMessage = "Read timed out or no response from device"
        )
    }

    private fun sendSubaruSsmQuery(
        connection: UsbDeviceConnection,
        endpointOut: UsbEndpoint,
        endpointIn: UsbEndpoint
    ): CommandResult {
        val canFrame = byteArrayOf(
            0x00,
            0x00,
            0x07,
            0xE0.toByte(),
            0xA8.toByte(),
            0x00,
            0x00,
            0x00,
            0x08
        )

        EcuLogger.usb("Sending Subaru SSM raw CAN frame")
        EcuLogger.usb("CAN frame hex: ${toHex(canFrame)}")

        return sendRawPacket(
            connection = connection,
            endpointOut = endpointOut,
            endpointIn = endpointIn,
            packetLabel = "Packet",
            packet = canFrame,
            noDataMessage = "No raw response returned",
            timeoutMessage = "Read timed out after raw transmit"
        )
    }

    private fun sendRawPacket(
        connection: UsbDeviceConnection,
        endpointOut: UsbEndpoint,
        endpointIn: UsbEndpoint,
        packetLabel: String,
        packet: ByteArray,
        noDataMessage: String,
        timeoutMessage: String
    ): CommandResult {
        EcuLogger.usb("$packetLabel length: ${packet.size}")
        EcuLogger.usb("$packetLabel hex: ${toHex(packet)}")
        EcuLogger.usb("Write timeout ms: $WRITE_TIMEOUT_MS")
        EcuLogger.usb("Read timeout ms: $READ_TIMEOUT_MS")

        val sent = connection.bulkTransfer(
            endpointOut,
            packet,
            packet.size,
            WRITE_TIMEOUT_MS
        )

        EcuLogger.usb("Bytes sent: $sent")

        val buffer = ByteArray(256)
        val received = connection.bulkTransfer(
            endpointIn,
            buffer,
            buffer.size,
            READ_TIMEOUT_MS
        )

        EcuLogger.usb("Bytes received: $received")

        val responseBytes = if (received > 0) {
            buffer.copyOf(received)
        } else {
            byteArrayOf()
        }

        val responseHex = if (received > 0) {
            toHex(responseBytes)
        } else {
            ""
        }

        val responseAscii = if (received > 0) {
            String(responseBytes, Charset.forName("US-ASCII"))
        } else {
            ""
        }

        if (received > 0) {
            EcuLogger.usb("Response bytes: $responseHex")
            EcuLogger.usb("Response ascii: $responseAscii")
        } else if (received == 0) {
            EcuLogger.usb(noDataMessage)
        } else {
            EcuLogger.usb(timeoutMessage)
        }

        return CommandResult(
            bytesSent = sent,
            bytesReceived = received,
            responseHex = responseHex,
            responseAscii = responseAscii,
            responseBytes = responseBytes
        )
    }

    private fun closeConnectionSafely(
        connection: UsbDeviceConnection,
        usbInterface: UsbInterface?
    ) {
        try {
            if (usbInterface != null) {
                connection.releaseInterface(usbInterface)
            }
        } catch (_: Exception) {
        }

        try {
            connection.close()
        } catch (_: Exception) {
        }

        EcuLogger.usb("Tactrix connection closed cleanly")
    }

    private fun findBulkCommunicationInterface(device: UsbDevice): UsbInterface? {
        for (i in 0 until device.interfaceCount) {
            val usbInterface = device.getInterface(i)
            val hasBulkIn = findBulkInEndpoint(usbInterface) != null
            val hasBulkOut = findBulkOutEndpoint(usbInterface) != null
            if (hasBulkIn && hasBulkOut) {
                return usbInterface
            }
        }
        return null
    }

    private fun findBulkInEndpoint(usbInterface: UsbInterface): UsbEndpoint? {
        for (i in 0 until usbInterface.endpointCount) {
            val endpoint = usbInterface.getEndpoint(i)
            if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                endpoint.direction == UsbConstants.USB_DIR_IN
            ) {
                return endpoint
            }
        }
        return null
    }

    private fun findBulkOutEndpoint(usbInterface: UsbInterface): UsbEndpoint? {
        for (i in 0 until usbInterface.endpointCount) {
            val endpoint = usbInterface.getEndpoint(i)
            if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                endpoint.direction == UsbConstants.USB_DIR_OUT
            ) {
                return endpoint
            }
        }
        return null
    }

    private fun toHex(bytes: ByteArray): String {
        return bytes.joinToString(" ") { "%02X".format(it) }
    }
}
