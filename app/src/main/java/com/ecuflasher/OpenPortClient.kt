package com.ecuflasher

import android.hardware.usb.UsbEndpoint

class OpenPortClient(
    private val connection: android.hardware.usb.UsbDeviceConnection,
    private val endpointOut: UsbEndpoint,
    private val endpointIn: UsbEndpoint
) {

    companion object {
        private const val READ_TIMEOUT_MS = 4000
        private const val WRITE_TIMEOUT_MS = 3000
    }

    data class CommandResult(
        val bytesSent: Int,
        val bytesReceived: Int,
        val responseHex: String,
        val responseAscii: String,
        val responseBytes: ByteArray
    )

    fun openCommandChannel(): CommandResult {
        SessionSummaryStore.setOpenPortCommand("ATA")
        return sendAsciiCommand(
            commandLabel = "OpenPort ATA command",
            commandString = "ata\r\n"
        )
    }

    fun openCanBus500k(): CommandResult {
        SessionSummaryStore.setBusMode("CAN 500k")
        return sendAsciiCommand(
            commandLabel = "OpenPort ATO CAN command",
            commandString = "ato6 0 500000 0\r\n"
        )
    }

    fun sendObdCanQuery0100(): CommandResult {
        SessionSummaryStore.setEcuQuery("OBD Mode 01 PID 00")

        val canFrame = byteArrayOf(
            0x00, 0x00, 0x07, 0xDF.toByte(),
            0x02, 0x01, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00
        )

        val header = "att6 ${canFrame.size} 0\r\n".toByteArray(Charsets.US_ASCII)
        val packet = ByteArray(header.size + canFrame.size)

        System.arraycopy(header, 0, packet, 0, header.size)
        System.arraycopy(canFrame, 0, packet, header.size, canFrame.size)

        EcuLogger.usb("Sending ECU OBD CAN query")
        EcuLogger.usb("Command text: att6 ${canFrame.size} 0\\r\\n + raw CAN frame")
        EcuLogger.usb("CAN frame hex: ${toHex(canFrame)}")
        EcuLogger.usb("Packet length: ${packet.size}")
        EcuLogger.usb("Packet hex: ${toHex(packet)}")
        EcuLogger.usb("Write timeout ms: $WRITE_TIMEOUT_MS")
        EcuLogger.usb("Read timeout ms: $READ_TIMEOUT_MS")

        val sent = connection.bulkTransfer(
            endpointOut,
            packet,
            packet.size,
            WRITE_TIMEOUT_MS
        )

        EcuLogger.usb("Bytes sent: $sent")

        val buffer = ByteArray(256)
        val received = connection.bulkTransfer(
            endpointIn,
            buffer,
            buffer.size,
            READ_TIMEOUT_MS
        )

        EcuLogger.usb("Bytes received: $received")

        val responseBytes = if (received > 0) {
            buffer.copyOf(received)
        } else {
            byteArrayOf()
        }

        val responseHex = if (received > 0) {
            toHex(responseBytes)
        } else {
            ""
        }

        val responseAscii = if (received > 0) {
            String(responseBytes, Charsets.US_ASCII)
        } else {
            ""
        }

        if (received > 0) {
            EcuLogger.usb("Response bytes: $responseHex")
            EcuLogger.usb("Response ascii: $responseAscii")
            SessionSummaryStore.setResponseType("Vehicle response received")
            SessionSummaryStore.setError("None")
        } else if (received == 0) {
            EcuLogger.usb("No data returned")
            SessionSummaryStore.setResponseType("No data returned")
            SessionSummaryStore.setError("None")
        } else {
            EcuLogger.usb("Read timed out or no ECU response")
            SessionSummaryStore.setResponseType("No ECU response")
            SessionSummaryStore.setError("Read timeout")
        }

        return CommandResult(
            bytesSent = sent,
            bytesReceived = received,
            responseHex = responseHex,
            responseAscii = responseAscii,
            responseBytes = responseBytes
        )
    }

    private fun sendAsciiCommand(
        commandLabel: String,
        commandString: String
    ): CommandResult {
        val packet = commandString.toByteArray(Charsets.US_ASCII)

        EcuLogger.usb("Sending $commandLabel")
        EcuLogger.usb("Command text: ${commandString.replace("\r", "\\r").replace("\n", "\\n")}")
        EcuLogger.usb("Packet length: ${packet.size}")
        EcuLogger.usb("Packet hex: ${toHex(packet)}")
        EcuLogger.usb("Write timeout ms: $WRITE_TIMEOUT_MS")
        EcuLogger.usb("Read timeout ms: $READ_TIMEOUT_MS")

        val sent = connection.bulkTransfer(
            endpointOut,
            packet,
            packet.size,
            WRITE_TIMEOUT_MS
        )

        EcuLogger.usb("Bytes sent: $sent")

        val buffer = ByteArray(128)
        val received = connection.bulkTransfer(
            endpointIn,
            buffer,
            buffer.size,
            READ_TIMEOUT_MS
        )

        EcuLogger.usb("Bytes received: $received")

        val responseBytes = if (received > 0) {
            buffer.copyOf(received)
        } else {
            byteArrayOf()
        }

        val responseHex = if (received > 0) {
            toHex(responseBytes)
        } else {
            ""
        }

        val responseAscii = if (received > 0) {
            String(responseBytes, Charsets.US_ASCII)
        } else {
            ""
        }

        if (received > 0) {
            EcuLogger.usb("Response bytes: $responseHex")
            EcuLogger.usb("Response ascii: $responseAscii")
            SessionSummaryStore.setResponseType("OpenPort response received")
            SessionSummaryStore.setError("None")
        } else if (received == 0) {
            EcuLogger.usb("No data returned")
            SessionSummaryStore.setResponseType("No data returned")
            SessionSummaryStore.setError("None")
        } else {
            EcuLogger.usb("Read timed out or no response from device")
            SessionSummaryStore.setResponseType("No OpenPort response")
            SessionSummaryStore.setError("Read timeout")
        }

        return CommandResult(
            bytesSent = sent,
            bytesReceived = received,
            responseHex = responseHex,
            responseAscii = responseAscii,
            responseBytes = responseBytes
        )
    }

    private fun toHex(data: ByteArray): String {
        return data.joinToString(" ") {
            "%02X".format(it.toInt() and 0xFF)
        }
    }
}
