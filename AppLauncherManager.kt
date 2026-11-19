package com.example.applications

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings

data class AppRegistryItem(
    val id: String,
    val displayName: String,
    val aliases: List<String>,
    val packageName: String?,
    val intentAction: String? = null,
    val category: String? = null,
    val webFallbackUrl: String? = null
)

class AppLauncherManager(private val context: Context) {

    val supportedApps = listOf(
        AppRegistryItem(
            id = "youtube",
            displayName = "YouTube",
            aliases = listOf("youtube", "yt", "you tube"),
            packageName = "com.google.android.youtube",
            webFallbackUrl = "https://www.youtube.com"
        ),
        AppRegistryItem(
            id = "chrome",
            displayName = "Google Chrome",
            aliases = listOf("chrome", "browser", "google chrome", "web browser"),
            packageName = "com.android.chrome",
            webFallbackUrl = "https://www.google.com"
        ),
        AppRegistryItem(
            id = "maps",
            displayName = "Google Maps",
            aliases = listOf("maps", "google maps", "navigation", "gps"),
            packageName = "com.google.android.apps.maps",
            webFallbackUrl = "https://maps.google.com"
        ),
        AppRegistryItem(
            id = "calculator",
            displayName = "Calculator",
            aliases = listOf("calculator", "calc"),
            packageName = "com.google.android.calculator",
            category = Intent.CATEGORY_APP_CALCULATOR
        ),
        AppRegistryItem(
            id = "camera",
            displayName = "Camera",
            aliases = listOf("camera", "cam", "take photo", "take a picture"),
            packageName = null,
            intentAction = MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA
        ),
        AppRegistryItem(
            id = "settings",
            displayName = "Settings",
            aliases = listOf("settings", "system settings", "phone settings"),
            packageName = null,
            intentAction = Settings.ACTION_SETTINGS
        ),
        AppRegistryItem(
            id = "clock",
            displayName = "Clock / Alarm",
            aliases = listOf("clock", "alarm", "stopwatch", "timer"),
            packageName = "com.google.android.deskclock",
            intentAction = "android.intent.action.SHOW_ALARMS"
        ),
        AppRegistryItem(
            id = "playstore",
            displayName = "Google Play Store",
            aliases = listOf("play store", "google play", "store", "app store"),
            packageName = "com.android.vending",
            webFallbackUrl = "https://play.google.com/store"
        )
    )

    fun openAppByName(appNameQuery: String): LaunchResult {
        val normalized = appNameQuery.trim().lowercase()

        // Match against registry
        val matchedApp = supportedApps.firstOrNull { app ->
            app.displayName.lowercase() == normalized ||
                    app.aliases.any { alias -> normalized.contains(alias) || alias.contains(normalized) }
        }

        if (matchedApp != null) {
            return launchRegistryItem(matchedApp)
        }

        // Try direct package launch if query resembles a package or general installed query
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(normalized)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return LaunchResult(
                isSuccess = true,
                message = "Launched $normalized successfully."
            )
        }

        return LaunchResult(
            isSuccess = false,
            message = "Application \"$appNameQuery\" is not in the approved registry or not installed. Supported applications include: YouTube, Chrome, Maps, Calculator, Camera, Settings, and Clock."
        )
    }

    private fun launchRegistryItem(item: AppRegistryItem): LaunchResult {
        val pm = context.packageManager

        // 1. Try package intent
        if (!item.packageName.isNullOrBlank()) {
            val intent = pm.getLaunchIntentForPackage(item.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                return try {
                    context.startActivity(intent)
                    LaunchResult(true, "Opened ${item.displayName}.")
                } catch (e: Exception) {
                    LaunchResult(false, "Failed to launch ${item.displayName}: ${e.message}")
                }
            }
        }

        // 2. Try category intent (e.g. calculator)
        if (item.category != null) {
            val categoryIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(item.category)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(categoryIntent)
                return LaunchResult(true, "Opened ${item.displayName}.")
            } catch (e: Exception) {
                // fallback
            }
        }

        // 3. Try action intent (e.g. camera, settings)
        if (item.intentAction != null) {
            val actionIntent = Intent(item.intentAction).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(actionIntent)
                return LaunchResult(true, "Opened ${item.displayName}.")
            } catch (e: Exception) {
                // fallback
            }
        }

        // 4. Try web fallback
        if (item.webFallbackUrl != null) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.webFallbackUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return try {
                context.startActivity(webIntent)
                LaunchResult(true, "${item.displayName} is not installed natively, opened via web fallback.")
            } catch (e: Exception) {
                LaunchResult(false, "Could not open ${item.displayName} or its web fallback.")
            }
        }

        return LaunchResult(
            false,
            "${item.displayName} is not installed on this device."
        )
    }

    fun openWebsite(url: String): LaunchResult {
        var cleanUrl = url.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            LaunchResult(true, "Opened website: $cleanUrl")
        } catch (e: Exception) {
            LaunchResult(false, "Failed to open URL $url: ${e.message}")
        }
    }
}

data class LaunchResult(
    val isSuccess: Boolean,
    val message: String
)
