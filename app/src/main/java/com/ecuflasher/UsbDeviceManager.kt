package com.ecuflasher

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
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
        private const val READ_TIMEOUT_MS = 3000
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
                        ""
                    )
                }

                val usbInterface = findBulkCommunicationInterface(device)
                if (usbInterface == null) {
                    EcuLogger.error("No bulk communication interface found")
                    connection.close()
                    return TactrixTestResult(
                        false,
                        "No bulk communication interface found",
                        -1,
                        -1,
                        ""
                    )
                }

                val claimed = connection.claimInterface(usbInterface, true)
                if (!claimed) {
                    EcuLogger.error("Failed to claim Tactrix interface")
                    connection.close()
                    return TactrixTestResult(
                        false,
                        "Failed to claim Tactrix interface",
                        -1,
                        -1,
                        ""
                    )
                }

                EcuLogger.usb("Tactrix interface claimed successfully")

                val endpointOut = findBulkOutEndpoint(usbInterface)
                val endpointIn = findBulkInEndpoint(usbInterface)

                if (endpointOut == null || endpointIn == null) {
                    EcuLogger.error("Bulk endpoints not found")
                    connection.releaseInterface(usbInterface)
                    connection.close()
                    return TactrixTestResult(
                        false,
                        "Bulk endpoints not found",
                        -1,
                        -1,
                        ""
                    )
                }

                EcuLogger.usb("Bulk OUT endpoint address: ${endpointOut.address}")
                EcuLogger.usb("Bulk IN endpoint address: ${endpointIn.address}")

                val initResult = sendAsciiCommand(
                    connection = connection,
                    endpointOut = endpointOut,
                    endpointIn = endpointIn,
                    commandLabel = "OpenPort ATA command",
                    commandString = "ata\r\n"
                )

                if (!initResult.responseAscii.contains("aro")) {
                    connection.releaseInterface(usbInterface)
                    connection.close()
                    EcuLogger.usb("Tactrix connection closed cleanly")

                    return TactrixTestResult(
                        false,
                        "OpenPort ATA command failed before bus open",
                        initResult.bytesSent,
                        initResult.bytesReceived,
                        initResult.responseHex
                    )
                }

                val busResult = sendAsciiCommand(
                    connection = connection,
                    endpointOut = endpointOut,
                    endpointIn = endpointIn,
                    commandLabel = "OpenPort ATO command",
                    commandString = "ato4 0 10400 0\r\n"
                )

                connection.releaseInterface(usbInterface)
                connection.close()
                EcuLogger.usb("Tactrix connection closed cleanly")

                val success = busResult.responseAscii.contains("aro")

                return TactrixTestResult(
                    success = success,
                    statusMessage = if (success) {
                        "OpenPort bus open command responded"
                    } else {
                        "OpenPort bus open command sent but no valid response"
                    },
                    bytesSent = busResult.bytesSent,
                    bytesReceived = busResult.bytesReceived,
                    responseHex = busResult.responseHex
                )
            }
        }

        EcuLogger.usb("Tactrix device not found")
        return TactrixTestResult(
            false,
            "Tactrix device not detected",
            -1,
            -1,
            ""
        )
    }

    private data class CommandResult(
        val bytesSent: Int,
        val bytesReceived: Int,
        val responseHex: String,
        val responseAscii: String
    )

    private fun sendAsciiCommand(
        connection: android.hardware.usb.UsbDeviceConnection,
        endpointOut: UsbEndpoint,
        endpointIn: UsbEndpoint,
        commandLabel: String,
        commandString: String
    ): CommandResult {
        val packet = commandString.toByteArray(Charsets.US_ASCII)

        EcuLogger.usb("Sending $commandLabel")
        EcuLogger.usb("Command text: ${commandString.replace("\r", "\\r").replace("\n", "\\n")}")
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

        val responseHex = if (received > 0) {
            buffer.copyOf(received).joinToString(" ") {
                "%02X".format(it.toInt() and 0xFF)
            }
        } else {
            ""
        }

        val responseAscii = if (received > 0) {
            String(buffer, 0, received, Charsets.US_ASCII)
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
            responseAscii = responseAscii
        )
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

            if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
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

            if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                endpoint.direction == UsbConstants.USB_DIR_IN
            ) {
                return endpoint
            }
        }

        return null
    }

    private fun toHex(data: ByteArray): String {
        return data.joinToString(" ") {
            "%02X".format(it.toInt() and 0xFF)
        }
    }
}
