package com.hashslingingflasher

data class OpenPortInterpretedResponse(
    val responseTypeSummary: String,
    val statusSummary: String,
    val errorSummary: String
)

class OpenPortResponseInterpreter {

    fun interpret(result: TactrixTestResult): OpenPortInterpretedResponse {
        val responseTypeSummary = when {
            result.responseAscii.isNotBlank() -> classifyAsciiResponse(result.responseAscii)
            result.responseHex.isNotBlank() -> "Response Type: Raw hex response"
            else -> "Response Type: None"
        }

        val statusSummary = when {
            result.success && result.responseAscii.isNotBlank() ->
                "OpenPort adapter response received"
            result.success && result.responseHex.isNotBlank() ->
                "OpenPort raw response received"
            else -> result.statusMessage
        }

        val errorSummary = when {
            result.success -> "Last Error: None"
            looksLikeAdapterRejection(result.responseAscii) ->
                "Last Error: Adapter rejected command"
            result.statusMessage.isNotBlank() ->
                "Last Error: ${result.statusMessage}"
            else -> "Last Error: Unknown"
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
            normalized.contains("ati") || normalized.contains("firmware") ->
                "Response Type: Adapter info response"
            normalized.contains("ok") || normalized.contains("ack") ->
                "Response Type: Adapter acknowledgment"
            looksLikeAdapterRejection(normalized) ->
                "Response Type: Adapter rejection"
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
