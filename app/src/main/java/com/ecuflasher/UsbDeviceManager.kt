package com.ecuflasher

import android.content.Context
import android.hardware.usb.*

data class TactrixTestResult(
    val success: Boolean,
    val statusMessage: String
)

class UsbDeviceManager(private val context: Context) {

    private val TACTRIX_VENDOR_ID = 1027
    private val TACTRIX_PRODUCT_ID = 52301
    private val READ_TIMEOUT_MS = 4000
    private val WRITE_TIMEOUT_MS = 3000

    fun openTactrixChannel(): TactrixTestResult {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val tactrixDevice: UsbDevice? = usbManager.deviceList.values.firstOrNull {
            it.vendorId == TACTRIX_VENDOR_ID && it.productId == TACTRIX_PRODUCT_ID
        }

        if (tactrixDevice == null)
            return TactrixTestResult(false, "No Tactrix device detected")

        val connection: UsbDeviceConnection? = usbManager.openDevice(tactrixDevice)
        if (connection == null)
            return TactrixTestResult(false, "Failed to open Tactrix USB device (permission?)")

        val usbInterface: UsbInterface? = tactrixDevice.getInterface(0)
        if (usbInterface == null) {
            connection.close()
            return TactrixTestResult(false, "No USB interface found")
        }

        val claimed = connection.claimInterface(usbInterface, true)
        if (!claimed) {
            connection.close()
            return TactrixTestResult(false, "Failed to claim interface")
        }

        var endpointOut: UsbEndpoint? = null
        var endpointIn: UsbEndpoint? = null

        for (i in 0 until usbInterface.endpointCount) {
            val endpoint = usbInterface.getEndpoint(i)
            if (endpoint.direction == UsbConstants.USB_DIR_OUT) endpointOut = endpoint
            if (endpoint.direction == UsbConstants.USB_DIR_IN) endpointIn = endpoint
        }

        if (endpointOut == null || endpointIn == null) {
            connection.releaseInterface(usbInterface)
            connection.close()
            return TactrixTestResult(false, "Bulk endpoints not found")
        }

        connection.releaseInterface(usbInterface)
        connection.close()
        return TactrixTestResult(true, "Tactrix USB interface opened successfully")
    }
}
