package com.hashslingingflasher.sequencelab.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp

enum class LightningProgressState {
    IDLE,
    READY,
    RUNNING,
    COMPLETE,
    FAILED
}

@Composable
fun LightningProgressBar(
    state: LightningProgressState,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val normalizedProgress = progress.coerceIn(0f, 1f)

    val outlineColor = when (state) {
        LightningProgressState.FAILED -> Color(0xFFD93025)
        else -> Color(0xFFF5D34F)
    }

    val fillColor = when (state) {
        LightningProgressState.IDLE -> Color.Transparent
        LightningProgressState.READY -> Color(0xFFF5D34F)
        LightningProgressState.RUNNING -> Color(0xFFFFE066)
        LightningProgressState.COMPLETE -> Color(0xFFFFF176)
        LightningProgressState.FAILED -> Color(0xFFD93025)
    }

    val appliedProgress = when (state) {
        LightningProgressState.IDLE -> 0f
        LightningProgressState.READY -> 0.22f
        LightningProgressState.RUNNING -> normalizedProgress
        LightningProgressState.COMPLETE -> 1f
        LightningProgressState.FAILED -> normalizedProgress
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
    ) {
        val w = size.width
        val h = size.height

        val bolt = Path().apply {
            moveTo(w * 0.05f, h * 0.58f)
            lineTo(w * 0.36f, h * 0.58f)
            lineTo(w * 0.27f, h * 0.94f)
            lineTo(w * 0.71f, h * 0.42f)
            lineTo(w * 0.44f, h * 0.42f)
            lineTo(w * 0.53f, h * 0.08f)
            close()
        }

        clipRect(right = size.width * appliedProgress) {
            drawPath(
                path = bolt,
                color = fillColor,
                style = Fill
            )
        }

        drawPath(
            path = bolt,
            color = outlineColor,
            style = Stroke(
                width = size.minDimension * 0.08f,
                cap = StrokeCap.Round
            )
        )
    }
}
