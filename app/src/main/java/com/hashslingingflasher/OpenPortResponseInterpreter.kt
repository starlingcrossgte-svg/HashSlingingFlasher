package com.hashslingingflasher

data class OpenPortInterpretedResponse(
    val responseTypeSummary: String,
    val statusSummary: String,
    val errorSummary: String
)

class OpenPortResponseInterpreter {

    fun interpret(result: TactrixTestResult): OpenPortInterpretedResponse {
        val adapterRejected = looksLikeAdapterRejection(result.responseAscii)
        val adapterInfo = looksLikeAdapterInfo(result.responseAscii)
        val adapterStatus = looksLikeAdapterStatus(result.responseAscii)
        val adapterAck = looksLikeAdapterAcknowledgement(result.responseAscii)

        val responseTypeSummary = when {
            adapterRejected -> "Response Type: Adapter rejection"
            adapterInfo -> "Response Type: Adapter firmware info"
            adapterStatus -> "Response Type: Adapter status reply"
            adapterAck -> "Response Type: Adapter acknowledgement"
            result.responseHex.isNotBlank() -> "Response Type: Raw hex response"
            result.responseAscii.isNotBlank() -> "Response Type: OpenPort ASCII response"
            else -> "Response Type: None"
        }

        val statusSummary = when {
            adapterRejected -> "OpenPort adapter rejected command"
            result.statusMessage.isNotBlank() -> result.statusMessage
            adapterInfo -> "OpenPort adapter info received"
            adapterStatus -> "OpenPort adapter status reply received"
            adapterAck -> "OpenPort adapter acknowledged command"
            result.success && result.responseHex.isNotBlank() -> "OpenPort response received"
            result.success && result.responseAscii.isNotBlank() -> "OpenPort response received"
            else -> "No response from OpenPort"
        }

        val errorSummary = when {
            adapterRejected -> "Last Error: Adapter rejected command"
            result.success -> "Last Error: None"
            result.statusMessage.isNotBlank() -> "Last Error: ${result.statusMessage}"
            else -> "Last Error: Unknown"
        }

        return OpenPortInterpretedResponse(
            responseTypeSummary = responseTypeSummary,
            statusSummary = statusSummary,
            errorSummary = errorSummary
        )
    }

    private fun looksLikeAdapterInfo(responseAscii: String): Boolean {
        val normalized = responseAscii.trim().lowercase()
        return normalized.startsWith("ari ") ||
            normalized.contains("main code version") ||
            normalized.contains("firmware")
    }

    private fun looksLikeAdapterStatus(responseAscii: String): Boolean {
        val normalized = responseAscii.trim().lowercase()
        return normalized.startsWith("are ")
    }

    private fun looksLikeAdapterAcknowledgement(responseAscii: String): Boolean {
        val normalized = responseAscii.trim().lowercase()
        return normalized == "ok" ||
            normalized == "ok>" ||
            normalized == "ack" ||
            normalized == ">"
    }

    private fun looksLikeAdapterRejection(responseAscii: String): Boolean {
        val normalized = responseAscii.trim().lowercase()
        return normalized.contains("aro") ||
            normalized.contains("error") ||
            normalized.contains("?")
    }
}
