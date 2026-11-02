package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.data.model.TitleEntity
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GraphiteBlack
import com.example.ui.theme.GraphiteDark
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val SliceColors = listOf(
    Color(0xFF8B5CF6),
    Color(0xFF06B6D4),
    Color(0xFFEC4899),
    Color(0xFF3B82F6),
    Color(0xFF10B981),
    Color(0xFFA855F7),
    Color(0xFFF59E0B),
    Color(0xFF6366F1)
)

@Composable
fun RouletteWheel(
    items: List<TitleEntity>,
    isSpinning: Boolean,
    targetWinnerIndex: Int,
    onSpinComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isSpinning, targetWinnerIndex) {
        if (isSpinning && items.isNotEmpty()) {
            val count = items.size
            val sliceAngle = 360f / count
            // Pointer is at the top (270 degrees in standard coordinate or -90)
            // Slices start at 0 (right). Top is at 270 degrees.
            // Target angle to land winner at top:
            val winnerCenterAngle = (targetWinnerIndex * sliceAngle) + (sliceAngle / 2f)
            val fullSpins = 6 * 360f
            // We want (finalRotation + winnerCenterAngle) % 360 == 270
            val targetAngle = fullSpins + (270f - winnerCenterAngle) + 3600f

            rotation.animateTo(
                targetValue = targetAngle,
                animationSpec = tween(
                    durationMillis = 3800,
                    easing = FastOutSlowInEasing
                )
            )
            onSpinComplete()
        }
    }

    Box(
        modifier = modifier.size(310.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Neon Outer Backdrop
        Box(
            modifier = Modifier
                .size(310.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonPurple.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
        )

        // Rotating Wheel Canvas
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .rotate(rotation.value)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f

            if (items.isEmpty()) {
                // Empty state wheel
                drawCircle(
                    color = GraphiteSurface,
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = NeonPurple,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )
                return@Canvas
            }

            val count = items.size
            val sweepAngle = 360f / count

            for (i in 0 until count) {
                val startAngle = i * sweepAngle
                val color = SliceColors[i % SliceColors.size]

                // Draw Slice Arc
                drawArc(
                    brush = Brush.radialGradient(
                        colors = listOf(color, color.copy(alpha = 0.75f)),
                        center = center,
                        radius = radius
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(radius * 2f, radius * 2f),
                    topLeft = Offset(center.x - radius, center.y - radius)
                )

                // Draw Slice Divider line
                val lineAngleRad = (startAngle * PI / 180f).toFloat()
                val lineEnd = Offset(
                    center.x + radius * cos(lineAngleRad),
                    center.y + radius * sin(lineAngleRad)
                )
                drawLine(
                    color = GraphiteBlack,
                    start = center,
                    end = lineEnd,
                    strokeWidth = 2.dp.toPx()
                )

                // Draw Text on slice
                val midAngleDeg = startAngle + sweepAngle / 2f
                val midAngleRad = (midAngleDeg * PI / 180f).toFloat()
                val textRadius = radius * 0.62f
                val textPos = Offset(
                    center.x + textRadius * cos(midAngleRad),
                    center.y + textRadius * sin(midAngleRad)
                )

                val title = items[i].name
                val displayTitle = if (title.length > 12) title.take(10) + "…" else title

                drawContext.canvas.nativeCanvas.apply {
                    save()
                    rotate(midAngleDeg, textPos.x, textPos.y)
                    val paint = Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        this.textSize = 28f
                        this.typeface = Typeface.DEFAULT_BOLD
                        this.textAlign = Paint.Align.CENTER
                        this.isAntiAlias = true
                        setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
                    }
                    drawText(displayTitle, textPos.x, textPos.y + 10f, paint)
                    restore()
                }
            }

            // Outer Neon Rim
            drawCircle(
                color = NeonCyan,
                radius = radius,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )

            // Outer LED Dots
            val dotCount = maxOf(count * 2, 16)
            for (d in 0 until dotCount) {
                val dotAngle = (d * (360f / dotCount)) * PI / 180f
                val dotPos = Offset(
                    center.x + (radius - 8.dp.toPx()) * cos(dotAngle).toFloat(),
                    center.y + (radius - 8.dp.toPx()) * sin(dotAngle).toFloat()
                )
                drawCircle(
                    color = if (d % 2 == 0) NeonCyan else Color.White,
                    radius = 3.dp.toPx(),
                    center = dotPos
                )
            }

            // Center Core
            drawCircle(
                color = GraphiteBlack,
                radius = 34.dp.toPx(),
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonPurple, NeonPurple.copy(alpha = 0.4f)),
                    center = center,
                    radius = 30.dp.toPx()
                ),
                radius = 28.dp.toPx(),
                center = center
            )
            drawCircle(
                color = NeonCyan,
                radius = 12.dp.toPx(),
                center = center
            )
        }

        // Top Static Neon Pointer (arrow pointing down to top of wheel)
        Canvas(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.TopCenter)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height) // tip pointing down
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(
                path = path,
                color = NeonCyan,
                style = Fill
            )
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
