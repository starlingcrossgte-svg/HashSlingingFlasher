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

        if (devices.isEmpty()) {
            statusText.text = "No USB Device Connected"
        } else {
            statusText.text = "USB Device Detected"
        }

        EcuLogger.main("USB device count: ${devices.size}")
    }
}
