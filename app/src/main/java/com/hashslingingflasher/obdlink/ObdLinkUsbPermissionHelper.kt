package com.hashslingingflasher.obdlink

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build

class ObdLinkUsbPermissionHelper(
    private val context: Context,
    private val usbManager: UsbManager
) {

    fun requestPermission(device: UsbDevice, actionUsbPermission: String) {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val permissionIntent = Intent(actionUsbPermission).apply {
            setPackage(context.packageName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            permissionIntent,
            flags
        )

        usbManager.requestPermission(device, pendingIntent)
    }

    fun getUsbDeviceFromIntent(intent: Intent): UsbDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }
    }

    fun isPermissionGranted(intent: Intent): Boolean {
        return intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
    }
}
