package com.hashslingingflasher

import android.widget.TextView

class OpenPortStatusPresenter(
    private val deviceStateText: TextView,
    private val permissionStateText: TextView,
    private val statusMessageText: TextView,
    private val lastCommandText: TextView,
    private val bytesSentText: TextView,
    private val bytesReceivedText: TextView,
    private val responseHexText: TextView,
    private val summaryOpenPortCommandText: TextView,
    private val summaryBusModeText: TextView,
    private val summaryEcuQueryText: TextView,
    private val summaryResponseTypeText: TextView,
    private val summaryErrorText: TextView,
    private val manualCommandResponseText: TextView,
    private val developerLogText: TextView
) {
    fun showDeviceNotDetected() {
        deviceStateText.text = "Device: Tactrix OpenPort not detected"
        permissionStateText.text = "Permission: Unknown"
        statusMessageText.text = "USB status unknown"

        lastCommandText.text = "Last Command: None"
        bytesSentText.text = "Bytes Sent: -"
        bytesReceivedText.text = "Bytes Received: -"
        responseHexText.text = "No response yet"
        manualCommandResponseText.text = "No response yet"

        summaryOpenPortCommandText.text = "OpenPort Command: None"
        summaryBusModeText.text = "Bus Mode: None"
        summaryEcuQueryText.text = "Interrogation Path: None"
        summaryResponseTypeText.text = "Response Type: None"
        summaryErrorText.text = "Last Error: Device not detected"
    }

    fun showDeviceDisconnected() {
        deviceStateText.text = "Device: Tactrix OpenPort not detected"
        permissionStateText.text = "Permission: Unknown"
        statusMessageText.text = "USB status unknown"

        lastCommandText.text = "Last Command: None"
        bytesSentText.text = "Bytes Sent: -"
        bytesReceivedText.text = "Bytes Received: -"
        responseHexText.text = "No response yet"
        manualCommandResponseText.text = "No response yet"

        summaryOpenPortCommandText.text = "OpenPort Command: None"
        summaryBusModeText.text = "Bus Mode: None"
        summaryEcuQueryText.text = "Interrogation Path: None"
        summaryResponseTypeText.text = "Response Type: None"
        summaryErrorText.text = "Last Error: Device disconnected"
    }

    fun showDeviceDetectedPermissionGranted() {
        deviceStateText.text = "Device: Tactrix OpenPort detected"
        permissionStateText.text = "Permission: Granted"
        statusMessageText.text = "OpenPort ready"
    }

    fun showDeviceDetectedPermissionPending() {
        deviceStateText.text = "Device: Tactrix OpenPort detected"
        permissionStateText.text = "Permission: Pending"
        statusMessageText.text = "Requesting USB permission..."
    }

    fun resetCommandDisplayToNeutral() {
        lastCommandText.text = "Last Command: None"
        bytesSentText.text = "Bytes Sent: -"
        bytesReceivedText.text = "Bytes Received: -"
        responseHexText.text = "No response yet"
        manualCommandResponseText.text = "No response yet"

        summaryOpenPortCommandText.text = "OpenPort Command: None"
        summaryBusModeText.text = "Bus Mode: None"
        summaryEcuQueryText.text = "Interrogation Path: None"
        summaryResponseTypeText.text = "Response Type: None"
        summaryErrorText.text = "Last Error: None"
    }

    fun refreshDeveloperLog() {
        developerLogText.text = EcuLogger.getLogs().ifEmpty { "No logs yet" }
    }
}
