package com.hashslingingflasher

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import java.nio.charset.Charset

data class OpenPortCommandResult(
    val bytesSent: Int,
    val bytesReceived: Int,
    val responseHex: String,
    val responseAscii: String,
    val responseBytes: ByteArray
)

class OpenPortTransport {
    companion object {
        private const val READ_TIMEOUT_MS = 4000
        private const val WRITE_TIMEOUT_MS = 3000
    }

    fun sendAsciiCommand(
        connection: UsbDeviceConnection,
        endpointOut: UsbEndpoint,
        endpointIn: UsbEndpoint,
        commandLabel: String,
        commandString: String
    ): OpenPortCommandResult {
        val packet = commandString.toByteArray(Charset.forName("US-ASCII"))

        EcuLogger.usb("Sending $commandLabel")
        EcuLogger.usb(
            "Command text: ${commandString.replace("\r", "\\r").replace("\n", "\\n")}"
        )

        return sendRawPacket(
            connection = connection,
            endpointOut = endpointOut,
            endpointIn = endpointIn,
            packetLabel = "Packet",
            packet = packet,
            noDataMessage = "No data returned",
            timeoutMessage = "Read timed out or no response from device"
        )
    }

    fun sendRawPacket(
        connection: UsbDeviceConnection,
        endpointOut: UsbEndpoint,
        endpointIn: UsbEndpoint,
        packetLabel: String,
        packet: ByteArray,
        noDataMessage: String,
        timeoutMessage: String
    ): OpenPortCommandResult {
        EcuLogger.usb("PacketLabel length: ${packetLabel.length}")
        EcuLogger.usb("PacketLabel hex: ${toHex(packetLabel.toByteArray())}")
        EcuLogger.usb("Write timeout ms: $WRITE_TIMEOUT_MS")
        EcuLogger.usb("Read timeout ms: $READ_TIMEOUT_MS")

        val sent = connection.bulkTransfer(
            endpointOut,
            packet,
            packet.size,
            WRITE_TIMEOUT_MS
        )

        EcuLogger.usb("Bytes sent: $sent")
        val firstBuffer = ByteArray(256)
        val firstReceived = connection.bulkTransfer(
            endpointIn,
            firstBuffer,
            firstBuffer.size,
            READ_TIMEOUT_MS
        )

        EcuLogger.usb("Bytes received: $firstReceived")

        val firstResponseBytes = if (firstReceived > 0) {
            firstBuffer.copyOf(firstReceived)
        } else {
            byteArrayOf()
        }

        val looksLikeEcho =
            firstReceived == packet.size &&
            firstReceived > 0 &&
            firstResponseBytes.contentEquals(packet)

        val received: Int
        val responseBytes: ByteArray

        if (looksLikeEcho) {
            EcuLogger.usb("Echo detected; discarding first read and waiting for device reply")

            val secondBuffer = ByteArray(256)
            val secondReceived = connection.bulkTransfer(
                endpointIn,
                secondBuffer,
                secondBuffer.size,
                READ_TIMEOUT_MS
            )

            EcuLogger.usb("Bytes received after echo discard: $secondReceived")

            received = secondReceived
            responseBytes = if (secondReceived > 0) {
                secondBuffer.copyOf(secondReceived)
            } else {
                byteArrayOf()
            }
        } else {
            received = firstReceived
            responseBytes = firstResponseBytes
        }

        val responseHex = if (received > 0) {
            toHex(responseBytes)
        } else {
            ""
        }

        val responseAscii = if (received > 0) {
            String(responseBytes, Charset.forName("US-ASCII"))
        } else {
            ""
        }

        if (looksLikeEcho && received <= 0) {
            EcuLogger.usb("No device reply received after echo discard")
        }

        if (received > 0) {
            EcuLogger.usb("Response bytes: $responseHex")
            EcuLogger.usb("Response ascii: $responseAscii")
        } else if (received == 0) {
            EcuLogger.usb(noDataMessage)
        } else {
            EcuLogger.usb(timeoutMessage)
        }

        return OpenPortCommandResult(
            bytesSent = sent,
            bytesReceived = received,
            responseHex = responseHex,
            responseAscii = responseAscii,
            responseBytes = responseBytes
        )
    }

    fun toHex(bytes: ByteArray): String {
        return bytes.joinToString(" ") { "%02X".format(it) }
    }
}
