package com.hashslingingflasher.obdlink

import android.content.Context
import android.content.IntentFilter
import android.os.Build

class ObdLinkUsbPermissionRegistrar(
    private val context: Context,
    private val permissionHelper: ObdLinkUsbPermissionHelper,
    private val onPermissionResult: (deviceName: String?, granted: Boolean) -> Unit
) {
    private var receiver: ObdLinkUsbPermissionReceiver? = null

    fun register() {
        if (receiver != null) return

        val permissionReceiver = ObdLinkUsbPermissionReceiver(
            permissionHelper = permissionHelper,
            onPermissionResult = onPermissionResult
        )

        val filter = IntentFilter(ObdLinkUsbActions.ACTION_USB_PERMISSION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                permissionReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(permissionReceiver, filter)
        }

        receiver = permissionReceiver
    }

    fun unregister() {
        val activeReceiver = receiver ?: return
        try {
            context.unregisterReceiver(activeReceiver)
        } catch (_: Exception) {
        }
        receiver = null
    }
}
