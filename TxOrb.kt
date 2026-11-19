package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.agent.TxState
import com.example.ui.theme.TxCyanPrimary
import com.example.ui.theme.TxNeonPurple
import com.example.ui.theme.TxStateError
import com.example.ui.theme.TxStateExecuting
import com.example.ui.theme.TxStateListening
import com.example.ui.theme.TxStateSpeaking
import com.example.ui.theme.TxStateThinking
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TxOrb(
    state: TxState,
    audioRms: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tx_orb_transition")

    // Rotation angles
    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_outer"
    )

    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_inner"
    )

    // Pulse scale based on state & audio level
    val basePulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    TxState.THINKING -> 800
                    TxState.LISTENING -> 600
                    TxState.SPEAKING -> 700
                    TxState.EXECUTING -> 900
                    TxState.ERROR -> 500
                    TxState.IDLE -> 2400
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "base_pulse"
    )

    // Dynamic state accent colors
    val primaryColor = when (state) {
        TxState.IDLE -> TxCyanPrimary
        TxState.LISTENING -> TxStateListening
        TxState.THINKING -> TxStateThinking
        TxState.SPEAKING -> TxStateSpeaking
        TxState.EXECUTING -> TxStateExecuting
        TxState.ERROR -> TxStateError
    }

    val secondaryColor = when (state) {
        TxState.IDLE -> Color(0xFF0077B6)
        TxState.LISTENING -> Color(0xFF00B0FF)
        TxState.THINKING -> TxNeonPurple
        TxState.SPEAKING -> Color(0xFF059669)
        TxState.EXECUTING -> Color(0xFFD97706)
        TxState.ERROR -> Color(0xFFDC2626)
    }

    // Audio reactivity multiplier
    val dynamicScale = basePulse + (audioRms * 0.22f)

    Box(
        modifier = modifier
            .size(size)
            .testTag("tx_animated_orb")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = this.size.minDimension / 2f

            // 1. Outermost glow corona
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.28f * dynamicScale),
                        secondaryColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * dynamicScale
                ),
                radius = maxRadius * dynamicScale,
                center = center
            )

            // 2. Rotating segmented outer radar ring
            rotate(rotationOuter, pivot = center) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.45f),
                    radius = maxRadius * 0.90f,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 26f, 36f, 26f), 0f)
                    )
                )

                // 4 radar satellite blips
                for (i in 0 until 4) {
                    val angle = (i * 90.0) * (Math.PI / 180.0)
                    val blipR = maxRadius * 0.90f
                    val blipCenter = Offset(
                        center.x + (blipR * cos(angle)).toFloat(),
                        center.y + (blipR * sin(angle)).toFloat()
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 3.dp.toPx(),
                        center = blipCenter
                    )
                }
            }

            // 3. Counter-rotating inner tech ring
            rotate(rotationInner, pivot = center) {
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.6f),
                    radius = maxRadius * 0.76f,
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 14f), 0f)
                    )
                )
            }

            // 4. Core energy orb (gradient)
            val coreRadius = maxRadius * 0.58f * dynamicScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.92f),
                        primaryColor,
                        secondaryColor.copy(alpha = 0.85f),
                        Color(0xFF070B14)
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // 5. High-tech "Tx" Monogram rendered on Canvas
            val monoColor = Color.White
            val strokeW = 4.dp.toPx()

            // Letter 'T'
            val tLeft = center.x - 22.dp.toPx()
            val tRight = center.x - 2.dp.toPx()
            val tTop = center.y - 14.dp.toPx()
            val tBottom = center.y + 14.dp.toPx()
            val tMidX = (tLeft + tRight) / 2f

            // T bar
            drawLine(
                color = monoColor,
                start = Offset(tLeft, tTop),
                end = Offset(tRight, tTop),
                strokeWidth = strokeW
            )
            // T stem
            drawLine(
                color = monoColor,
                start = Offset(tMidX, tTop),
                end = Offset(tMidX, tBottom),
                strokeWidth = strokeW
            )

            // Letter 'x' (italic cyber x)
            val xLeft = center.x + 4.dp.toPx()
            val xRight = center.x + 22.dp.toPx()
            val xTop = center.y - 8.dp.toPx()
            val xBottom = center.y + 14.dp.toPx()

            drawLine(
                color = primaryColor,
                start = Offset(xLeft, xTop),
                end = Offset(xRight, xBottom),
                strokeWidth = strokeW
            )
            drawLine(
                color = primaryColor,
                start = Offset(xLeft, xBottom),
                end = Offset(xRight, xTop),
                strokeWidth = strokeW
            )
        }
    }
}
