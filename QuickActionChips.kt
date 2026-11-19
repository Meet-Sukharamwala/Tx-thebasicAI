package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TxCyanPrimary

data class QuickAction(
    val id: String,
    val label: String,
    val command: String,
    val icon: ImageVector
)

@Composable
fun QuickActionChips(
    onActionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        QuickAction("time", "Current Time", "What time is it?", Icons.Default.Schedule),
        QuickAction("youtube", "Open YouTube", "Open YouTube", Icons.Default.SmartDisplay),
        QuickAction("reminder", "Remind in 10m", "Remind me in 10 minutes to check progress", Icons.Default.Alarm),
        QuickAction("tasks", "List Tasks", "Show my pending tasks", Icons.Default.CheckCircle),
        QuickAction("device", "Device Specs", "What is my device and battery status?", Icons.Default.Info),
        QuickAction("note", "Create Note", "Create a note titled Project Ideas with content Test Tx assistant", Icons.Default.NoteAdd)
    )

    LazyRow(
        modifier = modifier.testTag("quick_action_chips"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(actions, key = { it.id }) { action ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F1A30))
                    .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(20.dp))
                    .clickable { onActionSelected(action.command) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("quick_chip_${action.id}"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = null,
                        tint = TxCyanPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = action.label,
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
