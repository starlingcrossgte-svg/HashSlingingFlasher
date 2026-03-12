package com.ecuflasher

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class UsbDeviceManager(private val context: Context) {

    companion object {
        private const val TACTRIX_VENDOR_ID = 1027
        private const val TACTRIX_PRODUCT_ID = 52301
    }

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

    fun isTactrixOpenPortConnected(): Boolean {
        val devices = usbManager.deviceList

        for (device in devices.values) {
            if (device.vendorId == TACTRIX_VENDOR_ID && device.productId == TACTRIX_PRODUCT_ID) {
                EcuLogger.usb("Tactrix OpenPort 2.0 detected")
                return true
            }
        }

        EcuLogger.usb("Tactrix OpenPort 2.0 not detected")
        return false
    }

    fun logTactrixInterfaces() {
        val devices = usbManager.deviceList

        for (device in devices.values) {
            if (device.vendorId == TACTRIX_VENDOR_ID && device.productId == TACTRIX_PRODUCT_ID) {
                EcuLogger.usb("Inspecting Tactrix interfaces")
                EcuLogger.usb("Interface count: ${device.interfaceCount}")

                for (i in 0 until device.interfaceCount) {
                    val usbInterface = device.getInterface(i)

                    EcuLogger.usb("Interface $i")
                    EcuLogger.usb("Interface ID: ${usbInterface.id}")
                    EcuLogger.usb("Endpoint count: ${usbInterface.endpointCount}")

                    for (j in 0 until usbInterface.endpointCount) {
                        val endpoint = usbInterface.getEndpoint(j)

                        val direction = when (endpoint.direction) {
                            UsbConstants.USB_DIR_IN -> "IN"
                            UsbConstants.USB_DIR_OUT -> "OUT"
                            else -> "UNKNOWN"
                        }

                        EcuLogger.usb("Endpoint $j")
                        EcuLogger.usb("Endpoint address: ${endpoint.address}")
                        EcuLogger.usb("Endpoint direction: $direction")
                        EcuLogger.usb("Endpoint type: ${endpoint.type}")
                        EcuLogger.usb("Endpoint max packet size: ${endpoint.maxPacketSize}")
                    }
                }

                return
            }
        }

        EcuLogger.usb("No Tactrix device available for interface inspection")
    }
}
