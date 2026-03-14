package com.ecuflasher

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val ACTION_USB_PERMISSION = "com.ecuflasher.USB_PERMISSION"

    private lateinit var statusText: TextView
    private lateinit var refreshButton: Button

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_USB_PERMISSION) return

            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            val usbManager = context?.getSystemService(Context.USB_SERVICE) as UsbManager
            val tDevice = usbManager.deviceList.values.firstOrNull { 
                it.vendorId == 1027 && it.productId == 52301 
            }

            if (granted && tDevice != null) {
                statusText.text = "USB permission granted"
            } else {
                statusText.text = "USB permission denied"
            }
        }
    }

    private fun requestUsbPermission() {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val tDevice = usbManager.deviceList.values.firstOrNull {
            it.vendorId == 1027 && it.productId == 52301
        } ?: run {
            statusText.text = "Device not detected"
            return
        }

        if (!usbManager.hasPermission(tDevice)) {
            val permissionIntent = PendingIntent.getBroadcast(
                this, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
            )
            usbManager.requestPermission(tDevice, permissionIntent)
            statusText.text = "Requesting USB permission..."
        } else {
            statusText.text = "USB permission already granted"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusMessageText)
        refreshButton = findViewById(R.id.refreshButton)

        refreshButton.setOnClickListener {
            requestUsbPermission()
        }

        registerReceiver(usbReceiver, IntentFilter(ACTION_USB_PERMISSION))
    }
}
