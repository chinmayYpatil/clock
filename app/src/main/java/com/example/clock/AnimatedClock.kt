package com.example.clock

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

@Composable
fun FancyClock() {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    val spectrumSize = 100

    // Initialize with a sine wave pattern
    val spectrumBars = remember {
        List(spectrumSize) { index ->
            val initialValue = (sin(index * (2 * PI / spectrumSize)) * 40f + 50f).toFloat()
            Animatable(initialValue)
        }
    }

    // funky colors!
    val coolColors = listOf(Color(0xFF9400D3), Color(0xFFFF1493), Color(0xFF00BFFF), Color.Yellow)
    val gradientBrush = remember { Brush.sweepGradient(coolColors) }

    val skinToneColor = Color(0xFFE6BE8A)

    val dateFormatter = remember { SimpleDateFormat("EEEE dd", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            spectrumBars.forEachIndexed { i, bar ->
                launch {
                    bar.animateTo(
                        targetValue = (0.2f + 0.8f * kotlin.random.Random.nextFloat()) * 100f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    )
                }
            }
            delay(75)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val middle = Offset(size.width / 2, size.height / 2)
            val clockRadius = size.minDimension / 2 * 0.7f

            // Black background
            drawRect(Color.Black)

            // Show the date
            val today = dateFormatter.format(Date(now))
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    today,
                    middle.x,
                    middle.y - clockRadius - 60.dp.toPx(),
                    Paint().apply {
                        color = skinToneColor.toArgb()
                        textAlign = Paint.Align.CENTER
                        textSize = 40.dp.toPx()
                    }
                )
            }

            // Show the time
            val currentTime = timeFormatter.format(Date(now))
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    currentTime,
                    middle.x,
                    middle.y - clockRadius - 30.dp.toPx(),
                    Paint().apply {
                        color = skinToneColor.toArgb()
                        textAlign = Paint.Align.CENTER
                        textSize = 40.dp.toPx()
                    }
                )
            }

            // Draw our fancy spectrum
            val spectrumPath = Path()
            spectrumBars.forEachIndexed { index, bar ->
                val angle = index * (360f / spectrumSize)
                val outerRadius = clockRadius + bar.value.dp.toPx() - 50.dp.toPx()
                val innerRadius = clockRadius - 1.dp.toPx()
                val outerPoint = polarToCartesian(middle, outerRadius, angle)
                val innerPoint = polarToCartesian(middle, innerRadius, angle)

                if (index == 0) {
                    spectrumPath.moveTo(outerPoint.x, outerPoint.y)
                } else {
                    spectrumPath.lineTo(outerPoint.x, outerPoint.y)
                }

                if (index == spectrumSize - 1) {
                    spectrumPath.lineTo(innerPoint.x, innerPoint.y)
                    for (i in spectrumSize - 1 downTo 0) {
                        val reverseAngle = i * (360f / spectrumSize)
                        val reverseInnerPoint = polarToCartesian(middle, innerRadius, reverseAngle)
                        spectrumPath.lineTo(reverseInnerPoint.x, reverseInnerPoint.y)
                    }
                }
            }
            spectrumPath.close()

            drawPath(
                path = spectrumPath,
                brush = gradientBrush,
                style = Fill
            )

            // Draw clock hands
            val cal = Calendar.getInstance().apply { timeInMillis = now }
            val hours = cal.get(Calendar.HOUR)
            val minutes = cal.get(Calendar.MINUTE)
            val seconds = cal.get(Calendar.SECOND)

            drawClockHand(middle, clockRadius * 0.5f, hours * 30f, Color.White, 4.dp.toPx())
            drawClockHand(middle, clockRadius * 0.7f, minutes * 6f, Color.White, 3.dp.toPx())
            drawClockHand(middle, clockRadius * 0.9f, seconds * 6f, Color(0xFFFF69B4), 2.dp.toPx())
        }
    }
}

private fun polarToCartesian(center: Offset, radius: Float, angleDegrees: Float): Offset {
    val angleRadians = Math.toRadians(angleDegrees.toDouble())
    val x = center.x + (radius * cos(angleRadians)).toFloat()
    val y = center.y + (radius * sin(angleRadians)).toFloat()
    return Offset(x, y)
}

private fun DrawScope.drawClockHand(center: Offset, length: Float, angle: Float, color: Color, thickness: Float) {
    val end = polarToCartesian(center, length, angle - 90)
    drawLine(color, center, end, thickness, cap = StrokeCap.Round)
}