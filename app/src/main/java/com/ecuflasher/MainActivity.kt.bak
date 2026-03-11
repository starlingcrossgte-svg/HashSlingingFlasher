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

        // Check USB when the activity starts
        checkUsbConnection()
    }

    private fun checkUsbConnection() {
        val usbManager = getSystemService(Activity.USB_SERVICE) as UsbManager
        val deviceList: HashMap<String, UsbDevice> = usbManager.deviceList

        val status = if (deviceList.isNotEmpty()) {
            UsbStatus.CONNECTED
        } else {
            UsbStatus.DISCONNECTED
        }

        updateUsbStatus(status)
    }

    private fun updateUsbStatus(status: UsbStatus) {
        // Inline UI update without refreshing the activity
        usbStatusText.text = when (status) {
            UsbStatus.CONNECTED -> "USB Device Connected"
            UsbStatus.DISCONNECTED -> "No USB Device Connected"
            UsbStatus.UNKNOWN -> "USB Status Unknown"
        }
    }
}
