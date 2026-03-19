package com.hashslingingflasher

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner

data class CommandPreset(
    val label: String,
    val command: String
)

class CommandPresetHelper(
    private val spinner: Spinner,
    private val manualCommandInput: EditText
) {
    private var activePresets: List<CommandPreset> = emptyList()

    fun bind(initialMode: String) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val preset = activePresets.getOrNull(position) ?: return
                manualCommandInput.setText(preset.command)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        updateForMode(initialMode)
    }

    fun updateForMode(selectedMode: String) {
        activePresets = presetsForMode(selectedMode)

        val labels = activePresets.map { it.label }
        val adapter = ArrayAdapter(
            spinner.context,
            android.R.layout.simple_spinner_item,
            labels
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        if (activePresets.isNotEmpty()) {
            spinner.setSelection(0)
            manualCommandInput.setText(activePresets[0].command)
        } else {
            manualCommandInput.setText("")
        }
    }

    private fun presetsForMode(selectedMode: String): List<CommandPreset> {
        return when (selectedMode) {
            CommandModeHelper.MODE_ADAPTER_ASCII -> listOf(
                CommandPreset("ata - wake OpenPort", "ata"),
                CommandPreset("ati - firmware version", "ati"),
                CommandPreset("atsp0 - auto protocol detect", "atsp0"),
                CommandPreset("at06 0 500000 0 - force CAN 500k", "at06 0 500000 0")
            )

            CommandModeHelper.MODE_RAW_PACKET -> listOf(
                CommandPreset("raw stub - 00 00", "00 00"),
                CommandPreset("raw stub - 7E0 01 00", "7E0 01 00"),
                CommandPreset("raw stub - custom bytes", "AA BB CC")
            )

            CommandModeHelper.MODE_SUBARU_SSM_KLINE -> listOf(
                CommandPreset("ssm stub - init marker", "SSM_INIT"),
                CommandPreset("ssm stub - read marker", "SSM_READ"),
                CommandPreset("ssm stub - custom payload", "80 10 F0 01 BF")
            )

            else -> listOf(
                CommandPreset("manual command", "")
            )
        }
    }
}
