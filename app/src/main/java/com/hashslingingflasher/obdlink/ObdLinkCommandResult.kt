package com.hashslingingflasher.obdlink

data class ObdLinkCommandResult(
    val success: Boolean,
    val requestAscii: String = "",
    val requestHex: String = "",
    val responseAscii: String = "",
    val responseHex: String = "",
    val bytesSent: Int = 0,
    val bytesReceived: Int = 0,
    val modeLabel: String = "",
    val errorMessage: String = ""
)
