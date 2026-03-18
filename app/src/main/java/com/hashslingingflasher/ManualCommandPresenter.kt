package com.hashslingingflasher

import android.widget.TextView
import com.hashslingingflasher.OpenPortInterrogationResult

class ManualCommandPresenter(
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
    private val manualCommandResponseText: TextView
) {

    fun showNoDeviceConnected() {
        statusMessageText.text = "OpenPort not detected"
        manualCommandResponseText.text = "No device connected"
        summaryErrorText.text = "Last Error: Device not detected"
    }

    fun showPermissionRequired() {
        statusMessageText.text = "Requesting USB permission..."
        manualCommandResponseText.text = "Permission required"
    }

    fun showCommandSending(command: String, profile: OpenPortCommandProfile) {
        lastCommandText.text = "Last Command: $command"
        summaryOpenPortCommandText.text = "OpenPort Command: ${profile.displayCommand}"
        summaryBusModeText.text = profile.busModeSummary
        summaryEcuQueryText.text =
            "${profile.commandFamilySummary} | ${profile.sendSequenceSummary}"

        statusMessageText.text = "Sending command..."
        manualCommandResponseText.text = "Waiting for OpenPort response..."
        responseHexText.text = "No response yet"
        bytesSentText.text = "Bytes Sent: -"
        bytesReceivedText.text = "Bytes Received: -"
        summaryResponseTypeText.text = "Response Type: None"
        summaryErrorText.text = "Last Error: None"
    }

    fun showCommandResult(result: OpenPortInterrogationResult) {
        bytesSentText.text = "Bytes Sent: ${result.transportResult.bytesSent}"
        bytesReceivedText.text = "Bytes Received: ${result.transportResult.bytesReceived}"

        responseHexText.text = if (result.transportResult.responseHex.isNotEmpty()) {
            result.transportResult.responseHex
        } else {
            "No response yet"
        }

        manualCommandResponseText.text = when {
            result.transportResult.responseAscii.isNotEmpty() ->
                result.transportResult.responseAscii.trim()

            result.transportResult.responseHex.isNotEmpty() ->
                result.transportResult.responseHex

            else ->
                result.transportResult.statusMessage
        }

        statusMessageText.text = result.statusSummary
        summaryOpenPortCommandText.text =
            "OpenPort Command: ${result.profile.displayCommand}"
        summaryBusModeText.text = result.profile.busModeSummary
        summaryEcuQueryText.text =
            "${result.profile.commandFamilySummary} | ${result.profile.sendSequenceSummary}"
        summaryResponseTypeText.text = result.responseTypeSummary
        summaryErrorText.text = result.errorSummary
    }
}
