cat > app/src/main/java/com/ecuflasher/MainActivity.kt << 'EOF'
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
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val ACTION_USB_PERMISSION = "com.ecuflasher.USB_PERMISSION"

    private lateinit var statusText: TextView
    private lateinit var refreshButton: Button

    private lateinit var developerModeStatusText: TextView
    private lateinit var developerToolsPanel: LinearLayout
    private lateinit var toggleDeveloperModeButton: Button

    private var developerModeEnabled = false

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

                } else {

                    EcuLogger.usb("USB permission denied")
                    statusText.text = "USB permission denied"

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.usbStatusText)
        refreshButton = findViewById(R.id.refreshButton)

        developerModeStatusText = findViewById(R.id.developerModeStatusText)
        developerToolsPanel = findViewById(R.id.developerToolsPanel)
        toggleDeveloperModeButton = findViewById(R.id.toggleDeveloperModeButton)

        registerReceiver(
            usbReceiver,
            IntentFilter(ACTION_USB_PERMISSION),
            Context.RECEIVER_NOT_EXPORTED
        )

        refreshButton.setOnClickListener {
            checkTactrix()
        }

        toggleDeveloperModeButton.setOnClickListener {

            developerModeEnabled = !developerModeEnabled

            if (developerModeEnabled) {

                developerModeStatusText.text = "Developer Mode: ON"
                developerToolsPanel.visibility = LinearLayout.VISIBLE

                EcuLogger.main("Developer mode enabled")

            } else {

                developerModeStatusText.text = "Developer Mode: OFF"
                developerToolsPanel.visibility = LinearLayout.GONE

                EcuLogger.main("Developer mode disabled")

            }
        }

        developerToolsPanel.visibility = LinearLayout.GONE

        EcuLogger.main("HashSlingingFlasher started")
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

            return
        }

        val manager = UsbDeviceManager(this)
        val result = manager.openTactrixChannel()

        statusText.text = buildStatusText(result)

    }

    private fun buildStatusText(result: TactrixTestResult): String {

        return if (result.success) {

            if (result.responseHex.isNotEmpty()) {

                "${result.statusMessage}\nSent: ${result.bytesSent}\nReceived: ${result.responseHex}"

            } else {

                "${result.statusMessage}\nSent: ${result.bytesSent}\nReceived: ${result.bytesReceived}"

            }

        } else {

            result.statusMessage

        }

    }
}
