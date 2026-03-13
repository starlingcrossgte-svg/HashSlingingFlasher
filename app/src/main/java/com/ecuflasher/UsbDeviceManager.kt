package com.ecuflasher

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager

class UsbDeviceManager(private val context: Context) {

    companion object {
        private const val TACTRIX_VENDOR_ID = 1027
        private const val TACTRIX_PRODUCT_ID = 52301
    }

    private val usbManager: UsbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    fun openTactrixChannel(): Boolean {
        val deviceList = usbManager.deviceList

        for (device in deviceList.values) {
            if (device.vendorId == TACTRIX_VENDOR_ID &&
                device.productId == TACTRIX_PRODUCT_ID) {

                EcuLogger.usb("Opening Tactrix connection")

                val connection = usbManager.openDevice(device)
                if (connection == null) {
                    EcuLogger.error("Failed to open Tactrix USB device")
                    return false
                }

                val usbInterface = findBulkCommunicationInterface(device)
                if (usbInterface == null) {
                    EcuLogger.error("No bulk communication interface found")
                    connection.close()
                    return false
                }

                val claimed = connection.claimInterface(usbInterface, true)
                if (!claimed) {
                    EcuLogger.error("Failed to claim Tactrix interface")
                    connection.close()
                    return false
                }

                EcuLogger.usb("Tactrix interface claimed successfully")

                val endpointOut = findBulkOutEndpoint(usbInterface)
                val endpointIn = findBulkInEndpoint(usbInterface)

                if (endpointOut == null) {
                    EcuLogger.error("Bulk OUT endpoint not found")
                    connection.releaseInterface(usbInterface)
                    connection.close()
                    return false
                }

                if (endpointIn == null) {
                    EcuLogger.error("Bulk IN endpoint not found")
                    connection.releaseInterface(usbInterface)
                    connection.close()
                    return false
                }

                EcuLogger.usb("Bulk OUT endpoint address: ${endpointOut.address}")
                EcuLogger.usb("Bulk IN endpoint address: ${endpointIn.address}")

                val testPacket = byteArrayOf(0x00)
                EcuLogger.usb("Sending test packet")

                val sent = connection.bulkTransfer(
                    endpointOut,
                    testPacket,
                    testPacket.size,
                    1000
                )

                EcuLogger.usb("Bytes sent: $sent")

                val buffer = ByteArray(64)
                val received = connection.bulkTransfer(
                    endpointIn,
                    buffer,
                    buffer.size,
                    1000
                )

                EcuLogger.usb("Bytes received: $received")

                if (received > 0) {
                    val hex = buffer
                        .copyOf(received)
                        .joinToString(" ") { byte -> "%02X".format(byte.toInt() and 0xFF) }
                    EcuLogger.usb("Response bytes: $hex")
                } else if (received == 0) {
                    EcuLogger.usb("No data returned")
                } else {
                    EcuLogger.usb("Read timed out or no response from device")
                }

                connection.releaseInterface(usbInterface)
                connection.close()
                EcuLogger.usb("Tactrix connection closed cleanly")

                return sent > 0
            }
        }

        EcuLogger.usb("Tactrix device not found")
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
