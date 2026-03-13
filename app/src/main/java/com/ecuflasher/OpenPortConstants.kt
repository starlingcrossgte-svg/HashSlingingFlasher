package com.ecuflasher

object OpenPortConstants {

    const val TACTRIX_VENDOR_ID = 1027
    const val TACTRIX_PRODUCT_ID = 52301

    const val READ_TIMEOUT_MS = 4000
    const val WRITE_TIMEOUT_MS = 3000

    const val COMMAND_ATA = "ata\r\n"
    const val COMMAND_ATO_CAN_500K = "ato6 0 500000 0\r\n"

    const val SESSION_OPENPORT_COMMAND_ATA = "ATA"
    const val SESSION_BUS_MODE_CAN_500K = "CAN 500k"
    const val SESSION_ECU_QUERY_MODE_01_PID_00 = "OBD Mode 01 PID 00"
}
