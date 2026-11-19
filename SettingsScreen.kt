package com.example.ui.screens

import android.app.AlarmManager
import android.content.Context
import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TxViewModel
import com.example.ui.theme.TxBackgroundDark
import com.example.ui.theme.TxCyanPrimary
import com.example.ui.theme.TxStateError
import com.example.ui.theme.TxStateSpeaking

@Composable
fun SettingsScreen(
    viewModel: TxViewModel,
    hasRecordAudioPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestRecordAudioPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var speechRate by remember { mutableFloatStateOf(1.0f) }
    var speechPitch by remember { mutableFloatStateOf(1.0f) }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val canExactAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TxBackgroundDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Column {
            Text(
                text = "Tx System Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Assistant configuration, permissions, and privacy controls",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Engine Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D1527))
                .border(1.dp, Color(0xFF1E355B), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = TxCyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Processing Status",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val isConfigured = viewModel.isApiKeyConfigured
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isConfigured) TxStateSpeaking else TxCyanPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isConfigured) {
                            "Gemini 3.5 Flash Model: Active"
                        } else {
                            "Tx Local Neural Engine: Active (Offline Mode)"
                        },
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isConfigured) {
                        "Tx securely processes natural voice conversations using Google's Gemini models via BuildConfig."
                    } else {
                        "All voice tools (reminders, apps, notes, tasks, time) operate natively without external API keys. Configure GEMINI_API_KEY in the Secrets panel to enable full LLM capabilities."
                    },
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Permissions Diagnostics Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D1527))
                .border(1.dp, Color(0xFF1E355B), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "Android System Permissions",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Microphone Permission
                PermissionItem(
                    title = "Microphone (RECORD_AUDIO)",
                    description = "Required for Android SpeechRecognizer voice input.",
                    isGranted = hasRecordAudioPermission,
                    onRequest = onRequestRecordAudioPermission
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notifications Permission
                PermissionItem(
                    title = "Notifications (POST_NOTIFICATIONS)",
                    description = "Required to alert you when reminders trigger.",
                    isGranted = hasNotificationPermission,
                    onRequest = onRequestNotificationPermission
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Exact Alarm
                PermissionItem(
                    title = "Exact Alarms (SCHEDULE_EXACT_ALARM)",
                    description = "Allows precise scheduling of timed reminders.",
                    isGranted = canExactAlarm,
                    onRequest = null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Speech Synthesis Settings
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D1527))
                .border(1.dp, Color(0xFF1E355B), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = TxCyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice Synthesis Settings",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Speed
                Text(
                    text = "Speech Rate: ${String.format("%.1f", speechRate)}x",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp
                )
                Slider(
                    value = speechRate,
                    onValueChange = {
                        speechRate = it
                        viewModel.ttsManager.setSpeechRate(it)
                    },
                    valueRange = 0.6f..1.6f,
                    colors = SliderDefaults.colors(
                        thumbColor = TxCyanPrimary,
                        activeTrackColor = TxCyanPrimary,
                        inactiveTrackColor = Color(0xFF1E355B)
                    )
                )

                // Pitch
                Text(
                    text = "Pitch: ${String.format("%.1f", speechPitch)}x",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp
                )
                Slider(
                    value = speechPitch,
                    onValueChange = {
                        speechPitch = it
                        viewModel.ttsManager.setPitch(it)
                    },
                    valueRange = 0.7f..1.4f,
                    colors = SliderDefaults.colors(
                        thumbColor = TxCyanPrimary,
                        activeTrackColor = TxCyanPrimary,
                        inactiveTrackColor = Color(0xFF1E355B)
                    )
                )

                Button(
                    onClick = {
                        viewModel.ttsManager.speak("Tx voice synthesis calibration test. Systems are operating normally.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E355B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Test Voice", color = TxCyanPrimary, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy & Data
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D1527))
                .border(1.dp, Color(0xFF1E355B), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "Privacy & Local Data",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tx retains all transcript history and records in on-device SQLite Room storage. You can purge transcript history at any time.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.clearAllHistory() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF331822)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TxStateError.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = TxStateError,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear Transcript History", color = TxStateError, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isGranted) TxStateSpeaking else TxStateError,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(text = description, color = Color(0xFF64748B), fontSize = 11.sp)
        }
        if (!isGranted && onRequest != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(containerColor = TxCyanPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Allow", color = TxBackgroundDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
