package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TxViewModel
import com.example.ui.theme.TxBackgroundDark
import com.example.ui.theme.TxCyanPrimary

data class AppGridItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val command: String
)

@Composable
fun AppLauncherScreen(
    viewModel: TxViewModel,
    modifier: Modifier = Modifier
) {
    val apps = listOf(
        AppGridItem("youtube", "YouTube", Icons.Default.SmartDisplay, "YouTube"),
        AppGridItem("chrome", "Chrome", Icons.Default.Language, "Chrome"),
        AppGridItem("maps", "Google Maps", Icons.Default.Map, "Google Maps"),
        AppGridItem("calc", "Calculator", Icons.Default.Calculate, "Calculator"),
        AppGridItem("camera", "Camera", Icons.Default.CameraAlt, "Camera"),
        AppGridItem("settings", "Settings", Icons.Default.Settings, "Settings"),
        AppGridItem("clock", "Clock", Icons.Default.Schedule, "Clock"),
        AppGridItem("playstore", "Play Store", Icons.Default.Shop, "Google Play Store")
    )

    var urlInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TxBackgroundDark)
            .padding(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Tx Application Hub",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "System package resolution and authorized deep links",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Web Launcher Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D1527))
                .border(1.dp, Color(0xFF1E355B), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "Open Approved Website",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TxCyanPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = { Text("e.g. wikipedia.org", color = Color(0xFF64748B), fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).testTag("url_launcher_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0F1A30),
                            unfocusedContainerColor = Color(0xFF0F1A30),
                            focusedBorderColor = TxCyanPrimary,
                            unfocusedBorderColor = Color(0xFF1E355B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (urlInput.isNotBlank()) {
                                viewModel.coordinator.processCommand("open this website: $urlInput")
                                urlInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TxCyanPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("launch_url_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            tint = TxBackgroundDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open", color = TxBackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Supported Android Applications",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Grid of Apps
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize().testTag("app_grid")
        ) {
            items(apps, key = { it.id }) { app ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D1527))
                        .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.launchAppDirect(app.command)
                        }
                        .padding(14.dp)
                        .testTag("app_tile_${app.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F1A30))
                                .border(1.dp, TxCyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = app.icon,
                                contentDescription = null,
                                tint = TxCyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = app.name,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tap to open",
                                color = Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
