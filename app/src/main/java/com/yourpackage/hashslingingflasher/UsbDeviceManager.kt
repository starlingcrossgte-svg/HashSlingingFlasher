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
        val deviceList = usbManager.deviceList

        for (device in deviceList.values) {
            if (device.vendorId == TACTRIX_VENDOR_ID &&
                device.productId == TACTRIX_PRODUCT_ID
            ) {
                EcuLogger.usb("Opening Tactrix connection")

                val connection = usbManager.openDevice(device)
                if (connection == null) {
                    EcuLogger.error("Failed to open Tactrix USB device")
                    return TactrixTestResult(
                        false,
                        "Failed to open Tactrix USB device",
                        -1,
                        -1,
                        "",
                        ""
                    )
                }

                val usbInterface = findBulkCommunicationInterface(device)
                if (usbInterface == null) {
                    EcuLogger.error("No bulk communication interface found")
                    closeConnectionSafely(connection, null)
                    return TactrixTestResult(
                        false,
                        "No bulk communication interface found",
                        -1,
                        -1,
                        "",
                        ""
                    )
                }

                if (!connection.claimInterface(usbInterface, true)) {
                    EcuLogger.error("Failed to claim Tactrix interface")
                    closeConnectionSafely(connection, usbInterface)
                    return TactrixTestResult(
                        false,
                        "Failed to claim Tactrix interface",
                        -1,
                        -1,
                        "",
                        ""
                    )
                }

                EcuLogger.usb("Tactrix interface claimed successfully")

                val endpointOut = findBulkOutEndpoint(usbInterface)
                val endpointIn = findBulkInEndpoint(usbInterface)

                if (endpointOut == null || endpointIn == null) {
                    EcuLogger.error("Bulk endpoints not found")
                    closeConnectionSafely(connection, usbInterface)
                    return TactrixTestResult(
                        false,
                        "Bulk endpoints not found",
                        -1,
                        -1,
                        "",
                        ""
                    )
                }

                EcuLogger.usb("Bulk OUT endpoint address: ${endpointOut.address}")
                EcuLogger.usb("Bulk IN endpoint address: ${endpointIn.address}")

                val ataResult = sendAsciiCommand(
                    connection = connection,
                    endpointOut = endpointOut,
                    endpointIn = endpointIn,
                    commandLabel = "OpenPort ATA command",
                    commandString = "ata\r\n"
                )

                if (!ataResult.responseAscii.contains("aro", ignoreCase = true)) {
                    closeConnectionSafely(connection, usbInterface)
                    return TactrixTestResult(
                        false,
                        "OpenPort ATA failed before ECU query",
                        ataResult.bytesSent,
                        ataResult.bytesReceived,
                        ataResult.responseHex,
                        ataResult.responseAscii
                    )
                }

                val atoResult = sendAsciiCommand(
                    connection = connection,
                    endpointOut = endpointOut,
                    endpointIn = endpointIn,
                    commandLabel = "OpenPort ATO CAN command",
                    commandString = "ato6 0 500000 0\r\n"
                )

                if (!atoResult.responseAscii.contains("aro", ignoreCase = true)) {
                    closeConnectionSafely(connection, usbInterface)
                    return TactrixTestResult(
                        false,
                        "OpenPort CAN bus open failed before ECU query",
                        atoResult.bytesSent,
                        atoResult.bytesReceived,
                        atoResult.responseHex,
                        atoResult.responseAscii
                    )
                }

                val ecuResult = sendObdRawQuery(
                    connection = connection,
                    endpointOut = endpointOut,
                    endpointIn = endpointIn
                )

                closeConnectionSafely(connection, usbInterface)

                val success = containsSequence(
                    ecuResult.responseBytes,
                    byteArrayOf(0x41, 0x00)
                )

                return TactrixTestResult(
                    success = success || ecuResult.bytesReceived > 0,
                    statusMessage = if (success) {
                        "ECU response received"
                    } else if (ecuResult.bytesReceived > 0) {
                        "OpenPort response received"
                    } else {
                        "No response from ECU"
                    },
                    bytesSent = ecuResult.bytesSent,
                    bytesReceived = ecuResult.bytesReceived,
                    responseHex = ecuResult.responseHex,
                    responseAscii = ecuResult.responseAscii
                )
            }
        }

        EcuLogger.usb("Tactrix device not found")
        return TactrixTestResult(false, "Tactrix device not detected", -1, -1, "", "")
    }

    fun sendCustomAsciiCommand(command: String): TactrixTestResult {
        val device = usbManager.deviceList.values.firstOrNull {
            it.vendorId == TACTRIX_VENDOR_ID && it.productId == TACTRIX_PRODUCT_ID
        } ?: return TactrixTestResult(false, "Tactrix device not detected", -1, -1, "", "")

        EcuLogger.usb("Opening Tactrix connection for manual command")

        val connection = usbManager.openDevice(device)
            ?: return TactrixTestResult(false, "Failed to open Tactrix USB device", -1, -1, "", "")

        val usbInterface = findBulkCommunicationInterface(device)
        if (usbInterface == null) {
            closeConnectionSafely(connection, null)
            return TactrixTestResult(false, "No bulk communication interface found", -1, -1, "", "")
        }

        if (!connection.claimInterface(usbInterface, true)) {
            closeConnectionSafely(connection, usbInterface)
            return TactrixTestResult(false, "Failed to claim Tactrix interface", -1, -1, "", "")
        }

        val endpointOut = findBulkOutEndpoint(usbInterface)
        val endpointIn = findBulkInEndpoint(usbInterface)

        if (endpointOut == null || endpointIn == null) {
            closeConnectionSafely(connection, usbInterface)
            return TactrixTestResult(false, "Bulk endpoints not found", -1, -1, "", "")
        }

        EcuLogger.usb("Bulk OUT endpoint address: ${endpointOut.address}")
        EcuLogger.usb("Bulk IN endpoint address: ${endpointIn.address}")

        val wakeResult = sendAsciiCommand(
            connection = connection,
            endpointOut = endpointOut,
            endpointIn = endpointIn,
            commandLabel = "Manual command ATA wake",
            commandString = "ata\r\n"
        )

        if (!wakeResult.responseAscii.contains("aro", ignoreCase = true)) {
            closeConnectionSafely(connection, usbInterface)
            return TactrixTestResult(
                false,
                "OpenPort did not respond to ATA wake",
                wakeResult.bytesSent,
                wakeResult.bytesReceived,
                wakeResult.responseHex,
                wakeResult.responseAscii
            )
        }

        val normalizedCommand = if (command.endsWith("\r\n")) {
            command
        } else {
            "$command\r\n"
        }

        val result = sendAsciiCommand(
            connection = connection,
            endpointOut = endpointOut,
            endpointIn = endpointIn,
            commandLabel = "Manual OpenPort command",
            commandString = normalizedCommand
        )

        closeConnectionSafely(connection, usbInterface)

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

    private data class CommandResult(
        val bytesSent: Int,
        val bytesReceived: Int,
        val responseHex: String,
        val responseAscii: String,
        val responseBytes: ByteArray
    )

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
        EcuLogger.usb("Packet length: ${packet.size}")
        EcuLogger.usb("Packet hex: ${toHex(packet)}")
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
            EcuLogger.usb("No data returned")
        } else {
            EcuLogger.usb("Read timed out or no response from device")
        }

        return CommandResult(
            bytesSent = sent,
            bytesReceived = received,
            responseHex = responseHex,
            responseAscii = responseAscii,
            responseBytes = responseBytes
        )
    }

    private fun sendObdRawQuery(
        connection: UsbDeviceConnection,
        endpointOut: UsbEndpoint,
        endpointIn: UsbEndpoint
    ): CommandResult {
        val canFrame = byteArrayOf(
            0x00,
            0x00,
            0x07,
            0xDF.toByte(),
            0x02,
            0x01,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00
        )

        val headerResult = sendAsciiCommand(
            connection = connection,
            endpointOut = endpointOut,
            endpointIn = endpointIn,
            commandLabel = "ECU query header command",
            commandString = "atg ${canFrame.size}\r\n"
        )

        if (headerResult.bytesReceived < 0) {
            return CommandResult(
                bytesSent = headerResult.bytesSent,
                bytesReceived = headerResult.bytesReceived,
                responseHex = headerResult.responseHex,
                responseAscii = headerResult.responseAscii,
                responseBytes = headerResult.responseBytes
            )
        }

        EcuLogger.usb("Sending ECU OBD raw CAN frame")
        EcuLogger.usb("CAN frame hex: ${toHex(canFrame)}")
        EcuLogger.usb("Packet length: ${canFrame.size}")
        EcuLogger.usb("Write timeout ms: $WRITE_TIMEOUT_MS")
        EcuLogger.usb("Read timeout ms: $READ_TIMEOUT_MS")

        val sent = connection.bulkTransfer(
            endpointOut,
            canFrame,
            canFrame.size,
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
            EcuLogger.usb("No ECU data returned")
        } else {
            EcuLogger.usb("Read timed out or ECU did not respond")
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

    private fun containsSequence(data: ByteArray, target: ByteArray): Boolean {
        if (target.isEmpty() || data.size < target.size) return false

        for (i in 0..data.size - target.size) {
            var match = true
            for (j in target.indices) {
                if (data[i + j] != target[j]) {
                    match = false
                    break
                }
            }
            if (match) return true
        }

        return false
    }
}
