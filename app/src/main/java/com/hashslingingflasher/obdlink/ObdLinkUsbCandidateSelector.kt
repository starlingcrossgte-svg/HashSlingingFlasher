package com.hashslingingflasher.obdlink

class ObdLinkUsbCandidateSelector {

    fun pickBestCandidate(devices: List<ObdLinkUsbDeviceInfo>): ObdLinkUsbDeviceInfo? {
        return devices
            .filterNot { isKnownTactrix(it) }
            .sortedByDescending { score(it) }
            .firstOrNull()
    }

    private fun isKnownTactrix(device: ObdLinkUsbDeviceInfo): Boolean {
        return device.vendorId == 1027 && device.productId == 52301
    }

    private fun score(device: ObdLinkUsbDeviceInfo): Int {
        val manufacturer = device.manufacturerName.lowercase()
        val product = device.productName.lowercase()
        val name = device.deviceName.lowercase()

        var score = 0

        if ("obdlink" in manufacturer || "obdlink" in product || "obdlink" in name) score += 100
        if ("scantool" in manufacturer || "scantool" in product || "scantool" in name) score += 60
        if ("ftdi" in manufacturer || "ftdi" in product || "ftdi" in name) score += 40

        if (device.vendorId > 0) score += 5
        if (device.productId > 0) score += 5
        if (device.productName.isNotBlank()) score += 3
        if (device.manufacturerName.isNotBlank()) score += 3

        return score
    }
}
