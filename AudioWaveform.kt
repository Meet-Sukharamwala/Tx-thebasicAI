package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.agent.TxState
import com.example.ui.theme.TxCyanPrimary
import com.example.ui.theme.TxNeonPurple
import com.example.ui.theme.TxStateSpeaking

@Composable
fun AudioWaveform(
    state: TxState,
    audioRms: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 18,
    maxHeight: Dp = 32.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    val baseAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveform_pulse"
    )

    val isActive = state == TxState.LISTENING || state == TxState.SPEAKING || state == TxState.THINKING
    val color = when (state) {
        TxState.LISTENING -> TxCyanPrimary
        TxState.SPEAKING -> TxStateSpeaking
        TxState.THINKING -> TxNeonPurple
        else -> Color(0xFF1E355B)
    }

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val offsetFactor = (i.toFloat() / barCount)
            val barHeightFraction = if (isActive) {
                if (state == TxState.LISTENING) {
                    (0.2f + audioRms * 0.8f * kotlin.math.sin((offsetFactor * Math.PI).toFloat())).coerceIn(0.15f, 1f)
                } else {
                    (0.2f + baseAnim * kotlin.math.sin(((offsetFactor + baseAnim) * Math.PI).toFloat())).coerceIn(0.15f, 0.95f)
                }
            } else {
                0.15f
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(barHeightFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}
