package com.parsping.vpn.ui.theme

import androidx.compose.ui.graphics.Color

// Default Pars Ping Cyberpunk Colors
val ParsPingBackground = Color(0xFF030303)
val ParsPingCardDark = Color(0xFF0D0D10)
val ParsPingCardBorder = Color(0xFF1E1E24)
val ParsPingTextPrimary = Color(0xFFFFFFFF)
val ParsPingTextSecondary = Color(0xFF9E9EA8)

// 5 Specified Themes
val AccentRedGang = Color(0xFFFF0033)       // 1. قرمز گنگ (Default)
val SecondaryDarkCrimson = Color(0xFF8B001B)

val AccentNeonBlue = Color(0xFF168CFF)      // 2. آبی نئونی
val SecondaryNeonBlue = Color(0xFF0A4A8F)

val AccentCyberPurple = Color(0xFFB026FF)   // 3. بنفش سایبر
val SecondaryCyberPurple = Color(0xFF5A108C)

val AccentNeonGreen = Color(0xFF00D084)     // 4. سبز نئونی
val SecondaryNeonGreen = Color(0xFF006B42)

val AccentFireOrange = Color(0xFFFF8500)    // 5. نارنجی فایر
val SecondaryFireOrange = Color(0xFF8F4700)

data class ParsPingThemePreset(
    val id: String,
    val nameFa: String,
    val accentColor: Color,
    val secondaryColor: Color,
    val hexCode: String
)

val AvailableThemes = listOf(
    ParsPingThemePreset("red_gang", "قرمز گنگ (پیش‌فرض)", AccentRedGang, SecondaryDarkCrimson, "#FF0033"),
    ParsPingThemePreset("neon_blue", "آبی نئونی", AccentNeonBlue, SecondaryNeonBlue, "#168CFF"),
    ParsPingThemePreset("cyber_purple", "بنفش سایبر", AccentCyberPurple, SecondaryCyberPurple, "#B026FF"),
    ParsPingThemePreset("neon_green", "سبز نئونی", AccentNeonGreen, SecondaryNeonGreen, "#00D084"),
    ParsPingThemePreset("fire_orange", "نارنجی فایر", AccentFireOrange, SecondaryFireOrange, "#FF8500")
)
