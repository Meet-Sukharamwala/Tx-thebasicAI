package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agent.TxState
import com.example.ui.TxViewModel
import com.example.ui.components.ActivityStatusBadge
import com.example.ui.components.AudioWaveform
import com.example.ui.components.QuickActionChips
import com.example.ui.components.TranscriptView
import com.example.ui.components.TxOrb
import com.example.ui.theme.TxBackgroundDark
import com.example.ui.theme.TxCyanPrimary
import com.example.ui.theme.TxStateError
import com.example.ui.theme.TxStateSpeaking

@Composable
fun TxDashboardScreen(
    viewModel: TxViewModel,
    onRequestRecordAudioPermission: () -> Unit,
    hasRecordAudioPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val state by viewModel.coordinator.currentState.collectAsState()
    val statusDetail by viewModel.coordinator.statusDetail.collectAsState()
    val audioRms by viewModel.speechManager.audioRms.collectAsState()
    val typedCommand by viewModel.typedCommand.collectAsState()
    val messages by viewModel.conversationHistory.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TxBackgroundDark),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Tx Monogram Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F1A30))
                        .border(1.dp, TxCyanPrimary, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tx",
                        color = TxCyanPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Tx Assistant",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (viewModel.isApiKeyConfigured) "Gemini Neural Core Active" else "Tx Offline Neural Engine Active",
                        color = if (viewModel.isApiKeyConfigured) TxCyanPrimary else Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }
            }

            // Mute / Stop spoken playback button
            AnimatedVisibility(visible = isSpeaking) {
                IconButton(
                    onClick = { viewModel.coordinator.stopSpeaking() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TxStateSpeaking.copy(alpha = 0.2f))
                        .border(1.dp, TxStateSpeaking, CircleShape)
                        .testTag("stop_speaking_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeOff,
                        contentDescription = "Stop Speaking",
                        tint = TxStateSpeaking,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Current Activity Status Badge
        ActivityStatusBadge(
            state = state,
            detailText = statusDetail,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Central Animated Tx Orb
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            TxOrb(
                state = state,
                audioRms = audioRms,
                onClick = {
                    if (!hasRecordAudioPermission) {
                        onRequestRecordAudioPermission()
                    } else {
                        viewModel.coordinator.startListening()
                    }
                },
                size = 190.dp
            )
        }

        // Reactive Audio Waveform
        AudioWaveform(
            state = state,
            audioRms = audioRms,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Quick Action Chips
        QuickActionChips(
            onActionSelected = { command ->
                viewModel.coordinator.processCommand(command)
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Conversation Transcript (weight fills remaining space)
        TranscriptView(
            messages = messages,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        // Bottom Input Command Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Input
            OutlinedTextField(
                value = typedCommand,
                onValueChange = { viewModel.setTypedCommand(it) },
                placeholder = {
                    Text(
                        text = "Speak or type command...",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF0F1A30),
                    unfocusedContainerColor = Color(0xFF0D1527),
                    focusedBorderColor = TxCyanPrimary,
                    unfocusedBorderColor = Color(0xFF1E355B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFE2E8F0)
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { viewModel.submitTypedCommand() })
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (typedCommand.isNotBlank()) {
                // Send text button
                IconButton(
                    onClick = { viewModel.submitTypedCommand() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(TxCyanPrimary)
                        .testTag("send_command_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Command",
                        tint = TxBackgroundDark
                    )
                }
            } else {
                // Microphone button
                val isListening = state == TxState.LISTENING
                IconButton(
                    onClick = {
                        if (!hasRecordAudioPermission) {
                            onRequestRecordAudioPermission()
                        } else {
                            viewModel.coordinator.startListening()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) TxStateError else TxCyanPrimary
                        )
                        .testTag("mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Start Voice Command",
                        tint = TxBackgroundDark
                    )
                }
            }
        }
    }
}
