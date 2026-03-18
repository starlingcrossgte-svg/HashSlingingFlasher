package com.hashslingingflasher

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class OpenPortStatusRefresher(
    private val usbManager: UsbManager,
    private val usbPermissionHelper: UsbPermissionHelper,
    private val openPortStatusPresenter: OpenPortStatusPresenter,
    private val requestUsbPermission: (UsbDevice) -> Unit,
    private val onCurrentDeviceChanged: (UsbDevice?) -> Unit
) {
    fun refresh() {
        val tactrixDevice = usbManager.deviceList.values.firstOrNull {
            usbPermissionHelper.isTactrixDevice(it)
        }

        onCurrentDeviceChanged(tactrixDevice)

        if (tactrixDevice == null) {
            openPortStatusPresenter.showDeviceNotDetected()
            EcuLogger.usb("Tactrix device not found")
            openPortStatusPresenter.refreshDeveloperLog()
            return
        }

        if (usbManager.hasPermission(tactrixDevice)) {
            openPortStatusPresenter.showDeviceDetectedPermissionGranted()
            openPortStatusPresenter.resetCommandDisplayToNeutral()
            EcuLogger.usb("USB permission already granted")
        } else {
            openPortStatusPresenter.showDeviceDetectedPermissionPending()
            EcuLogger.usb("Requesting USB permission for Tactrix")
            requestUsbPermission(tactrixDevice)
        }

        openPortStatusPresenter.refreshDeveloperLog()
    }
}
