package com.zavedahmad.yaHabit.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 shape scale.
 *
 * Every structural surface in the app should pull its corners from
 * [androidx.compose.material3.MaterialTheme.shapes] rather than hard-coding a radius, so the
 * rounded language stays consistent (cards, dialogs, sheets, text fields...). Chips keep their
 * M3 default (small, 8.dp); filled/outlined buttons are squared to [Shapes.medium] for the same
 * consistency, rather than the M3 stadium default.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
