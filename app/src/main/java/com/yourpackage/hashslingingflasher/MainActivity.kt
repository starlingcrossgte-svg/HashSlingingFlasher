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
import android.view.View
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
    private lateinit var developerModeStatusText: TextView
    private lateinit var toggleDeveloperModeButton: Button
    private lateinit var statusMessageText: TextView
    private lateinit var developerToolsPanel: View

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

    private var developerModeEnabled = false
    private var currentDevice: UsbDevice? = null

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_USB_PERMISSION) return

            val device = getUsbDeviceFromIntent(intent)
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

            currentDevice = device

            if (granted && device != null) {
                permissionStateText.text = "Permission: Granted"
                statusMessageText.text = "OpenPort detected and permission granted"
                EcuLogger.usb("USB permission granted")
                runTactrixTest()
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

        appTitleText.text = "HashSlingingFlasher"
        developerModeStatusText.text = "Developer Mode: OFF"
        developerToolsPanel.visibility = View.GONE

        registerUsbReceiver()

        toggleDeveloperModeButton.setOnClickListener {
            developerModeEnabled = !developerModeEnabled
            developerModeStatusText.text =
                if (developerModeEnabled) "Developer Mode: ON" else "Developer Mode: OFF"
            developerToolsPanel.visibility =
                if (developerModeEnabled) View.VISIBLE else View.GONE

            EcuLogger.main(
                if (developerModeEnabled) "Developer mode enabled" else "Developer mode disabled"
            )
            refreshDeveloperLog()

            Toast.makeText(
                this,
                if (developerModeEnabled) "Developer mode: ON" else "Developer mode: OFF",
                Toast.LENGTH_SHORT
            ).show()
        }

        sendManualCommandButton.setOnClickListener {
            val command = manualCommandInput.text.toString().trim()
            if (command.isEmpty()) {
                Toast.makeText(this, "Enter a command first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lastCommandText.text = "Last Command: $command"
            manualCommandResponseText.text = "Command queued: $command"
            summaryOpenPortCommandText.text = "OpenPort Command: $command"
            EcuLogger.main("Manual command sent: $command")
            refreshDeveloperLog()

            Toast.makeText(this, "Manual command queued", Toast.LENGTH_SHORT).show()
        }

        clearLogsButton.setOnClickListener {
            EcuLogger.clear()
            refreshDeveloperLog()
            Toast.makeText(this, "Logs cleared", Toast.LENGTH_SHORT).show()
        }

        refreshButton.setOnClickListener {
            checkTactrix()
            Toast.makeText(this, "USB status refreshed", Toast.LENGTH_SHORT).show()
        }

        EcuLogger.main("HashSlingingFlasher started")
        refreshDeveloperLog()
        checkTactrix()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiverSafe()
    }

    private fun bindViews() {
        appTitleText = findViewById(R.id.appTitleText)
        developerModeStatusText = findViewById(R.id.developerModeStatusText)
        toggleDeveloperModeButton = findViewById(R.id.toggleDeveloperModeButton)
        statusMessageText = findViewById(R.id.statusMessageText)
        developerToolsPanel = findViewById(R.id.developerModePanel)

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
        val presets = listOf("ATI", "ATA", "ATZ", "ATRV", "0100")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, presets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        commandPresetSpinner.adapter = adapter

        commandPresetSpinner.setSelection(0)
        manualCommandInput.setText("ATI")
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

    private fun checkTactrix() {
        val tactrixDevice = usbManager.deviceList.values.firstOrNull {
            it.vendorId == TACTRIX_VENDOR_ID && it.productId == TACTRIX_PRODUCT_ID
        }

        currentDevice = tactrixDevice

        if (tactrixDevice == null) {
            deviceStateText.text = "Device: Tactrix OpenPort not detected"
            permissionStateText.text = "Permission: Unknown"
            statusMessageText.text = "USB Status Unknown"
            summaryErrorText.text = "Last Error: Device not detected"
            EcuLogger.usb("Tactrix device not found")
            refreshDeveloperLog()
            return
        }

        deviceStateText.text = "Device: Tactrix OpenPort detected"

        if (usbManager.hasPermission(tactrixDevice)) {
            permissionStateText.text = "Permission: Granted"
            statusMessageText.text = "OpenPort detected and permission granted"
            EcuLogger.usb("USB permission already granted")
            runTactrixTest()
        } else {
            permissionStateText.text = "Permission: Pending"
            statusMessageText.text = "Requesting USB permission..."
            EcuLogger.usb("Requested USB permission for Tactrix")
            requestUsbPermission(tactrixDevice)
            refreshDeveloperLog()
        }
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

    private fun runTactrixTest() {
        val result = UsbDeviceManager(this).openTactrixChannel()

        statusMessageText.text = if (result.success) {
            "OpenPort detected and permission granted"
        } else {
            result.statusMessage
        }

        bytesSentText.text = "Bytes Sent: ${result.bytesSent}"
        bytesReceivedText.text = "Bytes Received: ${result.bytesReceived}"
        responseHexText.text = if (result.responseHex.isNotEmpty()) result.responseHex else "No response yet"

        summaryBusModeText.text = "Bus Mode: CAN"
        summaryEcuQueryText.text = "ECU Query: 0100"
        summaryResponseTypeText.text = if (result.success) {
            "Response Type: ECU response"
        } else if (result.bytesReceived > 0) {
            "Response Type: OpenPort response"
        } else {
            "Response Type: None"
        }

        summaryErrorText.text = if (result.success) {
            "Last Error: None"
        } else {
            "Last Error: ${result.statusMessage}"
        }

        manualCommandResponseText.text = if (result.responseHex.isNotEmpty()) {
            "Last response hex captured"
        } else {
            "No response yet"
        }

        EcuLogger.main(result.statusMessage)
        refreshDeveloperLog()
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
