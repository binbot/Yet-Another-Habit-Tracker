package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.zavedahmad.yaHabit.database.entities.DayState
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import java.time.LocalDate

/**
 * Plain heat cell: the calendar computes the shade, this just draws it.
 */
@Composable
fun GridDayItem(
    bg: Color,
    date: LocalDate,
    showDate: Boolean = false,
    interactive: Boolean = false,
    hasNote: Boolean = false,
    isSkipCell: Boolean = false,
    incrementHabit: () -> Unit = {},
    skipHabit: () -> Unit = {},
    unSkipHabit: () -> Unit = {},
    habit: HabitEntity? = null,
    state: DayState? = null,
    dialogueComposable: @Composable (Boolean, () -> Unit) -> Unit
) {
    val isDialogVisible = remember { mutableStateOf(false) }
    dialogueComposable(isDialogVisible.value, { isDialogVisible.value = false })

    val textColor = if (bg.luminance() > 0.5f) Color(0xFF1C1B1F) else Color.White

    val isYesNo = habit?.repetitionPerDay == 1.0 && habit?.isNegative == false
    val modifier = if (interactive) {
        Modifier.combinedClickable(
            onClick = {
                when {
                    isSkipCell -> unSkipHabit()
                    state == DayState.Absolute && isYesNo -> skipHabit()
                    state == DayState.AbsoluteMore -> skipHabit()
                    else -> incrementHabit()
                }
            },
            onLongClick = { isDialogVisible.value = true }
        )
    } else Modifier

    Box(
        modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(5.dp))
            .background(color = bg),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (hasNote && interactive) {
            Surface(
                shape = CircleShape,
                modifier = Modifier
                    .size(15.dp)
                    .padding(3.dp),
                shadowElevation = 100.dp,
                color = if (bg.luminance() > 0.5f) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.tertiaryContainer
            ) {}
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (showDate) {
                Text(date.dayOfMonth.toString(), color = textColor)
            }
        }
    }
}
