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
            if (intent?.action == ACTION_USB_PERMISSION) {
                val device: UsbDevice? =
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && device != null) {
                    EcuLogger.usb("USB permission granted")

                    val usbDeviceManager = UsbDeviceManager(this@MainActivity)
                    val opened = usbDeviceManager.openTactrixConnection()

                    statusText.text = if (opened) {
                        "Tactrix OpenPort 2.0 Connected\nVID: ${device.vendorId}\nPID: ${device.productId}\nUSB channel opened"
                    } else {
                        "Tactrix detected but failed to open USB channel"
                    }
                } else {
                    EcuLogger.usb("USB permission denied")
                    statusText.text = "USB permission denied"
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.usbStatusText)
        refreshButton = findViewById(R.id.refreshButton)

        registerReceiver(
            usbReceiver,
            IntentFilter(ACTION_USB_PERMISSION),
            Context.RECEIVER_NOT_EXPORTED
        )

        refreshButton.setOnClickListener {
            checkUsbDevices()
        }

        EcuLogger.main("ECUFlasher started")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    private fun checkUsbDevices() {
        val usbDeviceManager = UsbDeviceManager(this)
        val devices = usbDeviceManager.listUsbDevices()

        val tactrixDevice = devices.firstOrNull {
            it.vendorId == 1027 && it.productId == 52301
        }

        if (tactrixDevice != null) {
            val systemUsbManager = getSystemService(USB_SERVICE) as UsbManager

            if (!systemUsbManager.hasPermission(tactrixDevice)) {
                val permissionIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(ACTION_USB_PERMISSION),
                    PendingIntent.FLAG_IMMUTABLE
                )

                systemUsbManager.requestPermission(tactrixDevice, permissionIntent)
                statusText.text = "Requesting USB permission..."
                EcuLogger.usb("Requested USB permission for Tactrix")
                return
            }

            val opened = usbDeviceManager.openTactrixConnection()

            statusText.text = if (opened) {
                "Tactrix OpenPort 2.0 Connected\nVID: ${tactrixDevice.vendorId}\nPID: ${tactrixDevice.productId}\nUSB channel opened"
            } else {
                "Tactrix detected but failed to open USB channel"
            }

            usbDeviceManager.logTactrixInterfaces()
        } else {
            statusText.text = "No Tactrix OpenPort Detected"
        }

        EcuLogger.main("USB device count: ${devices.size}")
    }
}
