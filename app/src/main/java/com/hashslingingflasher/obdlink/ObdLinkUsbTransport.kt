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
        return ObdLinkCommandResult(
            success = false,
            responseAscii = "",
            responseHex = "",
            bytesSent = 0,
            bytesReceived = 0,
            errorMessage = "OBDLink USB raw hex passthrough not implemented yet"
        )
    }

    private fun exchange(
        port: UsbSerialPort,
        requestBytes: ByteArray,
        timeoutMs: Long,
        modeLabel: String
    ): ObdLinkCommandResult {
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
                responseAscii = String(responseBytes, StandardCharsets.US_ASCII),
                responseHex = toHex(responseBytes),
                bytesSent = requestBytes.size,
                bytesReceived = responseBytes.size,
                errorMessage = if (responseBytes.isEmpty()) {
                    "$modeLabel write succeeded but no response bytes were received"
                } else {
                    ""
                }
            )
        } catch (e: IOException) {
            ObdLinkCommandResult(
                success = false,
                responseAscii = "",
                responseHex = "",
                bytesSent = 0,
                bytesReceived = 0,
                errorMessage = e.message ?: "$modeLabel transport I/O error"
            )
        } catch (e: Exception) {
            ObdLinkCommandResult(
                success = false,
                responseAscii = "",
                responseHex = "",
                bytesSent = 0,
                bytesReceived = 0,
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
            } else if (sawAnyData) {
                break
            }
        }

        return output.toByteArray()
    }

    private fun looksLikePromptComplete(bytes: ByteArray): Boolean {
        val ascii = String(bytes, StandardCharsets.US_ASCII)
        return ascii.contains(">")
    }

    private fun noSessionResult(): ObdLinkCommandResult {
        return ObdLinkCommandResult(
            success = false,
            responseAscii = "",
            responseHex = "",
            bytesSent = 0,
            bytesReceived = 0,
            errorMessage = "No OBDLink USB session"
        )
    }

    private fun toHex(bytes: ByteArray): String {
        return bytes.joinToString(" ") { byte ->
            "%02X".format(byte.toInt() and 0xFF)
        }
    }
}
