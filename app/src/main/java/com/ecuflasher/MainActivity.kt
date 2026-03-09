package com.ecuflasher

import android.app.PendingIntent
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var usbManager: UsbManager
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        usbManager = getSystemService(USB_SERVICE) as UsbManager

        detectUsbDevices()
    }

    private fun detectUsbDevices() {
        val deviceList = usbManager.deviceList

        if (deviceList.isEmpty()) {
            statusText.text = "No USB devices detected"
        } else {
            val device = deviceList.values.first()
            statusText.text =
                "USB Device Detected\nVendor ID: ${device.vendorId}\nProduct ID: ${device.productId}"
        }
    }
}
