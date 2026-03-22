package com.hashslingingflasher.obdlink

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class ObdLinkUsbDiscovery(
    private val usbManager: UsbManager
) {

    fun listAttachedDevices(): List<ObdLinkUsbDeviceInfo> {
        return usbManager.deviceList.values.map { device ->
            ObdLinkUsbDeviceInfo(
                vendorId = device.vendorId,
                productId = device.productId,
                deviceName = device.deviceName ?: "",
                manufacturerName = device.manufacturerName ?: "",
                productName = device.productName ?: ""
            )
        }.sortedWith(
            compareBy<ObdLinkUsbDeviceInfo> { it.vendorId }
                .thenBy { it.productId }
                .thenBy { it.deviceName }
        )
    }

    fun findDeviceByName(deviceName: String): UsbDevice? {
        return usbManager.deviceList.values.firstOrNull { device ->
            (device.deviceName ?: "") == deviceName
        }
    }
}
