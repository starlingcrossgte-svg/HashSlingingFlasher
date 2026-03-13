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

    private lateinit var liveLogText: TextView
    private lateinit var refreshButton: Button

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_USB_PERMISSION) return

            val device: UsbDevice? =
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && device != null) {
                EcuLogger.usb("USB permission granted")
                permissionStateText.text = "Permission: Granted"
                runTactrixTest()
            } else {
                EcuLogger.usb("USB permission denied")
                permissionStateText.text = "Permission: Denied"
                statusMessageText.text = "USB permission denied"
                lastCommandText.text = "Last Command: None"
                bytesSentText.text = "Bytes Sent: -"
                bytesReceivedText.text = "Bytes Received: -"
                responseHexText.text = "No response yet"
                refreshLiveLog()
                refreshSessionSummary()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

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

        liveLogText = findViewById(R.id.liveLogText)
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
        refreshLiveLog()
        refreshSessionSummary()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    private fun checkTactrix() {
        val systemUsbManager = getSystemService(USB_SERVICE) as UsbManager
        val tactrixDevice = systemUsbManager.deviceList.values.firstOrNull {
            it.vendorId == UsbTransport.TACTRIX_VENDOR_ID &&
            it.productId == UsbTransport.TACTRIX_PRODUCT_ID
        }

        if (tactrixDevice == null) {
            EcuLogger.usb("Tactrix device not found")
            deviceStateText.text = "Device: Not Detected"
            permissionStateText.text = "Permission: N/A"
            statusMessageText.text = "Tactrix device not detected"
            lastCommandText.text = "Last Command: None"
            bytesSentText.text = "Bytes Sent: -"
            bytesReceivedText.text = "Bytes Received: -"
            responseHexText.text = "No response yet"
            refreshLiveLog()
            refreshSessionSummary()
            return
        }

        deviceStateText.text = "Device: Tactrix OpenPort 2.0 Detected"

        if (!systemUsbManager.hasPermission(tactrixDevice)) {
            permissionStateText.text = "Permission: Not Granted"
            requestUsbPermission(systemUsbManager, tactrixDevice)
            return
        }

        permissionStateText.text = "Permission: Granted"
        runTactrixTest()
    }

    private fun requestUsbPermission(systemUsbManager: UsbManager, tactrixDevice: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE
        )

        systemUsbManager.requestPermission(tactrixDevice, permissionIntent)
        EcuLogger.usb("Requested USB permission for Tactrix")
        statusMessageText.text = "Requesting USB permission..."
        lastCommandText.text = "Last Command: Permission Request"
        bytesSentText.text = "Bytes Sent: -"
        bytesReceivedText.text = "Bytes Received: -"
        responseHexText.text = "No response yet"
        refreshLiveLog()
        refreshSessionSummary()
    }

    private fun runTactrixTest() {
        val manager = UsbDeviceManager(this)
        val result = manager.openTactrixChannel()
        renderResult(result)
    }

    private fun renderResult(result: TactrixTestResult) {
        statusMessageText.text = result.statusMessage
        lastCommandText.text = "Last Command: USB Refresh/Test Run"
        bytesSentText.text = "Bytes Sent: ${formatCount(result.bytesSent)}"
        bytesReceivedText.text = "Bytes Received: ${formatCount(result.bytesReceived)}"
        responseHexText.text = if (result.responseHex.isNotEmpty()) {
            result.responseHex
        } else {
            "No response yet"
        }
        refreshLiveLog()
        refreshSessionSummary()
    }

    private fun refreshLiveLog() {
        liveLogText.text = InAppLogStore.getAllText()
    }

    private fun refreshSessionSummary() {
        summaryOpenPortCommandText.text =
            "OpenPort Command: ${SessionSummaryStore.lastOpenPortCommand}"
        summaryBusModeText.text =
            "Bus Mode: ${SessionSummaryStore.lastBusMode}"
        summaryEcuQueryText.text =
            "ECU Query: ${SessionSummaryStore.lastEcuQuery}"
        summaryResponseTypeText.text =
            "Response Type: ${SessionSummaryStore.lastResponseType}"
        summaryErrorText.text =
            "Last Error: ${SessionSummaryStore.lastError}"
    }

    private fun formatCount(value: Int): String {
        return if (value >= 0) value.toString() else "-"
    }
}
