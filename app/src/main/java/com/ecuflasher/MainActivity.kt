package com.ecuflasher

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val ACTION_USB_PERMISSION = "com.ecuflasher.USB_PERMISSION"
    private val TACTRIX_VENDOR_ID = 1027
    private val TACTRIX_PRODUCT_ID = 52301

    private lateinit var statusText: TextView
    private lateinit var refreshButton: Button

    private lateinit var developerModeStatusText: TextView
    private lateinit var debugDetailsPanel: LinearLayout
    private lateinit var sessionSummaryPanel: LinearLayout
    private lateinit var manualCommandPanel: LinearLayout
    private lateinit var developerToolsPanel: LinearLayout

    private lateinit var deviceStateText: TextView
    private lateinit var permissionStateText: TextView
    private lateinit var lastCommandText: TextView
    private lateinit var bytesSentText: TextView
    private lateinit var bytesReceivedText: TextView
    private lateinit var responseHexText: TextView

    private lateinit var toggleDeveloperModeButton: Button
    private lateinit var developerLogText: TextView
    private lateinit var clearLogsButton: Button

    private lateinit var manualCommandPresetSpinner: Spinner
    private lateinit var manualCommandInput: EditText
    private lateinit var sendManualCommandButton: Button
    private lateinit var manualCommandResponseText: TextView

    private var developerModeEnabled = false

    private var lastCommand = "None"
    private var bytesSent = "-"
    private var bytesReceived = "-"
    private var responseHex = "--"

    private fun refreshDeveloperLog() {
        developerLogText.text = EcuLogger.getLogs()
    }

    private fun setDeveloperPanelsVisible(visible: Boolean) {
        val state = if (visible) LinearLayout.VISIBLE else LinearLayout.GONE
        debugDetailsPanel.visibility = state
        sessionSummaryPanel.visibility = state
        manualCommandPanel.visibility = state
        developerToolsPanel.visibility = state
    }

    private fun getTactrixDevice(systemUsbManager: UsbManager): UsbDevice? {
        return systemUsbManager.deviceList.values.firstOrNull {
            it.vendorId == TACTRIX_VENDOR_ID && it.productId == TACTRIX_PRODUCT_ID
        }
    }

    private fun refreshDebugPanel() {
        val systemUsbManager = getSystemService(USB_SERVICE) as UsbManager
        val tactrixDevice = getTactrixDevice(systemUsbManager)

        if (tactrixDevice == null) {
            deviceStateText.text = "Device: Not detected"
            permissionStateText.text = "Permission: Not applicable"
        } else {
            val permissionText = if (systemUsbManager.hasPermission(tactrixDevice)) {
                "Granted"
            } else {
                "Not granted"
            }
            deviceStateText.text = "Device: Tactrix OpenPort detected"
            permissionStateText.text = "Permission: $permissionText"
        }

        lastCommandText.text = "Last Command: $lastCommand"
        bytesSentText.text = "Bytes Sent: $bytesSent"
        bytesReceivedText.text = "Bytes Received: $bytesReceived"
        responseHexText.text = "Response Hex: $responseHex"
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_USB_PERMISSION) return

            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            if (granted) {
                EcuLogger.usb("USB permission granted")
                val manager = UsbDeviceManager(this@MainActivity)
                val result = manager.openTactrixChannel()
                statusText.text = buildStatusText(result)
            } else {
                EcuLogger.usb("USB permission denied")
                statusText.text = "USB permission denied"
            }

            refreshDeveloperLog()
            refreshDebugPanel()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusMessageText)
        refreshButton = findViewById(R.id.refreshButton)

        developerModeStatusText = findViewById(R.id.developerModeStatusText)
        debugDetailsPanel = findViewById(R.id.debugDetailsPanel)
        sessionSummaryPanel = findViewById(R.id.sessionSummaryPanel)
        manualCommandPanel = findViewById(R.id.manualCommandPanel)
        developerToolsPanel = findViewById(R.id.liveLogPanel)

        deviceStateText = findViewById(R.id.deviceStateText)
        permissionStateText = findViewById(R.id.permissionStateText)
        lastCommandText = findViewById(R.id.lastCommandText)
        bytesSentText = findViewById(R.id.bytesSentText)
        bytesReceivedText = findViewById(R.id.bytesReceivedText)
        responseHexText = findViewById(R.id.responseHexText)

        toggleDeveloperModeButton = findViewById(R.id.toggleDeveloperModeButton)
        developerLogText = findViewById(R.id.liveLogText)
        clearLogsButton = findViewById(R.id.clearLogsButton)

        manualCommandPresetSpinner = findViewById(R.id.manualCommandPresetSpinner)
        manualCommandInput = findViewById(R.id.manualCommandInput)
        sendManualCommandButton = findViewById(R.id.sendManualCommandButton)
        manualCommandResponseText = findViewById(R.id.manualCommandResponseText)

        registerReceiver(
            usbReceiver,
            IntentFilter(ACTION_USB_PERMISSION),
            Context.RECEIVER_NOT_EXPORTED
        )

        refreshButton.setOnClickListener {
            checkTactrix()
            refreshDeveloperLog()
        }

        toggleDeveloperModeButton.setOnClickListener {
            developerModeEnabled = !developerModeEnabled
            if (developerModeEnabled) {
                developerModeStatusText.text = "Developer Mode: ON"
                setDeveloperPanelsVisible(true)
                EcuLogger.main("Developer mode enabled")
            } else {
                developerModeStatusText.text = "Developer Mode: OFF"
                setDeveloperPanelsVisible(false)
                EcuLogger.main("Developer mode disabled")
            }
        }
    }

    private fun checkTactrix() {
        val manager = UsbDeviceManager(this)
        val result = manager.openTactrixChannel()
        statusText.text = buildStatusText(result)
        refreshDebugPanel()
    }

    private fun buildStatusText(result: TactrixTestResult): String {
        return result.statusMessage
    }
}
