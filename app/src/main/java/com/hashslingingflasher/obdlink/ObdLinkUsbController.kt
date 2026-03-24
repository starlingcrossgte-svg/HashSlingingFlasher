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

    fun listAttachedDevices(): List<ObdLinkUsbDeviceInfo> = discovery.listAttachedDevices()

    fun findBestCandidate(): ObdLinkUsbDeviceInfo? {
        return selector.pickBestCandidate(listAttachedDevices())
    }

    fun hasPermission(deviceName: String): Boolean {
        val device = discovery.findDeviceByName(deviceName) ?: return false
        return usbManager.hasPermission(device)
    }

    fun requestPermission(deviceName: String) {
        val device = discovery.findDeviceByName(deviceName) ?: return
        // Use a consistent Action string to prevent duplicate receiver collisions
        permissionHelper.requestPermission(device, ObdLinkUsbActions.ACTION_USB_PERMISSION)
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

    /**
     * Attempts to connect to the device and automatically attaches the session to the transport.
     * Returns the rich ConnectResult instead of a simple Boolean.
     */
    fun connectByDeviceName(deviceName: String): ObdLinkUsbManager.ConnectResult {
        val device = discovery.findDeviceByName(deviceName) 
            ?: return ObdLinkUsbManager.ConnectResult.Failure("Device '$deviceName' not found")

        if (!usbManager.hasPermission(device)) {
            return ObdLinkUsbManager.ConnectResult.Failure("USB Permission not granted for '$deviceName'")
        }

        // Attempt the physical connection
        return when (val result = sessionManager.connectToDevice(device)) {
            is ObdLinkUsbManager.ConnectResult.Success -> {
                // Auto-sync the session to the transport layer
                activeSession = result.session
                transport.attachSession(result.session)
                result
            }
            is ObdLinkUsbManager.ConnectResult.Failure -> {
                // Connection failed; ensure transport is cleared
                disconnect() 
                result
            }
        }
    }

    fun disconnect() {
        activeSession?.let { sessionManager.disconnect(it) }
        activeSession = null
        transport.clearSession()
    }

    fun transport(): ObdLinkTransport = transport

    fun isConnected(): Boolean = activeSession != null && transport.hasSession()
}
