package com.ecuflasher

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

        // Initial check on start
        checkUsbConnection()

        // Register USB attach/detach receiver
        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(usbReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
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
        // Inline UI update without refreshing activity
        usbStatusText.text = when (status) {
            UsbStatus.CONNECTED -> "USB Device Connected"
            UsbStatus.DISCONNECTED -> "No USB Device Connected"
            UsbStatus.UNKNOWN -> "USB Status Unknown"
        }
    }

    // BroadcastReceiver to handle USB attach/detach live
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> updateUsbStatus(UsbStatus.CONNECTED)
                UsbManager.ACTION_USB_DEVICE_DETACHED -> updateUsbStatus(UsbStatus.DISCONNECTED)
            }
        }
    }
}
