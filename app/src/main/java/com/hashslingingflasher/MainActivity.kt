package com.hashslingingflasher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    companion object {
        private const val ACTION_USB_PERMISSION =
            "com.hashslingingflasher.USB_PERMISSION"
    }

    private lateinit var usbManager: UsbManager
    private lateinit var usbPermissionHelper: UsbPermissionHelper
    private lateinit var openPortStatusPresenter: OpenPortStatusPresenter
    private lateinit var manualCommandPresenter: ManualCommandPresenter
    private lateinit var commandModeHelper: CommandModeHelper
    private lateinit var openPortStatusRefresher: OpenPortStatusRefresher
    private lateinit var openPortUsbEventHandler: OpenPortUsbEventHandler
    private lateinit var openPortInterrogator: OpenPortInterrogator

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
    private lateinit var commandModeSpinner: Spinner
    private lateinit var manualCommandInput: EditText
    private lateinit var sendManualCommandButton: Button
    private lateinit var endSessionButton: Button
    private lateinit var manualCommandResponseText: TextView

    private lateinit var developerLogText: TextView
    private lateinit var clearLogsButton: Button

    private var currentDevice: UsbDevice? = null

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            openPortUsbEventHandler.handle(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        usbPermissionHelper = UsbPermissionHelper(this, usbManager)

        bindViews()

        openPortStatusPresenter = OpenPortStatusPresenter(
            deviceStateText = deviceStateText,
            permissionStateText = permissionStateText,
            statusMessageText = statusMessageText,
            lastCommandText = lastCommandText,
            bytesSentText = bytesSentText,
            bytesReceivedText = bytesReceivedText,
            responseHexText = responseHexText,
            summaryOpenPortCommandText = summaryOpenPortCommandText,
            summaryBusModeText = summaryBusModeText,
            summaryEcuQueryText = summaryEcuQueryText,
            summaryResponseTypeText = summaryResponseTypeText,
            summaryErrorText = summaryErrorText,
            developerLogText = developerLogText
        )

        manualCommandPresenter = ManualCommandPresenter(
            statusMessageText = statusMessageText,
            lastCommandText = lastCommandText,
            bytesSentText = bytesSentText,
            bytesReceivedText = bytesReceivedText,
            responseHexText = responseHexText,
            summaryOpenPortCommandText = summaryOpenPortCommandText,
            summaryBusModeText = summaryBusModeText,
            summaryEcuQueryText = summaryEcuQueryText,
            summaryResponseTypeText = summaryResponseTypeText,
            summaryErrorText = summaryErrorText,
            manualCommandResponseText = manualCommandResponseText
        )

        val commandPresetHelper = CommandPresetHelper(
            spinner = commandPresetSpinner,
            manualCommandInput = manualCommandInput
        )

        commandModeHelper = CommandModeHelper(
            spinner = commandModeSpinner,
            onModeChanged = { mode ->
                commandPresetHelper.updateForMode(mode)
            }
        )

        openPortInterrogator = OpenPortInterrogator(
            usbDeviceManager = UsbDeviceManager(this)
        )

        openPortStatusRefresher = OpenPortStatusRefresher(
            usbManager = usbManager,
            usbPermissionHelper = usbPermissionHelper,
            openPortStatusPresenter = openPortStatusPresenter,
            requestUsbPermission = { device ->
                usbPermissionHelper.requestUsbPermission(device, ACTION_USB_PERMISSION)
            },
            onCurrentDeviceChanged = { device ->
                currentDevice = device
            }
        )

        openPortUsbEventHandler = OpenPortUsbEventHandler(
            activity = this,
            usbManager = usbManager,
            usbPermissionHelper = usbPermissionHelper,
            openPortStatusPresenter = openPortStatusPresenter,
            permissionStateText = permissionStateText,
            statusMessageText = statusMessageText,
            summaryErrorText = summaryErrorText,
            actionUsbPermission = ACTION_USB_PERMISSION,
            onCurrentDeviceChanged = { device ->
                currentDevice = device
            }
        )

        commandPresetHelper.bind(CommandModeHelper.MODE_ADAPTER_ASCII)
        commandModeHelper.bind()
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

        endSessionButton.setOnClickListener {
            endManualSession()
        }

        clearLogsButton.setOnClickListener {
            EcuLogger.clear()
            refreshOpenPortStatusOnly()
            Toast.makeText(this, "Logs cleared", Toast.LENGTH_SHORT).show()
        }

        EcuLogger.main("HashSlingingFlasher started")
        openPortStatusPresenter.refreshDeveloperLog()
        refreshOpenPortStatusOnly()
    }

    override fun onDestroy() {
        super.onDestroy()
        openPortInterrogator.endManualSession()
        unregisterReceiverSafely()
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

        commandModeSpinner = findViewById(R.id.commandModeSpinner)
        commandPresetSpinner = findViewById(R.id.commandPresetSpinner)
        manualCommandInput = findViewById(R.id.manualCommandInput)
        sendManualCommandButton = findViewById(R.id.sendManualCommandButton)
        endSessionButton = findViewById(R.id.endSessionButton)
        manualCommandResponseText = findViewById(R.id.manualCommandResponseText)

        developerLogText = findViewById(R.id.developerLogText)
        clearLogsButton = findViewById(R.id.clearLogsButton)
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
        openPortStatusPresenter.refreshDeveloperLog()
    }

    private fun unregisterReceiverSafely() {
        try {
            unregisterReceiver(usbReceiver)
            EcuLogger.usb("USB receiver unregistered")
            openPortStatusPresenter.refreshDeveloperLog()
        } catch (_: IllegalArgumentException) {
        }
    }

    private fun refreshOpenPortStatusOnly() {
        openPortStatusRefresher.refresh()
    }

    private fun sendManualCommand(command: String) {
        val tactrixDevice = usbManager.deviceList.values.firstOrNull {
            usbPermissionHelper.isTactrixDevice(it)
        }

        if (tactrixDevice == null) {
            manualCommandPresenter.showNoDeviceConnected()
            EcuLogger.error("Manual command failed: Tactrix device not found")
            openPortStatusPresenter.refreshDeveloperLog()
            return
        }

        if (!usbManager.hasPermission(tactrixDevice)) {
            manualCommandPresenter.showPermissionRequired()
            EcuLogger.usb("Manual command requested USB permission")
            usbPermissionHelper.requestUsbPermission(tactrixDevice, ACTION_USB_PERMISSION)
            openPortStatusPresenter.refreshDeveloperLog()
            return
        }

        val selectedMode = commandModeHelper.selectedModeLabel()
        EcuLogger.main("Manual mode selected: $selectedMode")

        val profile = openPortInterrogator.profileCommand(command, selectedMode)
        manualCommandPresenter.showCommandSending(command, profile)

        thread {
            val result = openPortInterrogator.runManualCommand(command, selectedMode)

            runOnUiThread {
                manualCommandPresenter.showCommandResult(result)
                EcuLogger.main("Manual command sent: $command")
                EcuLogger.main(result.transportResult.statusMessage)
                openPortStatusPresenter.refreshDeveloperLog()
                Toast.makeText(this, "Manual command sent", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun endManualSession() {
        val ended = openPortInterrogator.endManualSession()

        if (ended) {
            manualCommandPresenter.showSessionEnded()
            EcuLogger.main("Manual session ended")
            openPortStatusPresenter.refreshDeveloperLog()
            Toast.makeText(this, "OpenPort session ended", Toast.LENGTH_SHORT).show()
        } else {
            EcuLogger.main("No active manual session to end")
            openPortStatusPresenter.refreshDeveloperLog()
            Toast.makeText(this, "No active session", Toast.LENGTH_SHORT).show()
        }
    }
}
