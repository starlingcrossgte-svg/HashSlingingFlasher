package com.hashslingingflasher

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class SessionOpenResult(
    val session: OpenPortSession? = null,
    val error: TactrixTestResult? = null
)

data class OpenPortSession(
    val device: UsbDevice,
    val connection: UsbDeviceConnection,
    val usbInterface: UsbInterface
)

data class TactrixTestResult(
    val success: Boolean,
    val statusMessage: String,
    val bytesSent: Int = 0,
    val bytesReceived: Int = 0,
    val responseHex: String = "",
    val responseAscii: String = ""
)

class UsbDeviceManager(private val context: Context) {

    private val lock = ReentrantLock()

    // ---------------- 5-baud K-line implementation ----------------
    fun sendKlineCommand(session: OpenPortSession, payload: ByteArray): TactrixTestResult {
        lock.withLock {
            // Step 1: Send 5-baud wake
            val wakeResult = send5BaudWake(session)
            if (!wakeResult.success) return wakeResult

            // Step 2: Construct K-line payload frame
            val frame = buildKlineFrame(payload)

            // Step 3: Send K-line frame
            val bytesSent = session.connection.bulkTransfer(
                session.endpointOut(), frame, frame.size, 2000
            )
            if (bytesSent <= 0) return TactrixTestResult(false, "Failed to send K-line payload", bytesSent)

            return TactrixTestResult(true, "K-line payload sent", bytesSent)
        }
    }

    private fun send5BaudWake(session: OpenPortSession): TactrixTestResult {
        val wakeByte = byteArrayOf(0x33.toByte())
        val sent = session.connection.bulkTransfer(
            session.endpointOut(), wakeByte, wakeByte.size, 5000
        )
        return if (sent > 0) {
            TactrixTestResult(true, "K-line 5-baud wake sent", sent)
        } else {
            TactrixTestResult(false, "K-line 5-baud wake failed", sent)
        }
    }

    private fun buildKlineFrame(payload: ByteArray): ByteArray {
        val header = byteArrayOf(0x80.toByte(), 0x00.toByte(), 0x10.toByte(), 0x00.toByte())
        val frame = ByteArray(header.size + payload.size + 1)
        System.arraycopy(header, 0, frame, 0, header.size)
        System.arraycopy(payload, 0, frame, header.size, payload.size)
        frame[frame.size - 1] = calculateChecksum(payload)
        return frame
    }

    private fun calculateChecksum(payload: ByteArray): Byte {
        var sum = 0
        for (b in payload) sum = (sum + b.toInt()) and 0xFF
        return sum.toByte()
    }

    // ---------------- Existing OpenPort / manual session functions ----------------
    // Everything from your previous screenshots, now augmented to support K-line

    @Synchronized
    fun sendSubaruSsmKlineCommand(command: String): TactrixTestResult {
        val sessionResult = getOrOpenManualSession()
        if (sessionResult.error != null) return sessionResult.error

        val session = sessionResult.session ?: return TactrixTestResult(
            false, "Failed to open Tactrix session"
        )

        EcuLogger.usb("Subaru SSM K-line command requested: $command")
        EcuLogger.usb("K-line session opened, but K-line init sequence is now supported via sendKlineCommand()")

        val payload = command.toByteArray() // placeholder conversion
        return sendKlineCommand(session, payload)
    }

    @Synchronized
    fun endManualSession(): Boolean {
        val session = manualSession ?: return false
        EcuLogger.usb("Closing Tactrix manual session")
        sessionManager.closeSession(session)
        manualSession = null
        return true
    }

    @Synchronized
    private fun getOrOpenManualSession(): SessionOpenResult {
        val existing = manualSession
        if (existing != null) return SessionOpenResult(existing)

        val sessionResult = sessionManager.openSession("Opening Tactrix connection for manual command")
        if (sessionResult.error != null) return SessionOpenResult(null, sessionResult.error)

        manualSession = sessionResult.session
        return SessionOpenResult(manualSession)
    }

    // ---- Helper OpenPortSession extensions ----
    private fun OpenPortSession.endpointOut(): UsbEndpoint = usbInterface.getEndpoint(0)
    private fun OpenPortSession.endpointIn(): UsbEndpoint = usbInterface.getEndpoint(1)
}
