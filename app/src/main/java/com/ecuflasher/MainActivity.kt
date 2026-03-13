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

                    val manager = UsbDeviceManager(this@MainActivity)
                    val result = manager.openTactrixChannel()

                    statusText.text = if (result) {
                        "Tactrix communication test complete"
                    } else {
                        "Tactrix communication test failed"
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
            checkTactrix()
        }

        EcuLogger.main("ECUFlasher started")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    private fun checkTactrix() {
        val systemUsbManager = getSystemService(USB_SERVICE) as UsbManager
        val tactrixDevice = systemUsbManager.deviceList.values.firstOrNull {
            it.vendorId == 1027 && it.productId == 52301
        }

        if (tactrixDevice == null) {
            EcuLogger.usb("Tactrix device not found")
            statusText.text = "Tactrix device not detected"
            return
        }

        if (!systemUsbManager.hasPermission(tactrixDevice)) {
            val permissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_IMMUTABLE
            )

            systemUsbManager.requestPermission(tactrixDevice, permissionIntent)
            EcuLogger.usb("Requested USB permission for Tactrix")
            statusText.text = "Requesting USB permission..."
            return
        }

        val manager = UsbDeviceManager(this)
        val result = manager.openTactrixChannel()

        statusText.text = if (result) {
            "Tactrix communication test complete"
        } else {
            "Tactrix communication test failed"
        }
    }
}
