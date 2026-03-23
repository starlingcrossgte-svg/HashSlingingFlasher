package com.hashslingingflasher.sequencelab.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val ActiveBlue = Color(0xFF2D6CDF)
private val InactiveGray = Color(0xFFD3D7DE)
private val TextMuted = Color(0xFF6B7280)

@Composable
fun TransportButton(
    label: String,
    selected: Boolean
) {
    Button(
        onClick = { },
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) ActiveBlue else InactiveGray,
            contentColor = if (selected) Color.White else TextMuted
        )
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold
        )
    }
}
