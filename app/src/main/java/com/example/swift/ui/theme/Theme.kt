package com.example.swift.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SwiftColorScheme = lightColorScheme(
    primary = SwiftRed,
    onPrimary = SwiftWhite,
    primaryContainer = SwiftRedLight,
    onPrimaryContainer = SwiftWhite,
    secondary = SwiftDarkTeal,
    onSecondary = SwiftWhite,
    secondaryContainer = SwiftTeal,
    onSecondaryContainer = SwiftWhite,
    tertiary = SwiftTeal,
    onTertiary = SwiftDarkTeal,
    background = SwiftPinkBg,
    onBackground = SwiftBlack,
    surface = SwiftWhite,
    onSurface = SwiftBlack,
    surfaceVariant = SwiftGrayLight,
    onSurfaceVariant = SwiftGrayDark,
    outline = SwiftGrayMedium,
    error = Color(0xFFDC2626),
    onError = SwiftWhite
)

@Composable
fun SwiftTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SwiftRed.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = SwiftColorScheme,
        typography = SwiftTypography,
        content = content
    )
}