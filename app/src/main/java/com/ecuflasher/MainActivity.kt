package com.ecuflasher

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        statusText = TextView(this)
        statusText.textSize = 20f
        setContentView(statusText)

        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList

        if (deviceList.isEmpty()) {
            statusText.text = "No USB devices connected"
            return
        }

        val device: UsbDevice = deviceList.values.first()

        statusText.text =
            "USB device connected\nVendor ID: ${device.vendorId}\nProduct ID: ${device.productId}"

        if (!usbManager.hasPermission(device)) {
            val permissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent("com.ecuflasher.USB_PERMISSION"),
                PendingIntent.FLAG_IMMUTABLE
            )
            usbManager.requestPermission(device, permissionIntent)
        }
    }
}
