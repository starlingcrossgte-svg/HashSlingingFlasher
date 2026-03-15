package com.hashslingingflasher

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val ACTION_USB_PERMISSION = "com.hashslingingflasher.USB_PERMISSION"
        private const val TACTRIX_VENDOR_ID = 1027
        private const val TACTRIX_PRODUCT_ID = 52301
    }

    private lateinit var usbManager: UsbManager

    private lateinit var appTitleText: TextView
    private lateinit var statusMessageText: TextView

    private lateinit var deviceStateText: TextView
    private lateinit var permissionStateText: TextView
    private lateinit var lastCommandText: TextView
    private lateinit var bytesSentText: TextView
    private lateinit var bytesReceivedText: TextView
    private lateinit var responseHexText: TextView

    private lateinit var summaryOpenPortCommandText: TextView
    private lateinit var summaryBusModeText: TextView
    private lateinit var summaryEcuQueryText: TextView
    private lateinit var summaryResponseTypeText: TextView
    private lateinit var summaryErrorText: TextView

    private lateinit var commandPresetSpinner: Spinner
    private lateinit var manualCommandInput: EditText
    private lateinit var sendManualCommandButton: Button
    private lateinit var manualCommandResponseText: TextView

    private lateinit var developerLogText: TextView
    private lateinit var clearLogsButton: Button
    private lateinit var refreshButton: Button

    private var currentDevice: UsbDevice? = null

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_USB_PERMISSION) return

            val device = getUsbDeviceFromIntent(intent)
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

            currentDevice = device

            if (granted && device != null) {
                permissionStateText.text = "Permission: Granted"
                statusMessageText.text = "OpenPort ready"
                summaryErrorText.text = "Last Error: None"
                EcuLogger.usb("USB permission granted")
                refreshDeveloperLog()
            } else {
                permissionStateText.text = "Permission: Denied"
                statusMessageText.text = "USB permission denied"
                summaryErrorText.text = "Last Error: USB permission denied"
                EcuLogger.error("USB permission denied")
                refreshDeveloperLog()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        bindViews()
        setupCommandPresetSpinner()
        registerUsbReceiver()

        appTitleText.text = "HashSlingingFlasher"

        sendManualCommandButton.setOnClickListener {
            val command = manualCommandInput.text.toString().trim()
            if (command.isEmpty()) {
                Toast.makeText(this, "Enter a command first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendManualCommand(command)
        }

        clearLogsButton.setOnClickListener {
            EcuLogger.clear()
            refreshDeveloperLog()
            Toast.makeText(this, "Logs cleared", Toast.LENGTH_SHORT).show()
        }

        refreshButton.setOnClickListener {
            refreshOpenPortStatusOnly()
            Toast.makeText(this, "USB status refreshed", Toast.LENGTH_SHORT).show()
        }

        EcuLogger.main("HashSlingingFlasher started")
        refreshDeveloperLog()
        refreshOpenPortStatusOnly()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiverSafe()
    }

    private fun bindViews() {
        appTitleText = findViewById(R.id.appTitleText)
        statusMessageText = findViewById(R.id.statusMessageText)

        deviceStateText = findViewById(R.id.deviceStateText)
        permissionStateText = findViewById(R.id.permissionStateText)
        lastCommandText = findViewById(R.id.lastCommandText)
        bytesSentText = findViewById(R.id.bytesSentText)
        bytesReceivedText = findViewById(R.id.bytesReceivedText)
        responseHexText = findViewById(R.id.responseHexText)

        summaryOpenPortCommandText = findViewById(R.id.summaryOpenPortCommandText)
        summaryBusModeText = findViewById(R.id.summaryBusModeText)
        summaryEcuQueryText = findViewById(R.id.summaryEcuQueryText)
        summaryResponseTypeText = findViewById(R.id.summaryResponseTypeText)
        summaryErrorText = findViewById(R.id.summaryErrorText)

        commandPresetSpinner = findViewById(R.id.commandPresetSpinner)
        manualCommandInput = findViewById(R.id.manualCommandInput)
        sendManualCommandButton = findViewById(R.id.sendManualCommandButton)
        manualCommandResponseText = findViewById(R.id.manualCommandResponseText)

        developerLogText = findViewById(R.id.developerLogText)
        clearLogsButton = findViewById(R.id.clearLogsButton)
        refreshButton = findViewById(R.id.refreshButton)
    }

    private fun setupCommandPresetSpinner() {
        val presets = listOf(
            "ata",
            "ato6 0 500000 0"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, presets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        commandPresetSpinner.adapter = adapter

        commandPresetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                manualCommandInput.setText(presets[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        commandPresetSpinner.setSelection(0)
        manualCommandInput.setText("ata")
    }

    private fun registerUsbReceiver() {
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(usbReceiver, filter)
        }
    }

    private fun unregisterReceiverSafe() {
        try {
            unregisterReceiver(usbReceiver)
        } catch (_: IllegalArgumentException) {
        }
    }

    private fun refreshOpenPortStatusOnly() {
        val tactrixDevice = usbManager.deviceList.values.firstOrNull {
            it.vendorId == TACTRIX_VENDOR_ID && it.productId == TACTRIX_PRODUCT_ID
        }

        currentDevice = tactrixDevice

        if (tactrixDevice == null) {
            deviceStateText.text = "Device: Tactrix OpenPort not detected"
            permissionStateText.text = "Permission: Unknown"
            statusMessageText.text = "USB Status Unknown"
            summaryOpenPortCommandText.text = "OpenPort Command: None"
            summaryBusModeText.text = "Bus Mode: None"
            summaryEcuQueryText.text = "ECU Query: None"
            summaryResponseTypeText.text = "Response Type: None"
            summaryErrorText.text = "Last Error: Device not detected"
            EcuLogger.usb("Tactrix device not found")
            refreshDeveloperLog()
            return
        }

        deviceStateText.text = "Device: Tactrix OpenPort detected"

        if (usbManager.hasPermission(tactrixDevice)) {
            permissionStateText.text = "Permission: Granted"
            statusMessageText.text = "OpenPort ready"
            summaryErrorText.text = "Last Error: None"
            EcuLogger.usb("USB permission already granted")
        } else {
            permissionStateText.text = "Permission: Pending"
            statusMessageText.text = "Requesting USB permission..."
            EcuLogger.usb("Requested USB permission for Tactrix")
            requestUsbPermission(tactrixDevice)
        }

        refreshDeveloperLog()
    }

    private fun requestUsbPermission(device: UsbDevice) {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

        val permissionIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(ACTION_USB_PERMISSION),
            flags
        )

        usbManager.requestPermission(device, permissionIntent)
    }

    private fun sendManualCommand(command: String) {
        val tactrixDevice = usbManager.deviceList.values.firstOrNull {
            it.vendorId == TACTRIX_VENDOR_ID && it.productId == TACTRIX_PRODUCT_ID
        }

        if (tactrixDevice == null) {
            statusMessageText.text = "OpenPort not detected"
            manualCommandResponseText.text = "No device connected"
            summaryErrorText.text = "Last Error: Device not detected"
            EcuLogger.error("Manual command failed: Tactrix device not found")
            refreshDeveloperLog()
            return
        }

        if (!usbManager.hasPermission(tactrixDevice)) {
            statusMessageText.text = "Requesting USB permission..."
            manualCommandResponseText.text = "Permission required"
            EcuLogger.usb("Manual command requested USB permission")
            requestUsbPermission(tactrixDevice)
            refreshDeveloperLog()
            return
        }

        lastCommandText.text = "Last Command: $command"
        summaryOpenPortCommandText.text = "OpenPort Command: $command"
        summaryBusModeText.text = buildBusModeSummary(command)
        summaryEcuQueryText.text = "ECU Query: None"

        val result = UsbDeviceManager(this).sendCustomAsciiCommand(command)

        bytesSentText.text = "Bytes Sent: ${result.bytesSent}"
        bytesReceivedText.text = "Bytes Received: ${result.bytesReceived}"
        responseHexText.text =
            if (result.responseHex.isNotEmpty()) result.responseHex else "No response yet"

        manualCommandResponseText.text =
            if (result.responseAscii.isNotEmpty()) result.responseAscii.trim()
            else if (result.responseHex.isNotEmpty()) result.responseHex
            else result.statusMessage

        statusMessageText.text =
            if (result.success) "OpenPort command response received" else result.statusMessage

        summaryResponseTypeText.text =
            if (result.responseAscii.isNotEmpty() || result.responseHex.isNotEmpty()) {
                "Response Type: OpenPort response"
            } else {
                "Response Type: None"
            }

        summaryErrorText.text =
            if (result.success) "Last Error: None" else "Last Error: ${result.statusMessage}"

        EcuLogger.main("Manual command sent: $command")
        EcuLogger.main(result.statusMessage)
        refreshDeveloperLog()
        Toast.makeText(this, "Manual command sent", Toast.LENGTH_SHORT).show()
    }

    private fun buildBusModeSummary(command: String): String {
        val normalized = command.trim().lowercase()

        return when {
            normalized == "ata" -> "Bus Mode: None"
            normalized.startsWith("ato6") && normalized.contains("500000") -> "Bus Mode: CAN 500000"
            normalized.startsWith("ato6") -> "Bus Mode: CAN"
            else -> "Bus Mode: Manual OpenPort"
        }
    }

    private fun refreshDeveloperLog() {
        developerLogText.text = EcuLogger.getLogs().ifEmpty { "No logs yet" }
    }

    private fun getUsbDeviceFromIntent(intent: Intent): UsbDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }
    }
}
