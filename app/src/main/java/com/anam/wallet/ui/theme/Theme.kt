package com.anam.wallet.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val AnamDarkColorScheme = darkColorScheme(
    primary = AnamBlue,
    secondary = AnamMediumGray,
    tertiary = AnamSuccess,
    background = AnamBackground,
    surface = AnamDarkGray,
    onPrimary = AnamWhite,
    onSecondary = AnamWhite,
    onTertiary = AnamWhite,
    onBackground = AnamWhite,
    onSurface = AnamWhite,
    outline = AnamBorderGray
)

private val AnamLightColorScheme = darkColorScheme(
    primary = AnamBlue,
    secondary = AnamMediumGray,
    tertiary = AnamSuccess,
    background = AnamBackground,
    surface = AnamDarkGray,
    onPrimary = AnamWhite,
    onSecondary = AnamWhite,
    onTertiary = AnamWhite,
    onBackground = AnamWhite,
    onSurface = AnamWhite,
    outline = AnamBorderGray
)

@Composable
fun AnamwalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = AnamDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}