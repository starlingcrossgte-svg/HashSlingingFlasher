package com.hashslingingflasher.obdlink

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.io.IOException

class ObdLinkUsbManager(
    private val usbManager: UsbManager
) {

    sealed class ConnectResult {
        data class Success(val session: ObdLinkUsbSession) : ConnectResult()
        data class Failure(val reason: String) : ConnectResult()
    }

    fun connectToDevice(
        device: UsbDevice,
        baudRate: Int = 115200
    ): ConnectResult {
        val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
            ?: return ConnectResult.Failure("No USB serial driver matched this device")

        val connection = usbManager.openDevice(device)
            ?: return ConnectResult.Failure("Android denied or dismissed USB device access")

        val port = driver.ports.firstOrNull() ?: run {
            connection.close()
            return ConnectResult.Failure("USB serial driver reported no usable ports")
        }

        return try {
            port.open(connection)
            Thread.sleep(50)
            port.setParameters(
                baudRate,
                8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )

            try {
                port.purgeHwBuffers(true, true)
            } catch (_: Exception) {
            }

            ConnectResult.Success(
                ObdLinkUsbSession(
                    device = device,
                    connection = connection,
                    port = port
                )
            )
        } catch (e: IOException) {
            try {
                port.close()
            } catch (_: Exception) {
            }
            try {
                connection.close()
            } catch (_: Exception) {
            }

            ConnectResult.Failure(
                e.message ?: "USB serial open/configure failed"
            )
        } catch (e: InterruptedException) {
            try {
                port.close()
            } catch (_: Exception) {
            }
            try {
                connection.close()
            } catch (_: Exception) {
            }

            Thread.currentThread().interrupt()
            ConnectResult.Failure("USB serial open interrupted")
        }
    }

    fun disconnect(session: ObdLinkUsbSession) {
        try {
            session.port.close()
        } catch (_: Exception) {
        }

        try {
            session.connection.close()
        } catch (_: Exception) {
        }
    }
}
