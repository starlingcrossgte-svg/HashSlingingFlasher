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

        if (usbManager.isTactrixOpenPortConnected()) {
            statusText.text = "Tactrix OpenPort 2.0 Detected"
        } else {
            statusText.text = "No Tactrix OpenPort Detected"
        }

        val devices = usbManager.listUsbDevices()
        EcuLogger.main("USB device count: ${devices.size}")
    }
}
