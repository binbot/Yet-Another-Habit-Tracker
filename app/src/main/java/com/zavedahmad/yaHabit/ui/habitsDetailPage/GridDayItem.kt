package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import java.time.LocalDate

private val FailedRed = Color(0xFFF44336)

/**
 * Heatmap cell visuals driven by [DayClass]:
 * solid habit color = done/clean, red tint = missed, solid red = over limit.
 */
private data class GridVisuals(val bg: Color, val noteDot: Color, val text: Color)

private fun resolveGridVisuals(dayClass: DayClass, cs: ColorScheme): GridVisuals {
    val bg = when (dayClass) {
        DayClass.MET -> cs.primary.copy(alpha = 0.95f)
        DayClass.MISSED -> FailedRed.copy(alpha = 0.20f)
        DayClass.OVER_LIMIT -> FailedRed.copy(alpha = 0.90f)
        DayClass.SKIP -> cs.tertiaryContainer
        DayClass.EXCUSED -> cs.surfaceVariant.copy(alpha = 0.45f)
        DayClass.NEUTRAL -> cs.inverseSurface.copy(alpha = 0.04f)
    }
    val text = if (bg.luminance() > 0.5f) Color(0xFF1C1B1F) else Color.White
    return GridVisuals(
        bg = bg,
        noteDot = if (bg.luminance() > 0.5f) cs.tertiary else cs.tertiaryContainer,
        text = text
    )
}

@Composable
fun GridDayItem(
    dayClass: DayClass,
    date: LocalDate,
    showDate: Boolean = false,
    interactive: Boolean = false,
    hasNote: Boolean = false,
    incrementHabit: () -> Unit = {},
    unSkipHabit: () -> Unit = {},
    dialogueComposable: @Composable (Boolean, () -> Unit) -> Unit
) {
    val isDialogVisible = remember { mutableStateOf(false) }
    dialogueComposable(isDialogVisible.value, { isDialogVisible.value = false })

    val cs = MaterialTheme.colorScheme
    val visuals = remember(dayClass, cs) { resolveGridVisuals(dayClass, cs) }

    val modifier = if (interactive) {
        Modifier.combinedClickable(
            onClick = if (dayClass == DayClass.SKIP) unSkipHabit else incrementHabit,
            onLongClick = { isDialogVisible.value = true }
        )
    } else Modifier

    Box(
        modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(5.dp))
            .background(color = visuals.bg),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (hasNote && interactive) {
            Surface(
                shape = CircleShape,
                modifier = Modifier
                    .size(15.dp)
                    .padding(3.dp),
                shadowElevation = 100.dp,
                color = visuals.noteDot
            ) {}
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (showDate) {
                Text(date.dayOfMonth.toString(), color = visuals.text)
            }
        }
    }
}
