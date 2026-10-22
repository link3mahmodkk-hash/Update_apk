package com.parsping.vpn.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {

    private const val PREF_NAME = "parsping_theme_prefs"
    private const val KEY_THEME_ID = "selected_theme_id"
    private const val KEY_CUSTOM_COLOR_HEX = "custom_color_hex"

    private var prefs: SharedPreferences? = null

    private val _currentTheme = MutableStateFlow(AvailableThemes[0]) // Default: قرمز گنگ
    val currentTheme: StateFlow<ParsPingThemePreset> = _currentTheme.asStateFlow()

    private val _customColorHex = MutableStateFlow("#FF0033")
    val customColorHex: StateFlow<String> = _customColorHex.asStateFlow()

    private val _isCustomPickerActive = MutableStateFlow(false)
    val isCustomPickerActive: StateFlow<Boolean> = _isCustomPickerActive.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedThemeId = prefs?.getString(KEY_THEME_ID, "red_gang") ?: "red_gang"
        val savedCustomHex = prefs?.getString(KEY_CUSTOM_COLOR_HEX, "#FF0033") ?: "#FF0033"

        _customColorHex.value = savedCustomHex

        if (savedThemeId == "custom") {
            _isCustomPickerActive.value = true
            val parsedColor = parseHexToColor(savedCustomHex)
            _currentTheme.value = ParsPingThemePreset("custom", "تم سفارشی", parsedColor, parsedColor.copy(alpha = 0.5f), savedCustomHex)
        } else {
            _isCustomPickerActive.value = false
            val found = AvailableThemes.find { it.id == savedThemeId } ?: AvailableThemes[0]
            _currentTheme.value = found
        }
    }

    fun selectPreset(preset: ParsPingThemePreset) {
        _isCustomPickerActive.value = false
        _currentTheme.value = preset
        prefs?.edit()?.apply {
            putString(KEY_THEME_ID, preset.id)
            apply()
        }
    }

    fun setCustomColor(hexColor: String) {
        val cleanHex = if (hexColor.startsWith("#")) hexColor else "#$hexColor"
        val parsed = parseHexToColor(cleanHex)
        _customColorHex.value = cleanHex
        _isCustomPickerActive.value = true
        _currentTheme.value = ParsPingThemePreset("custom", "تم سفارشی", parsed, parsed.copy(alpha = 0.5f), cleanHex)

        prefs?.edit()?.apply {
            putString(KEY_THEME_ID, "custom")
            putString(KEY_CUSTOM_COLOR_HEX, cleanHex)
            apply()
        }
    }

    private fun parseHexToColor(hex: String): Color {
        return try {
            val clean = hex.removePrefix("#")
            val fullHex = if (clean.length == 6) "FF$clean" else clean
            Color(fullHex.toLong(16))
        } catch (_: Exception) {
            AccentRedGang
        }
    }
}
