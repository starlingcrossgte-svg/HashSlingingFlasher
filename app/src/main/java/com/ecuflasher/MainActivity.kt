package com.ecuflasher

import android.app.Activity
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var usbManager: UsbManager
    private lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById(R.id.statusTextView)
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        checkConnectedUsbDevices()
    }

    private fun checkConnectedUsbDevices() {
        val deviceList: HashMap<String, UsbDevice> = usbManager.deviceList
        if (deviceList.isEmpty()) {
            statusTextView.text = "No USB devices connected"
            Toast.makeText(this, "No USB devices detected", Toast.LENGTH_SHORT).show()
            return
        }

        for ((_, device) in deviceList) {
            val vendorId = device.vendorId
            val productId = device.productId
            statusTextView.text = "USB device connected\nVendor ID: $vendorId\nProduct ID: $productId"
            Toast.makeText(this, "USB device detected: Vendor $vendorId, Product $productId", Toast.LENGTH_LONG).show()

            // Here you can add extra logic to identify your Tactrix device
            if (vendorId == 0x1027 && productId == 0xCC01) { // Example IDs, adjust to your device
                statusTextView.append("\nTactrix OpenPort detected!")
            }
        }
    }
}
