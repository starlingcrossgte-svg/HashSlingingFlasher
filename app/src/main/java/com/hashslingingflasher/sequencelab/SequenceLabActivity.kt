package com.hashslingingflasher.sequencelab

import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import com.hashslingingflasher.obdlink.ObdLinkUsbController
import com.hashslingingflasher.obdlink.ObdLinkUsbManager
import com.hashslingingflasher.obdlink.ObdLinkUsbPermissionRegistrar
import com.hashslingingflasher.sequence.SequenceStep

class SequenceLabActivity : ComponentActivity() {

    private lateinit var viewModel: SequenceLabViewModel
    private lateinit var obdLinkUsbController: ObdLinkUsbController
    private var obdLinkPermissionRegistrar: ObdLinkUsbPermissionRegistrar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[SequenceLabViewModel::class.java]

        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        obdLinkUsbController = ObdLinkUsbController(
            context = this,
            usbManager = usbManager
        )

        obdLinkPermissionRegistrar = obdLinkUsbController.createPermissionRegistrar { deviceName, granted ->
            if (!granted) {
                viewModel.setRunning(false, "OBDLink USB permission denied")
                return@createPermissionRegistrar
            }

            if (deviceName.isNullOrBlank()) {
                viewModel.setRunning(false, "OBDLink USB permission callback missing device")
                return@createPermissionRegistrar
            }

            when (val result = obdLinkUsbController.connectByDeviceName(deviceName)) {
                is ObdLinkUsbManager.ConnectResult.Success -> {
                    viewModel.attachObdLinkTransport(obdLinkUsbController.transport())
                    viewModel.setRunning(false, "OBDLink USB connected: $deviceName")
                }
                is ObdLinkUsbManager.ConnectResult.Failure -> {
                    viewModel.setRunning(false, "OBDLink USB connect failed: ${result.reason}")
                }
            }
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            val sequenceLabColors = darkColorScheme(
                primary = Color(0xFFFF6A00),
                secondary = Color(0xFFFF6A00),
                tertiary = Color(0xFFFF6A00),
                background = Color(0xFF090A0C),
                surface = Color(0xFF14161A),
                onPrimary = Color.Black,
                onSecondary = Color.Black,
                onTertiary = Color.Black,
                onBackground = Color(0xFFE4E4E4),
                onSurface = Color(0xFFF4F4F4)
            )

            MaterialTheme(
                colorScheme = sequenceLabColors
            ) {
                SequenceLabScreen(
                    uiState = uiState,
                    onAddPauseStep = {
                        viewModel.addStep(
                            SequenceStep.PauseStep(
                                id = "pause-${System.currentTimeMillis()}",
                                title = "Pause",
                                durationMs = 500L
                            )
                        )
                    },
                    asciiPresets = obdLinkAsciiPresets,
                    onAddAsciiPreset = { preset ->
                        viewModel.addStep(
                            SequenceStep.AdapterAsciiStep(
                                id = "ascii-${System.currentTimeMillis()}",
                                title = preset.displayName,
                                command = preset.rawCommand
                            )
                        )
                    },
                    onAddRawStep = {
                        viewModel.addStep(
                            SequenceStep.RawHexStep(
                                id = "raw-${System.currentTimeMillis()}",
                                title = "Raw Hex",
                                hexPayload = "80 18 F0 01 BF 48"
                            )
                        )
                    },
                    onRemoveStep = { stepId ->
                        viewModel.removeStep(stepId)
                    },
                    onSendSingleAsciiCommand = { command ->
                        viewModel.sendSingleAsciiCommand(command)
                    },
                      onUpdateCommandSlot = { index, command ->
                          viewModel.updateCommandSlot(index, command)
                      },
                      onSendCommandSlot = { index ->
                          viewModel.sendCommandSlot(index)
                      },
                    onDiscoverUsb = {
                        val candidate = obdLinkUsbController.findBestCandidate()
                        when {
                            candidate == null -> {
                                viewModel.setRunning(false, "No non-Tactrix USB candidate found")
                            }
                            obdLinkUsbController.hasPermission(candidate.deviceName) -> {
                                when (val result = obdLinkUsbController.connectByDeviceName(candidate.deviceName)) {
                                    is ObdLinkUsbManager.ConnectResult.Success -> {
                                        viewModel.attachObdLinkTransport(obdLinkUsbController.transport())
                                        viewModel.setRunning(
                                            false,
                                            "OBDLink USB connected: ${candidate.deviceName}"
                                        )
                                    }
                                    is ObdLinkUsbManager.ConnectResult.Failure -> {
                                        viewModel.setRunning(
                                            false,
                                            "OBDLink USB connect failed: ${result.reason}"
                                        )
                                    }
                                }
                            }
                            else -> {
                                obdLinkUsbController.requestPermission(candidate.deviceName)
                                viewModel.setRunning(
                                    false,
                                    "Requesting OBDLink USB permission: VID=${candidate.vendorId} PID=${candidate.productId}"
                                )
                            }
                        }
                    },
                    onRunSequence = {
                        viewModel.runSequence()
                    },
                    onStopSequence = {
                        viewModel.stopSequence()
                    },
                    onClearLog = {
                        viewModel.clearLog()
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        obdLinkPermissionRegistrar?.register()
    }

    override fun onStop() {
        obdLinkPermissionRegistrar?.unregister()
        super.onStop()
    }

    override fun onDestroy() {
        obdLinkUsbController.disconnect()
        viewModel.clearObdLinkTransport()
        super.onDestroy()
    }
}
