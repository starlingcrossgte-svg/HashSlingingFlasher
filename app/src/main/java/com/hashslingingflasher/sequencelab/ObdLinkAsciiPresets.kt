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
