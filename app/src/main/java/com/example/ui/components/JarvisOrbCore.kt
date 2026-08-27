package com.example.ui.components

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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
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

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "jarvis_orb"
        )

    // =========================================================
    // ROTACIÓN EXTERIOR
    // =========================================================

    val rotationAngle by
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                when (voiceState) {
                                    VoiceState.THINKING -> 2000
                                    VoiceState.LISTENING -> 2500
                                    VoiceState.SPEAKING -> 3000
                                    VoiceState.ERROR -> 8000
                                    VoiceState.IDLE -> 6000
                                }
                        )
                ),
            label = "rotation"
        )

    // =========================================================
    // ROTACIÓN INTERIOR
    // =========================================================

    val reverseRotationAngle by
        infiniteTransition.animateFloat(
            initialValue = 360f,
            targetValue = 0f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis = 5000
                        )
                ),
            label = "reverse_rotation"
        )

    // =========================================================
    // PULSO DEL NÚCLEO
    // =========================================================

    val pulseScale by
        infiniteTransition.animateFloat(
            initialValue = 0.88f,
            targetValue =
                when (voiceState) {
                    VoiceState.LISTENING -> 1.15f
                    VoiceState.SPEAKING -> 1.12f
                    VoiceState.THINKING -> 1.08f
                    VoiceState.ERROR -> 1.05f
                    VoiceState.IDLE -> 0.96f
                },
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                when (voiceState) {
                                    VoiceState.LISTENING -> 600
                                    VoiceState.SPEAKING -> 800
                                    VoiceState.THINKING -> 400
                                    VoiceState.ERROR -> 1000
                                    VoiceState.IDLE -> 1800
                                }
                        ),
                    repeatMode = RepeatMode.Reverse
                ),
            label = "pulse"
        )

    // =========================================================
    // COLORES SEGÚN ESTADO
    // =========================================================

    val (
        primaryGlow,
        secondaryGlow,
        coreColor
    ) =
        when (voiceState) {

            VoiceState.IDLE ->
                Triple(
                    JarvisCyan,
                    JarvisBlue,
                    Color(0xFF00E5FF)
                )

            VoiceState.LISTENING ->
                Triple(
                    JarvisEmerald,
                    JarvisCyan,
                    Color(0xFF00FFC8)
                )

            VoiceState.THINKING ->
                Triple(
                    JarvisGold,
                    JarvisOrange,
                    Color(0xFFFFCC00)
                )

            VoiceState.SPEAKING ->
                Triple(
                    JarvisCyan,
                    JarvisBlue,
                    Color(0xFF80E5FF)
                )

            VoiceState.ERROR ->
                Triple(
                    JarvisRed,
                    JarvisOrange,
                    Color(0xFFFF5252)
                )
        }

    // =========================================================
    // ORBE INTERACTIVO
    // =========================================================

    Box(
        modifier =
            modifier
                .size(sizeDp)
                .clickable(
                    interactionSource =
                        remember {
                            MutableInteractionSource()
                        },
                    indication = null,
                    enabled = true,
                    onClick = {
                        // Un toque completo activa
                        // la acción del controlador.
                        onClick()
                    }
                ),
        contentAlignment =
            Alignment.Center
    ) {

        Canvas(
            modifier =
                Modifier.size(sizeDp)
        ) {

            val center =
                Offset(
                    size.width / 2f,
                    size.height / 2f
                )

            val radius =
                size.minDimension / 2f

            // =================================================
            // 1. BRILLO AMBIENTAL
            // =================================================

            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                primaryGlow.copy(
                                    alpha =
                                        0.35f *
                                            pulseScale
                                ),
                                secondaryGlow.copy(
                                    alpha =
                                        0.12f *
                                            pulseScale
                                ),
                                Color.Transparent
                            ),
                        center = center,
                        radius =
                            radius * 0.95f
                    ),
                radius =
                    radius * 0.95f,
                center = center
            )

            // =================================================
            // 2. ANILLO EXTERIOR
            // =================================================

            rotate(
                rotationAngle,
                pivot = center
            ) {

                drawCircle(
                    color =
                        primaryGlow.copy(
                            alpha = 0.5f
                        ),
                    radius =
                        radius * 0.88f,
                    center = center,
                    style =
                        Stroke(
                            width =
                                2.dp.toPx(),
                            pathEffect =
                                PathEffect
                                    .dashPathEffect(
                                        floatArrayOf(
                                            30f,
                                            15f,
                                            10f,
                                            15f
                                        ),
                                       