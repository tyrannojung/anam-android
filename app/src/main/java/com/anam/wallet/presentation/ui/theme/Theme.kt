package com.anam.wallet.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 다크 모드 컬러 스킴
private val AnamDarkColorScheme = darkColorScheme(
    primary = AnamPrimary,
    secondary = AnamSecondary,
    tertiary = AnamAqua,
    background = AnamDarkBackground,
    surface = AnamDarkSurface,
    onPrimary = AnamWhite,
    onSecondary = AnamTextDark,
    onTertiary = AnamTextDark,
    onBackground = AnamTextLight,
    onSurface = AnamTextLight,
    primaryContainer = AnamDarkSurface,
    onPrimaryContainer = AnamTextLight,
    outline = AnamDarkBorder,
    surfaceVariant = AnamDarkSurface,
    onSurfaceVariant = AnamTextSecondary
)

// 라이트 모드 컬러 스킴
private val AnamLightColorScheme = lightColorScheme(
    primary = AnamPrimary,
    secondary = AnamSecondary,
    tertiary = AnamAqua,
    background = AnamLight,
    surface = AnamLightSurface,
    onPrimary = AnamWhite,
    onSecondary = AnamTextDark,
    onTertiary = AnamTextDark,
    onBackground = AnamTextDark,
    onSurface = AnamTextDark,
    primaryContainer = AnamLightSurface,
    onPrimaryContainer = AnamTextDark,
    outline = AnamLightBorder,
    surfaceVariant = AnamLightBackground,
    onSurfaceVariant = AnamTextSecondary
)

// 테마 설정을 위한 CompositionLocal
val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

@Composable
fun AnamwalletTheme(
    themeMode: ThemeMode = LocalThemeMode.current,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    android.util.Log.d("AnamwalletTheme", "Theme mode: $themeMode, Dark theme: $darkTheme")
    
    val colorScheme = if (darkTheme) {
        AnamDarkColorScheme
    } else {
        AnamLightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalThemeMode provides themeMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}