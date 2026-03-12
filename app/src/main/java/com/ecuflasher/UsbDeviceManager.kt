package com.ecuflasher

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class UsbDeviceManager(private val context: Context) {

    private val usbManager: UsbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    fun listUsbDevices(): List<UsbDevice> {

        val devices = usbManager.deviceList
        val result = mutableListOf<UsbDevice>()

        for (device in devices.values) {

            EcuLogger.usb("USB Device Found")
            EcuLogger.usb("Vendor ID: ${device.vendorId}")
            EcuLogger.usb("Product ID: ${device.productId}")
            EcuLogger.usb("Device Name: ${device.deviceName}")

            result.add(device)
        }

        return result
    }
}
