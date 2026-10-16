package com.zavedahmad.yaHabit.widgets.overviewWidget

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import com.materialkolor.Contrast
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.zavedahmad.yahabit.common.AppThemeSeeds

/**
 * Color schemes for the widget, following the same rules as the app's `YaHabitTheme` so the widget
 * chrome and the app agree:
 *
 * - Material You wallpaper colors when the DynamicColor preference is on (API 31+),
 * - otherwise a MaterialKolor scheme generated from the shared default seed
 *   ([AppThemeSeeds]: warm yellow on dark, warm red on light),
 * - AMOLED pins surfaces to black, and is ignored while dynamic color is on.
 */
internal fun widgetChromeScheme(
    context: Context,
    isDark: Boolean,
    isAmoled: Boolean,
    dynamicColor: Boolean
): ColorScheme {
    if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    return dynamicColorScheme(
        primary = if (isDark) AppThemeSeeds.Dark else AppThemeSeeds.Light,
        isDark = isDark,
        isAmoled = isAmoled,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        contrastLevel = Contrast.Medium.value,
    )
}

/**
 * Accent scheme for a single habit/routine row — the same MaterialKolor generation the app does
 * per card, so a habit's widget chip uses its own accent color exactly like in the app.
 */
internal fun itemScheme(primary: Color, isDark: Boolean, isAmoled: Boolean): ColorScheme =
    dynamicColorScheme(
        primary = primary,
        isDark = isDark,
        isAmoled = isAmoled,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        contrastLevel = Contrast.Medium.value,
    )
