package com.hashslingingflasher.obdlink

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ObdLinkUsbPermissionReceiver(
    private val permissionHelper: ObdLinkUsbPermissionHelper,
    private val onPermissionResult: (deviceName: String?, granted: Boolean) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val activeIntent = intent ?: return
        if (activeIntent.action != ObdLinkUsbActions.ACTION_USB_PERMISSION) return

        val device = permissionHelper.getUsbDeviceFromIntent(activeIntent)
        val granted = permissionHelper.isPermissionGranted(activeIntent)

        onPermissionResult(device?.deviceName, granted)
    }
}
