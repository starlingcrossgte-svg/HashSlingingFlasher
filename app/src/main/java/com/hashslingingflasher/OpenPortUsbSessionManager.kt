package com.hashslingingflasher

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager

data class OpenPortSession(
    val device: UsbDevice,
    val connection: UsbDeviceConnection,
    val usbInterface: UsbInterface,
    val endpointOut: UsbEndpoint,
    val endpointIn: UsbEndpoint
)

data class SessionOpenResult(
    val session: OpenPortSession? = null,
    val error: TactrixTestResult? = null
)

class OpenPortUsbSessionManager(
    private val context: Context,
    private val tactrixVendorId: Int,
    private val tactrixProductId: Int
) {
    private val usbManager: UsbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    fun openSession(openLogMessage: String): SessionOpenResult {
        val device = usbManager.deviceList.values.firstOrNull {
            it.vendorId == tactrixVendorId && it.productId == tactrixProductId
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

    fun closeSession(session: OpenPortSession) {
        closeConnectionSafely(session.connection, session.usbInterface)
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
}
