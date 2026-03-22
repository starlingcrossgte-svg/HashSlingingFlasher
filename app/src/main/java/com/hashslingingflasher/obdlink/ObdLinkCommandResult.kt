package com.hashslingingflasher.obdlink

data class ObdLinkCommandResult(
    val success: Boolean,
    val responseAscii: String = "",
    val responseHex: String = "",
    val bytesSent: Int = 0,
    val bytesReceived: Int = 0,
    val errorMessage: String = ""
)
