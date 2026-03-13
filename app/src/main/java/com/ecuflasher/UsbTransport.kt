package com.ecuflasher

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager

class UsbTransport(private val context: Context) {

    companion object {
        const val TACTRIX_VENDOR_ID = OpenPortConstants.TACTRIX_VENDOR_ID
        const val TACTRIX_PRODUCT_ID = OpenPortConstants.TACTRIX_PRODUCT_ID
    }

    private val usbManager: UsbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    data class OpenResult(
        val device: UsbDevice,
        val connection: UsbDeviceConnection,
        val usbInterface: UsbInterface,
        val endpointOut: UsbEndpoint,
        val endpointIn: UsbEndpoint
    )

    fun openTactrix(): OpenResult? {
        val deviceList = usbManager.deviceList

        for (device in deviceList.values) {
            if (device.vendorId == OpenPortConstants.TACTRIX_VENDOR_ID &&
                device.productId == OpenPortConstants.TACTRIX_PRODUCT_ID
            ) {
                EcuLogger.usb("Opening Tactrix connection")

                val connection = usbManager.openDevice(device)
                if (connection == null) {
                    EcuLogger.error("Failed to open Tactrix USB device")
                    return null
                }

                val usbInterface = findBulkCommunicationInterface(device)
                if (usbInterface == null) {
                    EcuLogger.error("No bulk communication interface found")
                    connection.close()
                    return null
                }

                val claimed = connection.claimInterface(usbInterface, true)
                if (!claimed) {
                    EcuLogger.error("Failed to claim Tactrix interface")
                    connection.close()
                    return null
                }

                EcuLogger.usb("Tactrix interface claimed successfully")

                val endpointOut = findBulkOutEndpoint(usbInterface)
                val endpointIn = findBulkInEndpoint(usbInterface)

                if (endpointOut == null || endpointIn == null) {
                    EcuLogger.error("Bulk endpoints not found")
                    connection.releaseInterface(usbInterface)
                    connection.close()
                    return null
                }

                EcuLogger.usb("Bulk OUT endpoint address: ${endpointOut.address}")
                EcuLogger.usb("Bulk IN endpoint address: ${endpointIn.address}")

                return OpenResult(
                    device = device,
                    connection = connection,
                    usbInterface = usbInterface,
                    endpointOut = endpointOut,
                    endpointIn = endpointIn
                )
            }
        }

        EcuLogger.usb("Tactrix device not found")
        return null
    }

    fun close(openResult: OpenResult) {
        openResult.connection.releaseInterface(openResult.usbInterface)
        openResult.connection.close()
        EcuLogger.usb("Tactrix connection closed cleanly")
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
