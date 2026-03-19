package com.hashslingingflasher

import android.widget.ArrayAdapter
import android.widget.Spinner

class CommandModeHelper(
    private val spinner: Spinner
) {
    private val modeLabels = listOf(
        "Adapter ASCII",
        "Raw Packet",
        "Subaru SSM / K-line"
    )

    fun bind() {
        val adapter = ArrayAdapter(
            spinner.context,
            android.R.layout.simple_spinner_item,
            modeLabels
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)
    }

    fun selectedModeLabel(): String {
        return modeLabels[spinner.selectedItemPosition]
    }
}
