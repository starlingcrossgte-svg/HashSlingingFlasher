package com.hashslingingflasher

import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var headerTitle: TextView
    private lateinit var toggleDevButton: Button
    private lateinit var overallStatus: TextView
    private lateinit var manualCommandEdit: EditText
    private lateinit var sendCommandButton: Button
    private lateinit var refreshUsbButton: Button
    private lateinit var usbManager: UsbManager

    private var developerModeEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        headerTitle = findViewById(R.id.header_title)
        toggleDevButton = findViewById(R.id.button_toggle_dev)
        overallStatus = findViewById(R.id.text_overall_status)
        manualCommandEdit = findViewById(R.id.edit_manual_command)
        sendCommandButton = findViewById(R.id.button_send_command)
        refreshUsbButton = findViewById(R.id.button_refresh_usb)

        usbManager = getSystemService(USB_SERVICE) as UsbManager

        headerTitle.text = "HashSlingingFlasher"

        updateUsbStatus()

        toggleDevButton.setOnClickListener {
            developerModeEnabled = !developerModeEnabled
            val state = if (developerModeEnabled) "ON" else "OFF"
            Toast.makeText(this, "Developer mode: $state", Toast.LENGTH_SHORT).show()
        }

        sendCommandButton.setOnClickListener {
            val cmd = manualCommandEdit.text.toString().trim()
            if (cmd.isEmpty()) {
                Toast.makeText(this, "Enter a command first", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Manual command queued: $cmd", Toast.LENGTH_SHORT).show()
            }
        }

        refreshUsbButton.setOnClickListener {
            updateUsbStatus()
            Toast.makeText(this, "USB status refreshed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUsbStatus() {
        val connected = usbManager.deviceList.isNotEmpty()
        overallStatus.text = if (connected) {
            "USB Device Connected"
        } else {
            "USB Status Unknown"
        }
    }
}
