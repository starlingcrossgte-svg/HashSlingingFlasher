package com.hashslingingflasher.obdlink

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.io.IOException

class ObdLinkUsbManager(
    private val usbManager: UsbManager
) {

    fun connectToDevice(device: UsbDevice): ObdLinkUsbSession? {
        val driver = UsbSerialProber.getDefaultProber().probeDevice(device) ?: return null
        val connection = usbManager.openDevice(device) ?: return null
        val port = driver.ports.firstOrNull() ?: run {
            connection.close()
            return null
        }

        return try {
            port.open(connection)
            port.setParameters(
                115200,
                8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )
              try {
                  port.purgeHwBuffers(true, true)
              } catch (_: Exception) {
              }
            ObdLinkUsbSession(
                device = device,
                connection = connection,
                port = port
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
            null
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
