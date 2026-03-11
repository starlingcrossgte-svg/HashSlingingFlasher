package com.ecuflasher

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var usbStatusText: TextView
    private val ACTION_USB_PERMISSION = "com.ecuflasher.USB_PERMISSION"

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when (it.action) {
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                        val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        device?.let { d ->
                            usbStatusText.text = "USB Connected\nVendor ID: ${d.vendorId}\nProduct ID: ${d.productId}"
                        }
                    }
                    UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                        usbStatusText.text = "USB Disconnected"
                    }
                    ACTION_USB_PERMISSION -> {
                        val granted = it.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        usbStatusText.text = if (granted) "Permission Granted" else "Permission Denied"
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usbStatusText = findViewById(R.id.usbStatusText)

        // Register USB attach/detach receiver
        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)

        // Check if any USB device is already connected
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList
        if (deviceList.isNotEmpty()) {
            val device = deviceList.values.first()
            usbStatusText.text = "USB Connected\nVendor ID: ${device.vendorId}\nProduct ID: ${device.productId}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }
}
