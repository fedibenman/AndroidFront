package com.example.myapplication.ui.teammate

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.PrimaryGold
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Enhanced radar animation with sweep line, distance labels, and tap handling
 */
@Composable
fun RadarAnimation(
    modifier: Modifier = Modifier,
    isSearching: Boolean = true,
    userDots: List<RadarDot> = emptyList(),
    range: Int = 50,
    onUserTapped: ((RadarDot) -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val textMeasurer = rememberTextMeasurer()
    
    // Pulse animations
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )
    
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, delayMillis = 833, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )
    
    val pulse3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, delayMillis = 1666, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse3"
    )

    // Sweep rotation animation
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    // Dot pulse animation
    val dotPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotPulse"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(userDots) {
                    detectTapGestures { offset ->
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val maxRadius = minOf(size.width, size.height) / 2
                        
                        // Find tapped dot
                        userDots.forEach { dot ->
                            val dotX = centerX + (dot.normalizedX * maxRadius * 0.9f)
                            val dotY = centerY + (dot.normalizedY * maxRadius * 0.9f)
                            val distance = sqrt(
                                (offset.x - dotX).pow(2) + (offset.y - dotY).pow(2)
                            )
                            if (distance < 40f) {
                                onUserTapped?.invoke(dot)
                            }
                        }
                    }
                }
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val maxRadius = minOf(size.width, size.height) / 2

            if (isSearching) {
                // Draw sweep with gradient
                rotate(sweepAngle, pivot = Offset(centerX, centerY)) {
                    val sweepBrush = Brush.sweepGradient(
                        0f to Color.Transparent,
                        0.8f to Color.Transparent,
                        0.9f to Color.Green.copy(alpha = 0.3f),
                        1f to Color.Green.copy(alpha = 0.6f),
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        brush = sweepBrush,
                        radius = maxRadius,
                        center = Offset(centerX, centerY)
                    )
                }
                
                // Draw expanding pulse circles
                listOf(pulse1, pulse2, pulse3).forEach { pulse ->
                    val radius = maxRadius * pulse
                    val alpha = (1f - pulse) * 0.5f
                    
                    drawCircle(
                        color = PrimaryGold.copy(alpha = alpha),
                        radius = radius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Draw static range circles with labels
                val rangeLabels = listOf(
                    0.25f to "${range / 4}km",
                    0.5f to "${range / 2}km",
                    0.75f to "${range * 3 / 4}km",
                    1f to "${range}km"
                )
                
                rangeLabels.forEach { (scale, label) ->
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = maxRadius * scale,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    
                    // Draw distance label
                    val textResult = textMeasurer.measure(
                        text = label,
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 8.sp
                        )
                    )
                    drawText(
                        textResult,
                        topLeft = Offset(
                            centerX + (maxRadius * scale) - textResult.size.width - 4.dp.toPx(),
                            centerY - textResult.size.height / 2
                        )
                    )
                }
                
                // Draw center dot (you)
                drawCircle(
                    color = PrimaryGold.copy(alpha = 0.3f),
                    radius = 18.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = PrimaryGold,
                    radius = 12.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
            }

            // Draw user dots
            userDots.forEach { dot ->
                val dotX = centerX + (dot.normalizedX * maxRadius * 0.9f)
                val dotY = centerY + (dot.normalizedY * maxRadius * 0.9f)
                
                // Glow effect
                drawCircle(
                    color = dot.color.copy(alpha = 0.3f * dotPulse),
                    radius = 20.dp.toPx() * dotPulse,
                    center = Offset(dotX, dotY)
                )
                
                // Solid dot
                drawCircle(
                    color = dot.color,
                    radius = 10.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
                
                // Draw user name
                val nameResult = textMeasurer.measure(
                    text = dot.userName.take(6),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 8.sp
                    )
                )
                val distResult = textMeasurer.measure(
                    text = "${dot.distance}km",
                    style = TextStyle(
                        color = dot.color,
                        fontSize = 7.sp
                    )
                )
                
                // Background for label
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.7f),
                    topLeft = Offset(
                        dotX - maxOf(nameResult.size.width, distResult.size.width) / 2 - 4.dp.toPx(),
                        dotY + 14.dp.toPx()
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        maxOf(nameResult.size.width, distResult.size.width).toFloat() + 8.dp.toPx(),
                        (nameResult.size.height + distResult.size.height).toFloat() + 4.dp.toPx()
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )
                
                drawText(
                    nameResult,
                    topLeft = Offset(
                        dotX - nameResult.size.width / 2,
                        dotY + 16.dp.toPx()
                    )
                )
                drawText(
                    distResult,
                    topLeft = Offset(
                        dotX - distResult.size.width / 2,
                        dotY + 16.dp.toPx() + nameResult.size.height
                    )
                )
            }
        }
    }
}

/**
 * Represents a user dot on the radar
 */
data class RadarDot(
    val normalizedX: Float,
    val normalizedY: Float,
    val color: Color = Color.Cyan,
    val userId: String = "",
    val userName: String = "",
    val distance: Int = 0
)

/**
 * Helper to convert distance and angle to normalized coordinates
 */
fun polarToNormalized(distanceRatio: Float, angleDegrees: Float): Pair<Float, Float> {
    val angleRadians = Math.toRadians(angleDegrees.toDouble())
    val x = (distanceRatio * cos(angleRadians)).toFloat()
    val y = (distanceRatio * sin(angleRadians)).toFloat()
    return Pair(x, y)
}
