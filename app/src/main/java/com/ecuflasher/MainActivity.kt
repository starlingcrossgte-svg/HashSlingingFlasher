package com.ecuflasher

import android.app.Activity
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

enum class UsbStatus {
    CONNECTED,
    DISCONNECTED,
    UNKNOWN
}

class MainActivity : AppCompatActivity() {

    private lateinit var usbStatusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usbStatusText = findViewById(R.id.usbStatusText)

        checkUsbConnection()
    }

    private fun checkUsbConnection() {
        val usbManager = getSystemService(Activity.USB_SERVICE) as UsbManager
        val connectedDevices: HashMap<String, UsbDevice> = usbManager.deviceList

        val status = if (connectedDevices.isNotEmpty()) {
            UsbStatus.CONNECTED
        } else {
            UsbStatus.DISCONNECTED
        }

        updateUsbStatus(status)
    }

    private fun updateUsbStatus(status: UsbStatus) {
        when (status) {
            UsbStatus.CONNECTED -> handleConnected()
            UsbStatus.DISCONNECTED -> handleDisconnected()
            else -> handleUnknown()
        }
    }

    private fun handleConnected() {
        usbStatusText.text = "USB Device Connected"
        // Additional logic for connected device
    }

    private fun handleDisconnected() {
        usbStatusText.text = "No USB Device Connected"
        // Additional logic for disconnected device
    }

    private fun handleUnknown() {
        usbStatusText.text = "USB Status Unknown"
        // Optional fallback logic
    }
}
