package com.hashslingingflasher.obdlink

data class ObdLinkUsbDeviceInfo(
    val vendorId: Int,
    val productId: Int,
    val deviceName: String = "",
    val manufacturerName: String = "",
    val productName: String = ""
)
