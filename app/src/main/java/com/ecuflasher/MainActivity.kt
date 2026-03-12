package com.ecuflasher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        EcuLogger.main("ECUFlasher started")

        val usbManager = UsbDeviceManager(this)
        val devices = usbManager.listUsbDevices()

        EcuLogger.main("USB device count: ${devices.size}")
    }
}
