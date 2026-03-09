package com.ecuflasher

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this)
        text.text = "ECUFlasher Pro"
        text.textSize = 26f

        setContentView(text)
    }
}
