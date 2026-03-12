package com.ecuflasher

import android.util.Log

object EcuLogger {

    private const val TAG_MAIN = "ECU_MAIN"
    private const val TAG_USB = "ECU_USB"
    private const val TAG_COMM = "ECU_COMM"
    private const val TAG_FLASH = "ECU_FLASH"

    fun main(message: String) {
        Log.d(TAG_MAIN, message)
    }

    fun usb(message: String) {
        Log.d(TAG_USB, message)
    }

    fun comm(message: String) {
        Log.d(TAG_COMM, message)
    }

    fun flash(message: String) {
        Log.d(TAG_FLASH, message)
    }

    fun error(message: String) {
        Log.e("ECU_ERROR", message)
    }
}
