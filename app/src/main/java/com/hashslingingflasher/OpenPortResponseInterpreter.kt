package com.hashslingingflasher

data class OpenPortInterpretedResponse(
    val responseTypeSummary: String,
    val statusSummary: String,
    val errorSummary: String
)

class OpenPortResponseInterpreter {

    fun interpret(result: TactrixTestResult): OpenPortInterpretedResponse {
        val adapterRejected = looksLikeAdapterRejection(result.responseAscii)

        val responseTypeSummary = when {
            result.responseAscii.isNotBlank() -> classifyAsciiResponse(result.responseAscii)
            result.responseHex.isNotBlank() -> "Response Type: Raw hex response"
            else -> "Response Type: None"
        }

        val statusSummary = when {
            adapterRejected -> "OpenPort adapter rejected command"
            result.success && result.responseAscii.isNotBlank() ->
                "OpenPort adapter response received"
            result.success && result.responseHex.isNotBlank() ->
                "OpenPort raw response received"
            result.statusMessage.isNotBlank() ->
                result.statusMessage
            else ->
                "No response from OpenPort"
        }

        val errorSummary = when {
            adapterRejected ->
                "Last Error: Adapter rejected command"
            result.success ->
                "Last Error: None"
            result.statusMessage.isNotBlank() ->
                "Last Error: ${result.statusMessage}"
            else ->
                "Last Error: Unknown"
        }

        return OpenPortInterpretedResponse(
            responseTypeSummary = responseTypeSummary,
            statusSummary = statusSummary,
            errorSummary = errorSummary
        )
    }

    private fun classifyAsciiResponse(responseAscii: String): String {
        val normalized = responseAscii.trim().lowercase()

        return when {
            looksLikeAdapterRejection(normalized) ->
                "Response Type: Adapter rejection"
            normalized.contains("ati") || normalized.contains("firmware") ->
                "Response Type: Adapter info response"
            normalized.contains("ok") || normalized.contains("ack") ->
                "Response Type: Adapter acknowledgment"
            else ->
                "Response Type: OpenPort ASCII response"
        }
    }

    private fun looksLikeAdapterRejection(responseAscii: String): Boolean {
        val normalized = responseAscii.trim().lowercase()

        return normalized.contains("aro") ||
            normalized.contains("error") ||
            normalized.contains("?")
    }
}
