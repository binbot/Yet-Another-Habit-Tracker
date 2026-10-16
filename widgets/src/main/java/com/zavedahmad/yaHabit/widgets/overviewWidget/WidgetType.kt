package com.zavedahmad.yaHabit.widgets.overviewWidget

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle

/**
 * Material 3 tokens for the widget surface.
 *
 * Glance has no theme typography (only colors), so the app's type scale is mirrored here with
 * explicit [TextStyle]s. The values match the stock M3 scale the app uses (`AppTypography`), so
 * widget text sits on the same ramp as in-app text instead of hard-coding sizes at call sites.
 */
internal object WidgetType {
    /** titleLarge (22sp) — header/app-bar title. */
    val Title = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)

    /** bodyLarge (16sp) — primary row label (M3 list-item primary text). */
    val RowLabel = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)

    /** bodyLarge (16sp), medium — numeric row value. */
    val RowValue = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)

    /** bodyMedium (14sp) — secondary/empty-state text. */
    val Body = TextStyle(fontSize = 14.sp)
}

/** Shape scale used by widget chips: M3 `medium` (12dp), matching the app's card/chip corners. */
internal val WidgetChipRadius = 12.dp
