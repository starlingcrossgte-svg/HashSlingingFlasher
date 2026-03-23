package com.hashslingingflasher.sequencelab

data class ObdLinkAsciiPreset(
    val displayName: String,
    val rawCommand: String,
    val description: String,
    val expectedResponse: String
)

val obdLinkAsciiPresets = listOf(
    ObdLinkAsciiPreset(
        displayName = "ATZ",
        rawCommand = "ATZ",
        description = "Full reset. Reboots the adapter and restores startup state.",
        expectedResponse = "Adapter banner / prompt"
    ),
    ObdLinkAsciiPreset(
        displayName = "ATI",
        rawCommand = "ATI",
        description = "Identify adapter ELM compatibility version.",
        expectedResponse = "ELM327 v1.4b"
    ),
    ObdLinkAsciiPreset(
        displayName = "STI",
        rawCommand = "STI",
        description = "Identify the actual STN firmware version.",
        expectedResponse = "STN2230 v..."
    ),
    ObdLinkAsciiPreset(
        displayName = "ATRV",
        rawCommand = "ATRV",
        description = "Read vehicle battery voltage from pin 16.",
        expectedResponse = "12.4V"
    ),
    ObdLinkAsciiPreset(
        displayName = "ATE0",
        rawCommand = "ATE0",
        description = "Echo off. Stops the adapter from repeating commands back.",
        expectedResponse = "OK"
    ),
    ObdLinkAsciiPreset(
        displayName = "ATS0",
        rawCommand = "ATS0",
        description = "Spaces off. Removes spaces between hex bytes.",
        expectedResponse = "OK"
    ),
    ObdLinkAsciiPreset(
        displayName = "ATL0",
        rawCommand = "ATL0",
        description = "Linefeeds off. Cleans up serial reads and logs.",
        expectedResponse = "OK"
    ),
    ObdLinkAsciiPreset(
        displayName = "ATH1",
        rawCommand = "ATH1",
        description = "Headers on. Shows header bytes in responses.",
        expectedResponse = "OK"
    ),
    ObdLinkAsciiPreset(
        displayName = "ATFI",
        rawCommand = "ATFI",
        description = "Force ISO 14230 fast init on the K-Line.",
        expectedResponse = "OK / prompt"
    ),
    ObdLinkAsciiPreset(
        displayName = "ATSP4",
        rawCommand = "ATSP4",
        description = "Set protocol 4. Lock adapter to ISO 14230 K-Line.",
        expectedResponse = "OK"
    ),
    ObdLinkAsciiPreset(
        displayName = "ATDP",
        rawCommand = "ATDP",
        description = "Describe the currently selected protocol for debugging.",
        expectedResponse = "Protocol description"
    ),
    ObdLinkAsciiPreset(
        displayName = "ATSH8010F0",
        rawCommand = "ATSH8010F0",
        description = "Set header for Subaru ECU / engine target.",
        expectedResponse = "OK"
    ),
    ObdLinkAsciiPreset(
        displayName = "ATSH8018F0",
        rawCommand = "ATSH8018F0",
        description = "Set header for Subaru TCU / transmission target.",
        expectedResponse = "OK"
    ),
    ObdLinkAsciiPreset(
        displayName = "1081 Wake Up Payload",
        rawCommand = "1081",
        description = "Subaru wake-up payload used after K-Line init to open diagnostics.",
        expectedResponse = "Module wake-up / response bytes"
    ),
    ObdLinkAsciiPreset(
        displayName = "STDI",
        rawCommand = "STDI",
        description = "Read OBDLink device identifier information.",
        expectedResponse = "OBDLink EX ..."
    ),
    ObdLinkAsciiPreset(
        displayName = "STDIX",
        rawCommand = "STDIX",
        description = "Extended device information dump.",
        expectedResponse = "Multi-line device info"
    )
)
