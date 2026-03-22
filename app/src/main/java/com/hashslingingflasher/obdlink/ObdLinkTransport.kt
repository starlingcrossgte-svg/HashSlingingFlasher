package com.hashslingingflasher.obdlink

interface ObdLinkTransport {
    fun sendAdapterAscii(command: String, timeoutMs: Long = 1000L): ObdLinkCommandResult
    fun sendRawHex(hexPayload: String, timeoutMs: Long = 1000L): ObdLinkCommandResult
}
