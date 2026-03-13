package com.ecuflasher

object CanFrameBuilder {

    fun buildObdMode01Pid00Frame(): ByteArray {
        return byteArrayOf(
            0x00, 0x00, 0x07, 0xDF.toByte(),
            0x02, 0x01, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00
        )
    }
}
