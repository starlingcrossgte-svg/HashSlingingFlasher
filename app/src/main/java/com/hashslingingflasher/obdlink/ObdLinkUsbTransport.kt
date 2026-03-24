package com.hashslingingflasher.obdlink

import com.hoho.android.usbserial.driver.UsbSerialPort
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlin.math.min

class ObdLinkUsbTransport : ObdLinkTransport {

    private var session: ObdLinkUsbSession? = null

    fun attachSession(session: ObdLinkUsbSession) {
        this.session = session
    }

    fun clearSession() {
        session = null
    }

    fun hasSession(): Boolean {
        return session != null
    }

    override fun sendAdapterAscii(command: String, timeoutMs: Long): ObdLinkCommandResult {
        val activeSession = session ?: return noSessionResult()

        val normalizedCommand = if (command.endsWith("\r")) {
            command
        } else {
            "$command\r"
        }

        return exchange(
            port = activeSession.port,
            requestBytes = normalizedCommand.toByteArray(StandardCharsets.US_ASCII),
            timeoutMs = timeoutMs,
            modeLabel = "OBDLink USB ASCII"
        )
    }

    override fun sendRawHex(hexPayload: String, timeoutMs: Long): ObdLinkCommandResult {
        val activeSession = session ?: return noSessionResult()

        val sanitizedHex = hexPayload
            .replace(" ", "")
            .replace("\n", "")
            .replace("\r", "")
            .uppercase()

        if (sanitizedHex.isEmpty()) {
            return ObdLinkCommandResult(
                success = false,
                requestAscii = hexPayload,
                requestHex = "",
                responseAscii = "",
                responseHex = "",
                bytesSent = 0,
                bytesReceived = 0,
                modeLabel = "OBDLink USB RAW HEX",
                errorMessage = "Raw hex payload is empty"
            )
        }

        if (sanitizedHex.length % 2 != 0) {
            return ObdLinkCommandResult(
                success = false,
                requestAscii = sanitizedHex,
                requestHex = "",
                responseAscii = "",
                responseHex = "",
                bytesSent = 0,
                bytesReceived = 0,
                modeLabel = "OBDLink USB RAW HEX",
                errorMessage = "Raw hex payload must contain an even number of hex characters"
            )
        }

        if (!sanitizedHex.all { it in '0'..'9' || it in 'A'..'F' }) {
            return ObdLinkCommandResult(
                success = false,
                requestAscii = sanitizedHex,
                requestHex = "",
                responseAscii = "",
                responseHex = "",
                bytesSent = 0,
                bytesReceived = 0,
                modeLabel = "OBDLink USB RAW HEX",
                errorMessage = "Raw hex payload contains non-hex characters"
            )
        }

        val normalizedCommand = "$sanitizedHex\r"

        return exchange(
            port = activeSession.port,
            requestBytes = normalizedCommand.toByteArray(StandardCharsets.US_ASCII),
            timeoutMs = timeoutMs,
            modeLabel = "OBDLink USB RAW HEX"
        )
    }

    private fun exchange(
        port: UsbSerialPort,
        requestBytes: ByteArray,
        timeoutMs: Long,
        modeLabel: String
    ): ObdLinkCommandResult {
        val requestAscii = String(requestBytes, StandardCharsets.US_ASCII)
        val requestHex = toHex(requestBytes)

        return try {
            val safeTimeoutMs = timeoutMs.coerceAtLeast(1L)
            val timeoutInt = safeTimeoutMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

            port.write(requestBytes, timeoutInt)

            val responseBytes = readUntilPromptOrTimeout(
                port = port,
                timeoutMs = safeTimeoutMs
            )

            ObdLinkCommandResult(
                success = responseBytes.isNotEmpty(),
                requestAscii = requestAscii,
                requestHex = requestHex,
                responseAscii = String(responseBytes, StandardCharsets.US_ASCII),
                responseHex = toHex(responseBytes),
                bytesSent = requestBytes.size,
                bytesReceived = responseBytes.size,
                modeLabel = modeLabel,
                errorMessage = if (responseBytes.isEmpty()) {
                    "$modeLabel write succeeded but no response bytes were received"
                } else {
                    ""
                }
            )
        } catch (e: IOException) {
            ObdLinkCommandResult(
                success = false,
                requestAscii = requestAscii,
                requestHex = requestHex,
                responseAscii = "",
                responseHex = "",
                bytesSent = requestBytes.size,
                bytesReceived = 0,
                modeLabel = modeLabel,
                errorMessage = e.message ?: "$modeLabel transport I/O error"
            )
        } catch (e: Exception) {
            ObdLinkCommandResult(
                success = false,
                requestAscii = requestAscii,
                requestHex = requestHex,
                responseAscii = "",
                responseHex = "",
                bytesSent = requestBytes.size,
                bytesReceived = 0,
                modeLabel = modeLabel,
                errorMessage = e.message ?: "$modeLabel transport error"
            )
        }
    }

    private fun readUntilPromptOrTimeout(
        port: UsbSerialPort,
        timeoutMs: Long
    ): ByteArray {
        val deadlineMs = System.currentTimeMillis() + timeoutMs
        val output = ByteArrayOutputStream()
        val chunk = ByteArray(256)
        var sawAnyData = false

        while (System.currentTimeMillis() < deadlineMs) {
            val remainingMs = (deadlineMs - System.currentTimeMillis()).coerceAtLeast(1L)
            val sliceTimeoutMs = min(remainingMs, 150L).toInt()

            val bytesRead = try {
                port.read(chunk, chunk.size, sliceTimeoutMs)
            } catch (e: IOException) {
                if (sawAnyData) {
                    break
                }
                throw e
            }

            if (bytesRead > 0) {
                output.write(chunk, 0, bytesRead)
                sawAnyData = true

                if (looksLikePromptComplete(output.toByteArray())) {
                    break
                }
            }
        }

        return output.toByteArray()
    }

    private fun looksLikePromptComplete(bytes: ByteArray): Boolean {
        return bytes.any { it == 0x3E.toByte() }
    }

    private fun noSessionResult(): ObdLinkCommandResult {
        return ObdLinkCommandResult(
            success = false,
            requestAscii = "",
            requestHex = "",
            responseAscii = "",
            responseHex = "",
            bytesSent = 0,
            bytesReceived = 0,
            modeLabel = "",
            errorMessage = "No OBDLink USB session"
        )
    }

    private fun toHex(bytes: ByteArray): String {
        return bytes.joinToString(" ") { byte ->
            "%02X".format(byte.toInt() and 0xFF)
        }
    }
}
