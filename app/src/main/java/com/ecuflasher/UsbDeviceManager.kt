package com.ecuflasher

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager

data class TactrixTestResult(
    val success: Boolean,
    val statusMessage: String,
    val bytesSent: Int,
    val bytesReceived: Int,
    val responseHex: String
)

class UsbDeviceManager(private val context: Context) {

    companion object {
        private const val TACTRIX_VENDOR_ID = 1027
        private const val TACTRIX_PRODUCT_ID = 52301
        private const val READ_TIMEOUT_MS = 4000
        private const val WRITE_TIMEOUT_MS = 3000
    }

    private data class CommandResult(
        val bytesSent: Int,
        val bytesReceived: Int,
        val responseHex: String,
        val responseAscii: String,
        val responseBytes: ByteArray
    )

    private val usbManager: UsbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    fun openTactrixChannel(): TactrixTestResult {
        val device = usbManager.deviceList.values.firstOrNull {
            it.vendorId == TACTRIX_VENDOR_ID && it.productId == TACTRIX_PRODUCT_ID
        }

        if (device == null) {
            EcuLogger.error("Tactrix USB device not found")
            return TactrixTestResult(false, "Tactrix device not found", -1, -1, "")
        }

        EcuLogger.usb("Opening Tactrix connection")

        val connection = usbManager.openDevice(device)
        if (connection == null) {
            EcuLogger.error("Failed to open Tactrix USB device")
            return TactrixTestResult(false, "Failed to open Tactrix USB device", -1, -1, "")
        }

        val usbInterface = findBulkCommunicationInterface(device)
        if (usbInterface == null) {
            EcuLogger.error("No bulk communication interface found")
            connection.close()
            return TactrixTestResult(false, "No bulk communication interface found", -1, -1, "")
        }

        val claimed = connection.claimInterface(usbInterface, true)
        if (!claimed) {
            EcuLogger.error("Failed to claim Tactrix interface")
            connection.close()
            return TactrixTestResult(false, "Failed to claim Tactrix interface", -1, -1, "")
        }

        EcuLogger.usb("Tactrix interface claimed successfully")

        val endpointOut = findBulkOutEndpoint(usbInterface)
        val endpointIn = findBulkInEndpoint(usbInterface)

        if (endpointOut == null || endpointIn == null) {
            EcuLogger.error("Bulk endpoints not found")
            connection.releaseInterface(usbInterface)
            connection.close()
            return TactrixTestResult(false, "Bulk endpoints not found", -1, -1, "")
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

        if (!ataResult.responseAscii.contains("aro")) {
            connection.releaseInterface(usbInterface)
            connection.close()
            EcuLogger.usb("Tactrix connection closed cleanly")
            return TactrixTestResult(
                false,
                "OpenPort ATA failed before ECU query",
                ataResult.bytesSent,
                ataResult.bytesReceived,
                ataResult.responseHex
            )
        }

        val atoResult = sendAsciiCommand(
            connection = connection,
            endpointOut = endpointOut,
            endpointIn = endpointIn,
            commandLabel = "OpenPort AT0 CAN command",
            commandString = "at06 0 500000 0\r\n"
        )

        if (!atoResult.responseAscii.contains("aro")) {
            connection.releaseInterface(usbInterface)
            connection.close()
            EcuLogger.usb("Tactrix connection closed cleanly")
            return TactrixTestResult(
                false,
                "OpenPort CAN bus open failed before ECU query",
                atoResult.bytesSent,
                atoResult.bytesReceived,
                atoResult.responseHex
            )
        }

        val ecuResult = sendObdCanQuery(
            connection = connection,
            endpointOut = endpointOut,
            endpointIn = endpointIn
        )

        connection.releaseInterface(usbInterface)
        connection.close()
        EcuLogger.usb("Tactrix connection closed cleanly")

        return if (ecuResult.bytesReceived > 0) {
            TactrixTestResult(
                true,
                "OpenPort response received",
                ecuResult.bytesSent,
                ecuResult.bytesReceived,
                ecuResult.responseHex
            )
        } else {
            TactrixTestResult(
                false,
                "No ECU response received",
                ecuResult.bytesSent,
                ecuResult.bytesReceived,
                ecuResult.responseHex
            )
        }
    }

    fun sendManualAsciiCommand(commandString: String): TactrixTestResult {
        val device = usbManager.deviceList.values.firstOrNull {
            it.vendorId == TACTRIX_VENDOR_ID && it.productId == TACTRIX_PRODUCT_ID
        }

        if (device == null) {
            EcuLogger.error("Tactrix USB device not found")
            return TactrixTestResult(false, "Tactrix device not found", -1, -1, "")
        }

        if (!usbManager.hasPermission(device)) {
            EcuLogger.error("USB permission not granted for Tactrix")
            return TactrixTestResult(false, "USB permission not granted", -1, -1, "")
        }

        EcuLogger.usb("Opening Tactrix connection for manual command")

        val connection = usbManager.openDevice(device)
        if (connection == null) {
            EcuLogger.error("Failed to open Tactrix USB device")
            return TactrixTestResult(false, "Failed to open Tactrix USB device", -1, -1, "")
        }

        val usbInterface = findBulkCommunicationInterface(device)
        if (usbInterface == null) {
            EcuLogger.error("No bulk communication interface found")
            connection.close()
            return TactrixTestResult(false, "No bulk communication interface found", -1, -1, "")
        }

        val claimed = connection.claimInterface(usbInterface, true)
        if (!claimed) {
            EcuLogger.error("Failed to claim Tactrix interface")
            connection.close()
            return TactrixTestResult(false, "Failed to claim Tactrix interface", -1, -1, "")
        }

        val endpointOut = findBulkOutEndpoint(usbInterface)
        val endpointIn = findBulkInEndpoint(usbInterface)

        if (endpointOut == null || endpointIn == null) {
            EcuLogger.error("Bulk endpoints not found")
            connection.releaseInterface(usbInterface)
            connection.close()
            return TactrixTestResult(false, "Bulk endpoints not found", -1, -1, "")
        }

        val normalizedCommand = if (commandString.endsWith("\r\n")) {
            commandString
        } else {
            "$commandString\r\n"
        }

        val result = sendAsciiCommand(
            connection = connection,
            endpointOut = endpointOut,
            endpointIn = endpointIn,
            commandLabel = "Manual OpenPort command",
            commandString = normalizedCommand
        )

        connection.releaseInterface(usbInterface)
        connection.close()
        EcuLogger.usb("Tactrix manual command connection closed cleanly")

        return TactrixTestResult(
            success = result.bytesReceived >= 0,
            statusMessage = if (result.bytesReceived > 0) {
                "Manual command response received"
            } else if (result.bytesReceived == 0) {
                "Manual command sent, no data returned"
            } else {
                "Manual command read timed out"
            },
            bytesSent = result.bytesSent,
            bytesReceived = result.bytesReceived,
            responseHex = result.responseHex
        )
    }

    private fun sendAsciiCommand(
        connection: UsbDeviceConnection,
        endpointOut: UsbEndpoint,
        endpointIn: UsbEndpoint,
        commandLabel: String,
        commandString: String
    ): CommandResult {
        val packet = commandString.toByteArray(Charsets.US_ASCII)

        EcuLogger.usb("Sending $commandLabel")
        EcuLogger.usb(
            "Command text: ${
                commandString.replace("\r", "\\r").replace("\n", "\\n")
            }"
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

        val buffer = ByteArray(128)
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
            String(responseBytes, Charsets.US_ASCII)
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

    private fun sendObdCanQuery(
        connection: UsbDeviceConnection,
        endpointOut: UsbEndpoint,
        endpointIn: UsbEndpoint
    ): CommandResult {
        val canFrame = byteArrayOf(
            0x00, 0x00, 0x07, 0xDF.toByte(),
            0x02, 0x01, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00
        )

        val header = "att6 ${canFrame.size} 0\r\n".toByteArray(Charsets.US_ASCII)
        val packet = ByteArray(header.size + canFrame.size)

        System.arraycopy(header, 0, packet, 0, header.size)
        System.arraycopy(canFrame, 0, packet, header.size, canFrame.size)

        EcuLogger.usb("Sending ECU OBD CAN query")
        EcuLogger.usb("Command text: att6 ${canFrame.size} 0\\r\\n + raw CAN frame")
        EcuLogger.usb("CAN frame hex: ${toHex(canFrame)}")
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

        val buffer = ByteArray(128)
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
            String(responseBytes, Charsets.US_ASCII)
        } else {
            ""
        }

        if (received > 0) {
            EcuLogger.usb("Response bytes: $responseHex")
            EcuLogger.usb("Response ascii: $responseAscii")
        } else if (received == 0) {
            EcuLogger.usb("No data returned")
        } else {
            EcuLogger.usb("Read timed out or no ECU response")
        }

        return CommandResult(
            bytesSent = sent,
            bytesReceived = received,
            responseHex = responseHex,
            responseAscii = responseAscii,
            responseBytes = responseBytes
        )
    }

    private fun containsSequence(data: ByteArray, pattern: ByteArray): Boolean {
        if (pattern.isEmpty() || data.size < pattern.size) return false

        for (i in 0..data.size - pattern.size) {
            var match = true
            for (j in pattern.indices) {
                if (data[i + j] != pattern[j]) {
                    match = false
                    break
                }
            }
            if (match) return true
        }

        return false
    }

    private fun findBulkCommunicationInterface(device: UsbDevice): UsbInterface? {
        for (i in 0 until device.interfaceCount) {
            val usbInterface = device.getInterface(i)

            var hasBulkIn = false
            var hasBulkOut = false

            for (j in 0 until usbInterface.endpointCount) {
                val endpoint = usbInterface.getEndpoint(j)
                if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (endpoint.direction == UsbConstants.USB_DIR_IN) {
                        hasBulkIn = true
                    } else if (endpoint.direction == UsbConstants.USB_DIR_OUT) {
                        hasBulkOut = true
                    }
                }
            }

            if (hasBulkIn && hasBulkOut) {
                return usbInterface
            }
        }

        return null
    }

    private fun findBulkOutEndpoint(usbInterface: UsbInterface): UsbEndpoint? {
        for (i in 0 until usbInterface.endpointCount) {
            val endpoint = usbInterface.getEndpoint(i)
            if (
                endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                endpoint.direction == UsbConstants.USB_DIR_OUT
            ) {
                return endpoint
            }
        }

        return null
    }

    private fun findBulkInEndpoint(usbInterface: UsbInterface): UsbEndpoint? {
        for (i in 0 until usbInterface.endpointCount) {
            val endpoint = usbInterface.getEndpoint(i)
            if (
                endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                endpoint.direction == UsbConstants.USB_DIR_IN
            ) {
                return endpoint
            }
        }

        return null
    }

    private fun toHex(data: ByteArray): String {
        return data.joinToString(" ") { "%02X".format(it) }
    }
}
