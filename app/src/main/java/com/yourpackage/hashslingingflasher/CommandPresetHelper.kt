package com.hashslingingflasher

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner

class CommandPresetHelper(
    private val spinner: Spinner,
    private val manualCommandInput: EditText
) {
    private val presetLabels = listOf(
        "ata - wake OpenPort",
        "ati - firmware version",
        "atsp0 - auto protocol detect",
        "at06 0 500000 0 - force CAN 500k"
    )

    private val presetCommands = listOf(
        "ata",
        "ati",
        "atsp0",
        "at06 0 500000 0"
    )

    fun bind() {
        val adapter = ArrayAdapter(
            spinner.context,
            android.R.layout.simple_spinner_item,
            presetLabels
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                manualCommandInput.setText(presetCommands[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        spinner.setSelection(0)
        manualCommandInput.setText("ata")
    }
}
