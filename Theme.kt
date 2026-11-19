package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TxCyanPrimary,
    onPrimary = TxBackgroundDark,
    primaryContainer = TxSurfaceVariantDark,
    onPrimaryContainer = TxCyanPrimary,
    secondary = TxCyanSecondary,
    onSecondary = TxBackgroundDark,
    secondaryContainer = TxSurfaceDark,
    onSecondaryContainer = TxCyanSecondary,
    tertiary = TxNeonPurple,
    onTertiary = TxBackgroundDark,
    background = TxBackgroundDark,
    onBackground = TxTextPrimary,
    surface = TxSurfaceDark,
    onSurface = TxTextPrimary,
    surfaceVariant = TxSurfaceVariantDark,
    onSurfaceVariant = TxTextSecondary,
    error = TxStateError,
    onError = TxBackgroundDark
)

@Composable
fun TxTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
