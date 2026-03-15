package com.yourpackage.hashslingingflasher

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        headerTitle = findViewById(R.id.header_title)
        toggleDevButton = findViewById(R.id.button_toggle_dev)
        overallStatus = findViewById(R.id.text_overall_status)
        manualCommandEdit = findViewById(R.id.edit_manual_command)
        sendCommandButton = findViewById(R.id.button_send_command)
        refreshUsbButton = findViewById(R.id.button_refresh_usb)

        // Set header text
        headerTitle.text = "HashSlingingFlasher"

        // Toggle developer mode
        toggleDevButton.setOnClickListener {
            Toast.makeText(this, "Developer mode toggled", Toast.LENGTH_SHORT).show()
            // TODO: implement actual developer mode logic
        }

        // Manual command sender
        sendCommandButton.setOnClickListener {
            val cmd = manualCommandEdit.text.toString()
            if (cmd.isNotEmpty()) {
                Toast.makeText(this, "Command sent: $cmd", Toast.LENGTH_SHORT).show()
                // TODO: implement sending command to OpenPort
            } else {
                Toast.makeText(this, "Enter a command first", Toast.LENGTH_SHORT).show()
            }
        }

        // USB status refresh
        refreshUsbButton.setOnClickListener {
            Toast.makeText(this, "USB status refreshed", Toast.LENGTH_SHORT).show()
            overallStatus.text = "USB Status Unknown"
            // TODO: implement actual USB detection
        }
    }
}
// trigger GitHub Actions build
// trigger build
