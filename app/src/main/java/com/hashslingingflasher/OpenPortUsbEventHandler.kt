package com.hashslingingflasher

import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OpenPortUsbEventHandler(
    private val activity: AppCompatActivity,
    private val usbManager: UsbManager,
    private val usbPermissionHelper: UsbPermissionHelper,
    private val openPortStatusPresenter: OpenPortStatusPresenter,
    private val permissionStateText: TextView,
    private val statusMessageText: TextView,
    private val summaryErrorText: TextView,
    private val actionUsbPermission: String,
    private val onCurrentDeviceChanged: (UsbDevice?) -> Unit
) {
    fun handle(intent: Intent) {
        val action = intent.action ?: ""
        EcuLogger.usb("usbReceiver fired")
        EcuLogger.usb("usbReceiver action: $action")

        when (action) {
            actionUsbPermission -> handlePermissionResult(intent)
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> handleDeviceAttached(intent)
            UsbManager.ACTION_USB_DEVICE_DETACHED -> handleDeviceDetached(intent)
            else -> {
                EcuLogger.usb("usbReceiver ignored unexpected action")
                openPortStatusPresenter.refreshDeveloperLog()
            }
        }
    }

    private fun handlePermissionResult(intent: Intent) {
        val device = usbPermissionHelper.getUsbDeviceFromIntent(intent)
        val granted = intent.getBooleanExtra(
            UsbManager.EXTRA_PERMISSION_GRANTED,
            false
        )

        EcuLogger.usb("usbReceiver permission grant=$granted")
        if (device != null) {
            EcuLogger.usb(
                "usbReceiver device: vendor=${device.vendorId} product=${device.productId}"
            )
        } else {
            EcuLogger.usb("usbReceiver device: null")
        }

        onCurrentDeviceChanged(device)

        if (granted && device != null && usbPermissionHelper.isTactrixDevice(device)) {
            openPortStatusPresenter.showDeviceDetectedPermissionGranted()
            openPortStatusPresenter.resetCommandDisplayToNeutral()

            EcuLogger.usb("USB permission granted in receiver")
            openPortStatusPresenter.refreshDeveloperLog()

            Toast.makeText(
                activity,
                "USB permission granted",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            permissionStateText.text = "Permission: Denied"
            statusMessageText.text = "USB permission denied"
            summaryErrorText.text = "Last Error: USB permission denied"

            EcuLogger.error("USB permission denied in receiver")
            openPortStatusPresenter.refreshDeveloperLog()

            Toast.makeText(
                activity,
                "USB permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleDeviceAttached(intent: Intent) {
        val device = usbPermissionHelper.getUsbDeviceFromIntent(intent)
        if (device != null) {
            EcuLogger.usb(
                "USB device attached: vendor=${device.vendorId} product=${device.productId}"
            )
        } else {
            EcuLogger.usb("USB device attached: device null")
        }

        if (device != null && usbPermissionHelper.isTactrixDevice(device)) {
            onCurrentDeviceChanged(device)

            if (usbManager.hasPermission(device)) {
                openPortStatusPresenter.showDeviceDetectedPermissionGranted()
                openPortStatusPresenter.resetCommandDisplayToNeutral()
                EcuLogger.usb("Attached Tactrix already has permission")
            } else {
                openPortStatusPresenter.showDeviceDetectedPermissionPending()
                EcuLogger.usb("Requesting USB permission after attach event")
                usbPermissionHelper.requestUsbPermission(device, actionUsbPermission)
            }
        }

        openPortStatusPresenter.refreshDeveloperLog()
    }

    private fun handleDeviceDetached(intent: Intent) {
        val device = usbPermissionHelper.getUsbDeviceFromIntent(intent)
        if (device != null) {
            EcuLogger.usb(
                "USB device detached: vendor=${device.vendorId} product=${device.productId}"
            )
        } else {
            EcuLogger.usb("USB device detached: device null")
        }

        if (device == null || usbPermissionHelper.isTactrixDevice(device)) {
            onCurrentDeviceChanged(null)
            openPortStatusPresenter.showDeviceDisconnected()
        }

        openPortStatusPresenter.refreshDeveloperLog()
    }
}
