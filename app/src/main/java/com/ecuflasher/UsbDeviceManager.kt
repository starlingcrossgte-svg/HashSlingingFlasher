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
        private const val READ_TIMEOUT_MS = 1000
        private const val WRITE_TIMEOUT_MS = 1000
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
                        success = false,
                        statusMessage = "Failed to open Tactrix USB device",
                        bytesSent = -1,
                        bytesReceived = -1,
                        responseHex = ""
                    )
                }

                val usbInterface = findBulkCommunicationInterface(device)
                if (usbInterface == null) {
                    EcuLogger.error("No bulk communication interface found")
                    connection.close()
                    return TactrixTestResult(
                        success = false,
                        statusMessage = "No bulk communication interface found",
                        bytesSent = -1,
                        bytesReceived = -1,
                        responseHex = ""
                    )
                }

                val claimed = connection.claimInterface(usbInterface, true)
                if (!claimed) {
                    EcuLogger.error("Failed to claim Tactrix interface")
                    connection.close()
                    return TactrixTestResult(
                        success = false,
                        statusMessage = "Failed to claim Tactrix interface",
                        bytesSent = -1,
                        bytesReceived = -1,
                        responseHex = ""
                    )
                }

                EcuLogger.usb("Tactrix interface claimed successfully")

                val endpointOut = findBulkOutEndpoint(usbInterface)
                val endpointIn = findBulkInEndpoint(usbInterface)

                if (endpointOut == null) {
                    EcuLogger.error("Bulk OUT endpoint not found")
                    connection.releaseInterface(usbInterface)
                    connection.close()
                    return TactrixTestResult(
                        success = false,
                        statusMessage = "Bulk OUT endpoint not found",
                        bytesSent = -1,
                        bytesReceived = -1,
                        responseHex = ""
                    )
                }

                if (endpointIn == null) {
                    EcuLogger.error("Bulk IN endpoint not found")
                    connection.releaseInterface(usbInterface)
                    connection.close()
                    return TactrixTestResult(
                        success = false,
                        statusMessage = "Bulk IN endpoint not found",
                        bytesSent = -1,
                        bytesReceived = -1,
                        responseHex = ""
                    )
                }

                EcuLogger.usb("Bulk OUT endpoint address: ${endpointOut.address}")
                EcuLogger.usb("Bulk IN endpoint address: ${endpointIn.address}")

                val testPacket = buildTransportTestPacket()
                EcuLogger.usb("Sending transport test packet")
                EcuLogger.usb("Test packet length: ${testPacket.size}")
                EcuLogger.usb("Test packet hex: ${toHex(testPacket)}")
                EcuLogger.usb("Write timeout ms: $WRITE_TIMEOUT_MS")
                EcuLogger.usb("Read timeout ms: $READ_TIMEOUT_MS")

                val sent = connection.bulkTransfer(
                    endpointOut,
                    testPacket,
                    testPacket.size,
                    WRITE_TIMEOUT_MS
                )

                EcuLogger.usb("Bytes sent: $sent")

                val buffer = ByteArray(64)
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

                if (received > 0) {
                    EcuLogger.usb("Response bytes: $responseHex")
                } else if (received == 0) {
                    EcuLogger.usb("No data returned")
                } else {
                    EcuLogger.usb("Read timed out or no response from device")
                }

                connection.releaseInterface(usbInterface)
                connection.close()
                EcuLogger.usb("Tactrix connection closed cleanly")

                return TactrixTestResult(
                    success = sent > 0,
                    statusMessage = if (sent > 0) {
                        "Tactrix communication test complete"
                    } else {
                        "Tactrix communication test failed"
                    },
                    bytesSent = sent,
                    bytesReceived = received,
                    responseHex = responseHex
                )
            }
        }

        EcuLogger.usb("Tactrix device not found")
        return TactrixTestResult(
            success = false,
            statusMessage = "Tactrix device not detected",
            bytesSent = -1,
            bytesReceived = -1,
            responseHex = ""
        )
    }

    private fun buildTransportTestPacket(): ByteArray {
        return byteArrayOf(0x00)
    }

    private fun toHex(data: ByteArray): String {
        return data.joinToString(" ") {
            "%02X".format(it.toInt() and 0xFF)
        }
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
}
