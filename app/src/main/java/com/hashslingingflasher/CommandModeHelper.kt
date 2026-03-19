package com.hashslingingflasher

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class CommandModeHelper(
    private val spinner: Spinner,
    private val onModeChanged: (String) -> Unit
) {
    companion object {
        const val MODE_ADAPTER_ASCII = "Adapter ASCII"
        const val MODE_RAW_PACKET = "Raw Packet"
        const val MODE_SUBARU_SSM_KLINE = "Subaru SSM / K-line"
    }

    private val modeLabels = listOf(
        MODE_ADAPTER_ASCII,
        MODE_RAW_PACKET,
        MODE_SUBARU_SSM_KLINE
    )

    fun bind() {
        val adapter = ArrayAdapter(
            spinner.context,
            android.R.layout.simple_spinner_item,
            modeLabels
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
                onModeChanged(modeLabels[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        spinner.setSelection(0)
    }

    fun selectedModeLabel(): String {
        return modeLabels[spinner.selectedItemPosition]
    }
}
