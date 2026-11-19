package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agent.TxState
import com.example.ui.theme.TxCyanPrimary
import com.example.ui.theme.TxStateError
import com.example.ui.theme.TxStateExecuting
import com.example.ui.theme.TxStateListening
import com.example.ui.theme.TxStateSpeaking
import com.example.ui.theme.TxStateThinking

@Composable
fun ActivityStatusBadge(
    state: TxState,
    detailText: String,
    modifier: Modifier = Modifier
) {
    val stateColor by animateColorAsState(
        targetValue = when (state) {
            TxState.IDLE -> TxCyanPrimary
            TxState.LISTENING -> TxStateListening
            TxState.THINKING -> TxStateThinking
            TxState.SPEAKING -> TxStateSpeaking
            TxState.EXECUTING -> TxStateExecuting
            TxState.ERROR -> TxStateError
        },
        label = "state_color_anim"
    )

    val labelText = when (state) {
        TxState.IDLE -> "STANDBY"
        TxState.LISTENING -> "LISTENING"
        TxState.THINKING -> "ANALYZING"
        TxState.SPEAKING -> "TRANSMITTING"
        TxState.EXECUTING -> "EXECUTING"
        TxState.ERROR -> "SYSTEM ALERT"
    }

    Row(
        modifier = modifier
            .testTag("activity_status_badge")
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0D1527))
            .border(1.dp, stateColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(stateColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = labelText,
            color = stateColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "•  $detailText",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}
