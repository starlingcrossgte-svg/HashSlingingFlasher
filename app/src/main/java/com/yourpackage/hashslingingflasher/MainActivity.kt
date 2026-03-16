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
    private lateinit var refreshUsbButton: Button

    private var currentDevice: UsbDevice? = null

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null) return

            val action = intent.action ?: ""
            EcuLogger.usb("usbReceiver fired")
            EcuLogger.usb("usbReceiver action: $action")

            when (action) {
                ACTION_USB_PERMISSION -> {
                    val device = getUsbDeviceFromIntent(intent)
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

                    EcuLogger.usb("usbReceiver granted: $granted")
                    if (device != null) {
                        EcuLogger.usb("usbReceiver device: vendor=${device.vendorId} product=${device.productId}")
                    } else {
                        EcuLogger.usb("usbReceiver device: null")
                    }

                    currentDevice = device

                    if (granted && device != null) {
                        deviceStateText.text = "Device: Tactrix OpenPort detected"
                        permissionStateText.text = "Permission: Granted"
                        statusMessageText.text = "OpenPort ready"

                        summaryOpenPortCommandText.text = "OpenPort Command: None"
                        summaryBusModeText.text = "Bus Mode: None"
                        summaryEcuQueryText.text = "ECU Query: None"
                        summaryResponseTypeText.text = "Response Type: None"
                        summaryErrorText.text = "Last Error: None"

                        EcuLogger.usb("USB permission granted in receiver")
                        Toast.makeText(this@MainActivity, "USB permission granted", Toast.LENGTH_SHORT).show()
                    } else {
                        permissionStateText.text = "Permission: Denied"
                        statusMessageText.text = "USB permission denied"
                        summaryErrorText.text = "Last Error: USB permission denied"

                        EcuLogger.error("USB permission denied in receiver")
                        Toast.makeText(this@MainActivity, "USB permission denied", Toast.LENGTH_SHORT).show()
                    }

                    refreshDeveloperLog()
                }

                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device = getUsbDeviceFromIntent(intent)
                    if (device != null) {
                        EcuLogger.usb("USB device attached: vendor=${device.vendorId} product=${device.productId}")
                    } else {
                        EcuLogger.usb("USB device attached: device null")
                    }

                    if (device != null && isTactrixDevice(device)) {
                        currentDevice = device
                        deviceStateText.text = "Device: Tactrix OpenPort detected"

                        if (usbManager.hasPermission(device)) {
                            permissionStateText.text = "Permission: Granted"
                            statusMessageText.text = "OpenPort ready"
                            summaryErrorText.text = "Last Error: None"
                            EcuLogger.usb("Attached Tactrix already has permission")
                        } else {
                            permissionStateText.text = "Permission: Pending"
                            statusMessageText.text = "Requesting USB permission..."
                            EcuLogger.usb("Requesting USB permission after attach event")
                            requestUsbPermission(device)
                        }
                    }

                    refreshDeveloperLog()
                }

                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = getUsbDeviceFromIntent(intent)
                    if (device != null) {
                        EcuLogger.usb("USB device detached: vendor=${device.vendorId} product=${device.productId}")
                    } else {
                        EcuLogger.usb("USB device detached: device null")
                    }

                    if (device == null || isTactrixDevice(device)) {
                        currentDevice = null
                        deviceStateText.text = "Device: Tactrix OpenPort not detected"
                        permissionStateText.text = "Permission: Unknown"
                        statusMessageText.text = "USB status unknown"

                        summaryOpenPortCommandText.text = "OpenPort Command: None"
                        summaryBusModeText.text = "Bus Mode: None"
                        summaryEcuQueryText.text = "ECU Query: None"
                        summaryResponseTypeText.text = "Response Type: None"
                        summaryErrorText.text = "Last Error: Device disconnected"
                    }

                    refreshDeveloperLog()
                }

                else -> {
                    EcuLogger.usb("usbReceiver ignored unexpected action")
                    refreshDeveloperLog()
                }
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

        refreshUsbButton.setOnClickListener {
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
        refreshUsbButton = findViewById(R.id.refreshUsbButton)
    }

    private fun setupCommandPresetSpinner() {
        val presetLabels = listOf(
            "ata — wake OpenPort",
            "ati — firmware version",
            "atsp0 — auto protocol detect",
            "0100 — OBD CAN test",
            "atpb 500000 0 — force CAN 500k"
        )

        val presetCommands = listOf(
            "ata",
            "ati",
            "atsp0",
            "0100",
            "atpb 500000 0"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, presetLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        commandPresetSpinner.adapter = adapter

        commandPresetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                manualCommandInput.setText(presetCommands[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        commandPresetSpinner.setSelection(0)
        manualCommandInput.setText("ata")
    }

    private fun registerUsbReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(usbReceiver, filter)
        }

        EcuLogger.usb("USB receiver registered")
        refreshDeveloperLog()
    }

    private fun unregisterReceiverSafe() {
        try {
            unregisterReceiver(usbReceiver)
            EcuLogger.usb("USB receiver unregistered")
            refreshDeveloperLog()
        } catch (_: IllegalArgumentException) {
        }
    }

    private fun refreshOpenPortStatusOnly() {
        val tactrixDevice = usbManager.deviceList.values.firstOrNull { isTactrixDevice(it) }

        currentDevice = tactrixDevice

        if (tactrixDevice == null) {
            deviceStateText.text = "Device: Tactrix OpenPort not detected"
            permissionStateText.text = "Permission: Unknown"
            statusMessageText.text = "USB status unknown"

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
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        EcuLogger.usb("requestUsbPermission called")
        EcuLogger.usb("requestUsbPermission flags: $flags")
        EcuLogger.usb("requestUsbPermission device vendor=${device.vendorId} product=${device.productId}")

        val permissionIntent = Intent(ACTION_USB_PERMISSION).apply {
            setPackage(packageName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            permissionIntent,
            flags
        )

        usbManager.requestPermission(device, pendingIntent)
        EcuLogger.usb("usbManager.requestPermission invoked")
        refreshDeveloperLog()
    }

    private fun sendManualCommand(command: String) {
        val tactrixDevice = usbManager.deviceList.values.firstOrNull { isTactrixDevice(it) }

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
            when {
                result.responseAscii.isNotEmpty() -> result.responseAscii.trim()
                result.responseHex.isNotEmpty() -> result.responseHex
                else -> result.statusMessage
            }

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
            normalized.startsWith("atpb") && normalized.contains("500000") -> "Bus Mode: CAN 500000"
            normalized.startsWith("atsp") -> "Bus Mode: CAN"
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

    private fun isTactrixDevice(device: UsbDevice): Boolean {
        return device.vendorId == TACTRIX_VENDOR_ID && device.productId == TACTRIX_PRODUCT_ID
    }
}
