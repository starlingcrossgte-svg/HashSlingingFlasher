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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val ACTION_USB_PERMISSION = "com.ecuflasher.USB_PERMISSION"

    private lateinit var statusText: TextView
    private lateinit var refreshButton: Button

    private lateinit var developerModeStatusText: TextView
    private lateinit var debugDetailsPanel: LinearLayout
    private lateinit var sessionSummaryPanel: LinearLayout
    private lateinit var manualCommandPanel: LinearLayout
    private lateinit var developerToolsPanel: LinearLayout
    private lateinit var toggleDeveloperModeButton: Button
    private lateinit var developerLogText: TextView

    private var developerModeEnabled = false

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

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_USB_PERMISSION) {

                val device: UsbDevice? =
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                    EcuLogger.usb("USB permission granted")

                    val manager = UsbDeviceManager(this@MainActivity)
                    val result = manager.openTactrixChannel()

                    statusText.text = buildStatusText(result)
                    refreshDeveloperLog()

                } else {

                    EcuLogger.usb("USB permission denied")
                    statusText.text = "USB permission denied"
                    refreshDeveloperLog()
                }
            }
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
        toggleDeveloperModeButton = findViewById(R.id.toggleDeveloperModeButton)
        developerLogText = findViewById(R.id.liveLogText)

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

            refreshDeveloperLog()
        }

        developerModeEnabled = false
        developerModeStatusText.text = "Developer Mode: OFF"
        setDeveloperPanelsVisible(false)

        EcuLogger.main("HashSlingingFlasher started")
        refreshDeveloperLog()
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
            refreshDeveloperLog()
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
            refreshDeveloperLog()
            return
        }

        val manager = UsbDeviceManager(this)
        val result = manager.openTactrixChannel()

        statusText.text = buildStatusText(result)
        refreshDeveloperLog()
    }

    private fun buildStatusText(result: TactrixTestResult): String {
        return result.statusMessage
    }
}
