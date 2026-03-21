package com.hashslingingflasher.sequencelab.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
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
        LightningProgressState.IDLE -> Color(0x00000000)
        LightningProgressState.READY -> Color(0xFFF5D34F)
        LightningProgressState.RUNNING -> Color(0xFFFFE066)
        LightningProgressState.COMPLETE -> Color(0xFFFFF176)
        LightningProgressState.FAILED -> Color(0xFFD93025)
    }

    val appliedProgress = when (state) {
        LightningProgressState.IDLE -> 0f
        LightningProgressState.READY -> 0.28f
        LightningProgressState.RUNNING -> normalizedProgress
        LightningProgressState.COMPLETE -> 1f
        LightningProgressState.FAILED -> normalizedProgress
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
    ) {
        val w = size.width
        val h = size.height

        val bolt = Path().apply {
            moveTo(w * 0.08f, h * 0.62f)
            lineTo(w * 0.34f, h * 0.62f)
            lineTo(w * 0.28f, h * 0.25f)
            lineTo(w * 0.70f, h * 0.25f)
            lineTo(w * 0.48f, h * 0.02f)
            lineTo(w * 0.92f, h * 0.02f)
            lineTo(w * 0.56f, h * 0.98f)
            lineTo(w * 0.62f, h * 0.68f)
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
                width = size.minDimension * 0.04f,
                cap = StrokeCap.Round
            )
        )
    }
}
