package com.hashslingingflasher.sequencelab.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BannerLight = Color(0xFFB8B8BB)
private val BannerDark = Color(0xFF9F9FA2)
private val BannerText = Color(0xFFF1F1F1)

@Composable
fun SequenceLabHeader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .drawBehind {
                val w = size.width
                val h = size.height

                val mainBanner = Path().apply {
                    moveTo(w * 0.08f, h * 0.06f)
                    lineTo(w * 0.82f, h * 0.06f)
                    lineTo(w * 0.79f, h * 0.82f)
                    lineTo(w * 0.06f, h * 0.82f)
                    lineTo(w * 0.08f, h * 0.06f)
                    close()
                }
                drawPath(
                    path = mainBanner,
                    color = BannerLight
                )

                val leftCap = Path().apply {
                    moveTo(0f, h * 0.06f)
                    lineTo(w * 0.10f, h * 0.06f)
                    lineTo(w * 0.09f, h * 0.82f)
                    lineTo(0f, h * 0.82f)
                    close()
                }
                drawPath(
                    path = leftCap,
                    color = BannerDark
                )
            }
    ) {
        Text(
            text = "sequence lab",
            color = BannerText,
            fontSize = 44.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Light,
            letterSpacing = 1.5.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}
