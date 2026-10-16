package com.zavedahmad.yahabit.common

import androidx.compose.ui.graphics.Color

/**
 * Default accent seeds for the app chrome when no per-item accent is supplied.
 *
 * Single source of truth shared by the app theme (`YaHabitTheme`) and the home-screen widget, so
 * both render the same default palette: a warm yellow on dark surfaces, a warm red on light ones.
 */
object AppThemeSeeds {
    val Dark = Color(0xFFFFD037)
    val Light = Color(0xFFD32F2F)
}
