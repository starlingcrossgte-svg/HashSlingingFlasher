package com.ecuflasher

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var refreshButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.usbStatusText)
        refreshButton = findViewById(R.id.refreshButton)

        refreshButton.setOnClickListener {
            checkUsbDevices()
        }

        EcuLogger.main("ECUFlasher started")
    }

    private fun checkUsbDevices() {
        val usbManager = UsbDeviceManager(this)
        val devices = usbManager.listUsbDevices()

        val tactrixDevice = devices.firstOrNull {
            it.vendorId == 1027 && it.productId == 52301
        }

        if (tactrixDevice != null) {
            val opened = usbManager.openTactrixConnection()

            statusText.text = if (opened) {
                "Tactrix OpenPort 2.0 Connected\nVID: ${tactrixDevice.vendorId}\nPID: ${tactrixDevice.productId}\nUSB channel opened"
            } else {
                "Tactrix detected but failed to open USB channel"
            }

            usbManager.logTactrixInterfaces()
        } else {
            statusText.text = "No Tactrix OpenPort Detected"
        }

        EcuLogger.main("USB device count: ${devices.size}")
    }
}
