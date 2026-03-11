        val status = if (deviceList.isNotEmpty()) {
            UsbStatus.CONNECTED
        } else {
            UsbStatus.DISCONNECTED
        }

        updateUsbStatus(status)
    }

    private fun updateUsbStatus(status: UsbStatus) {
        // Inline UI update without refreshing the activity
        usbStatusText.text = when (status) {
            UsbStatus.CONNECTED -> "USB Device Connected"
            UsbStatus.DISCONNECTED -> "No USB Device Connected"
            UsbStatus.UNKNOWN -> "USB Status Unknown"
        }
    }
}
