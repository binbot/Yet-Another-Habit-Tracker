package com.zavedahmad.yaHabit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.Contrast
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import com.zavedahmad.yahabit.common.AppThemeSeeds

/**
 * Single Material 3 theme for the whole app.
 *
 * Builds one [MaterialTheme] combining an M3 color scheme (Material You wallpaper colors when
 * [dynamicColor] is on, otherwise a MaterialKolor scheme generated from [primaryColor] or the
 * default seed) with the shared [AppTypography] type scale and [AppShapes] shape scale.
 *
 * Detail/add screens pass the relevant habit/routine color as [primaryColor] to tint that
 * screen; the app chrome calls it without one.
 */
@Composable
fun YaHabitTheme(
    theme: String = "system",
    primaryColor: Color? = null,
    isAmoled: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        rememberDynamicColorScheme(
            primary = primaryColor ?: if (darkTheme) AppThemeSeeds.Dark else AppThemeSeeds.Light,
            isDark = darkTheme,
            isAmoled = isAmoled,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
            contrastLevel = Contrast.Medium.value,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
