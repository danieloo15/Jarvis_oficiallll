package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.device.VoiceState
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisOrbCore(
    voiceState: VoiceState,
    sizeDp: Dp = 220.dp,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_orb")

    // Rotation angle for outer holographic rings
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (voiceState) {
                    VoiceState.THINKING -> 2000
                    VoiceState.LISTENING -> 4000
                    VoiceState.SPEAKING -> 3000
                    VoiceState.ERROR -> 8000
                    VoiceState.IDLE -> 6000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Reverse rotation for counter-rotating inner ring
    val reverseRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reverse_rotation"
    )

    // Pulse scale for core breathing
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = when (voiceState) {
            VoiceState.LISTENING -> 1.15f
            VoiceState.SPEAKING -> 1.12f
            VoiceState.THINKING -> 1.08f
            VoiceState.ERROR -> 1.05f
            VoiceState.IDLE -> 0.96f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (voiceState) {
                    VoiceState.LISTENING -> 600
                    VoiceState.SPEAKING -> 800
                    VoiceState.THINKING -> 400
                    VoiceState.ERROR -> 1000
                    VoiceState.IDLE -> 1800
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Color tones based on state
    val (primaryGlow, secondaryGlow, coreColor) = when (voiceState) {
        VoiceState.IDLE -> Triple(JarvisCyan, JarvisBlue, Color(0xFF00E5FF))
        VoiceState.LISTENING -> Triple(JarvisEmerald, JarvisCyan, Color(0xFF00FFC8))
        VoiceState.THINKING -> Triple(JarvisGold, JarvisOrange, Color(0xFFFFCC00))
        VoiceState.SPEAKING -> Triple(JarvisCyan, JarvisBlue, Color(0xFF80E5FF))
        VoiceState.ERROR -> Triple(JarvisRed, JarvisOrange, Color(0xFFFF5252))
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            // 1. Ambient Background Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlow.copy(alpha = 0.35f * pulseScale),
                        secondaryGlow.copy(alpha = 0.12f * pulseScale),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 0.95f
                ),
                radius = radius * 0.95f,
                center = center
            )

            // 2. Outer Segmented Tech Ring
            rotate(rotationAngle, pivot = center) {
                drawCircle(
                    color = primaryGlow.copy(alpha = 0.5f),
                    radius = radius * 0.88f,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f, 10f, 15f), 0f)
                    )
                )

                // Outer tech nodes
                for (i in 0 until 6) {
                    val angle = (i * 60) * (Math.PI / 180f).toFloat()
                    val nodePos = Offset(
                        center.x + (radius * 0.88f) * cos(angle),
                        center.y + (radius * 0.88f) * sin(angle)
                    )
                    drawCircle(
                        color = primaryGlow,
                        radius = 3.dp.toPx(),
                        center = nodePos
                    )
                }
            }

            // 3. Middle Counter-Rotating Arc Ring
            rotate(reverseRotationAngle, pivot = center) {
                drawCircle(
                    color = secondaryGlow.copy(alpha = 0.6f),
                    radius = radius * 0.72f,
                    center = center,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(60f, 40f), 0f),
                        cap = StrokeCap.Round
                    )
                )
            }

            // 4. Inner Concentric Power Ring
            rotate(rotationAngle * 1.5f, pivot = center) {
                drawCircle(
                    color = primaryGlow.copy(alpha = 0.8f),
                    radius = radius * 0.52f,
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )
                )
            }

            // 5. Central Glowing Arc Reactor Core
            val coreRadius = radius * 0.36f * pulseScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        coreColor,
                        primaryGlow.copy(alpha = 0.85f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // 6. Central Tech Diamond / Geometric Mark
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = center
            )
        }
    }
}
