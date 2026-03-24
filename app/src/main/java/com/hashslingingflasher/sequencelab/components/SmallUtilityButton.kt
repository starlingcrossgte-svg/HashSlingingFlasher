package com.hashslingingflasher.sequencelab.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val InactiveGray = Color(0xFFD3D7DE)
private val TextDark = Color(0xFF171A20)

@Composable
fun SmallUtilityButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = InactiveGray,
            contentColor = TextDark
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}
