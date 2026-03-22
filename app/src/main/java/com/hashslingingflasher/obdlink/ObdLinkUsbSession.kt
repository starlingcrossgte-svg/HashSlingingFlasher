package com.hashslingingflasher.obdlink

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import com.hoho.android.usbserial.driver.UsbSerialPort

data class ObdLinkUsbSession(
    val device: UsbDevice,
    val connection: UsbDeviceConnection,
    val port: UsbSerialPort
)
