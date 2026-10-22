package com.parsping.vpn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ParsPingColors(
    val background: Color = ParsPingBackground,
    val cardBackground: Color = ParsPingCardDark,
    val cardBorder: Color = ParsPingCardBorder,
    val primaryAccent: Color = AccentRedGang,
    val secondaryAccent: Color = SecondaryDarkCrimson,
    val textPrimary: Color = ParsPingTextPrimary,
    val textSecondary: Color = ParsPingTextSecondary
)

val LocalParsPingColors = staticCompositionLocalOf { ParsPingColors() }

@Composable
fun ParsPingTheme(
    content: @Composable () -> Unit
) {
    val currentPreset by ThemeManager.currentTheme.collectAsState()

    val customColors = ParsPingColors(
        background = ParsPingBackground,
        cardBackground = ParsPingCardDark,
        cardBorder = ParsPingCardBorder,
        primaryAccent = currentPreset.accentColor,
        secondaryAccent = currentPreset.secondaryColor,
        textPrimary = ParsPingTextPrimary,
        textSecondary = ParsPingTextSecondary
    )

    val materialColors = darkColorScheme(
        primary = currentPreset.accentColor,
        secondary = currentPreset.secondaryColor,
        background = ParsPingBackground,
        surface = ParsPingCardDark,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = ParsPingTextPrimary,
        onSurface = ParsPingTextPrimary
    )

    CompositionLocalProvider(LocalParsPingColors provides customColors) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}
