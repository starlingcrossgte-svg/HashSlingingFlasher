package com.ecuflasher

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val ACTION_USB_PERMISSION = "com.ecuflasher.USB_PERMISSION"
    private val TACTRIX_VENDOR_ID = 1027
    private val TACTRIX_PRODUCT_ID = 52301

    private lateinit var statusText: TextView
    private lateinit var refreshButton: Button

    private lateinit var lastCommandText: TextView
    private lateinit var bytesSentText: TextView
    private lateinit var bytesReceivedText: TextView
    private lateinit var responseHexText: TextView
    private lateinit var manualCommandInput: EditText
    private lateinit var sendManualCommandButton: Button
    private lateinit var manualCommandResponseText: TextView

    private lateinit var persistentSession: UsbPersistentSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusMessageText)
        refreshButton = findViewById(R.id.refreshButton)

        lastCommandText = findViewById(R.id.lastCommandText)
        bytesSentText = findViewById(R.id.bytesSentText)
        bytesReceivedText = findViewById(R.id.bytesReceivedText)
        responseHexText = findViewById(R.id.responseHexText)
        manualCommandInput = findViewById(R.id.manualCommandInput)
        sendManualCommandButton = findViewById(R.id.sendManualCommandButton)
        manualCommandResponseText = findViewById(R.id.manualCommandResponseText)

        refreshButton.setOnClickListener { checkTactrix() }

        sendManualCommandButton.setOnClickListener {
            val command = manualCommandInput.text.toString()
            if (command.isEmpty()) {
                manualCommandResponseText.text = "No command entered"
                return@setOnClickListener
            }

            if (!::persistentSession.isInitialized || !persistentSession.sessionOpen) {
                checkTactrix() // ensure session open
            }

            val result = persistentSession.sendAsciiCommand(command)
            lastCommandText.text = "Last Command: $command"
            bytesSentText.text = "Bytes Sent: ${result.bytesSent}"
            bytesReceivedText.text = "Bytes Received: ${result.bytesReceived}"
            responseHexText.text = "Response Hex: ${result.responseHex}"
            manualCommandResponseText.text = result.responseAscii
        }
    }

    private fun getTactrixDevice(manager: UsbManager): UsbDevice? {
        return manager.deviceList.values.firstOrNull {
            it.vendorId == TACTRIX_VENDOR_ID && it.productId == TACTRIX_PRODUCT_ID
        }
    }

    private fun checkTactrix() {
        val manager = getSystemService(USB_SERVICE) as UsbManager
        val device = getTactrixDevice(manager)
        if (device == null) {
            statusText.text = "Tactrix not detected"
            return
        }

        if (!manager.hasPermission(device)) {
            val permissionIntent = PendingIntent.getBroadcast(
                this, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
            )
            manager.requestPermission(device, permissionIntent)
            statusText.text = "Requesting USB permission..."
            return
        }

        if (!::persistentSession.isInitialized) {
            persistentSession = UsbPersistentSession(this)
        }

        val success = persistentSession.openSession(device, manager)
        statusText.text = if (success) {
            "OpenPort detected and permission granted"
        } else {
            "Failed to open session"
        }
    }
}
