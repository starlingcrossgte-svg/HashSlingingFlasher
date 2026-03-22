package com.hashslingingflasher.obdlink

import android.content.Context
import android.hardware.usb.UsbManager

class ObdLinkUsbController(
    private val context: Context,
    private val usbManager: UsbManager
) {
    private val discovery = ObdLinkUsbDiscovery(usbManager)
    private val selector = ObdLinkUsbCandidateSelector()
    private val permissionHelper = ObdLinkUsbPermissionHelper(context, usbManager)
    private val sessionManager = ObdLinkUsbManager(usbManager)
    private val transport = ObdLinkUsbTransport()

    private var activeSession: ObdLinkUsbSession? = null

    fun listAttachedDevices(): List<ObdLinkUsbDeviceInfo> {
        return discovery.listAttachedDevices()
    }

    fun findBestCandidate(): ObdLinkUsbDeviceInfo? {
        return selector.pickBestCandidate(listAttachedDevices())
    }

    fun hasPermission(deviceName: String): Boolean {
        val device = discovery.findDeviceByName(deviceName) ?: return false
        return usbManager.hasPermission(device)
    }

    fun requestPermission(deviceName: String): Boolean {
        val device = discovery.findDeviceByName(deviceName) ?: return false
        permissionHelper.requestPermission(device, ObdLinkUsbActions.ACTION_USB_PERMISSION)
        return true
    }

    fun requestPermissionForBestCandidate(): ObdLinkUsbDeviceInfo? {
        val candidate = findBestCandidate() ?: return null
        requestPermission(candidate.deviceName)
        return candidate
    }

    fun createPermissionRegistrar(
        onPermissionResult: (deviceName: String?, granted: Boolean) -> Unit
    ): ObdLinkUsbPermissionRegistrar {
        return ObdLinkUsbPermissionRegistrar(
            context = context,
            permissionHelper = permissionHelper,
            onPermissionResult = onPermissionResult
        )
    }

    fun connectByDeviceName(deviceName: String): Boolean {
        val device = discovery.findDeviceByName(deviceName) ?: return false
        if (!usbManager.hasPermission(device)) return false

        val session = sessionManager.connectToDevice(device) ?: return false
        activeSession = session
        transport.attachSession(session)
        return true
    }

    fun disconnect() {
        activeSession?.let { session ->
            sessionManager.disconnect(session)
        }
        activeSession = null
        transport.clearSession()
    }

    fun transport(): ObdLinkTransport {
        return transport
    }

    fun isConnected(): Boolean {
        return activeSession != null && transport.hasSession()
    }
}
