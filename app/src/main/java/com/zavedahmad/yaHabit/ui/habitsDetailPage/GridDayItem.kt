package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material3.Icon
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
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import java.time.LocalDate

@Composable
fun GridDayItem(
    state: String = "error",
    incrementHabit: () -> Unit = {},
    date: LocalDate,
    repetitionsOnThisDay: Double = 0.0,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    tertiaryColor: Color = MaterialTheme.colorScheme.tertiary,
    deleteHabit: () -> Unit = {},
    showDate: Boolean = false,
    interactive: Boolean = false,
    skipHabit: () -> Unit,
    hasNote: Boolean = false,
    unSkipHabit: () -> Unit,
    dialogueComposable: @Composable (Boolean, () -> Unit) -> Unit,
    habitEntity: HabitEntity? = null
) {
    val isDialogVisible = remember { mutableStateOf(false) }
    var buttonAction: List<() -> Unit> = listOf({}, {})
    dialogueComposable(isDialogVisible.value, { isDialogVisible.value = false })
    
    var bgColor: Color = Color.Transparent
    var textColor: Color = Color.Transparent
    var noteIndicatorColor: Color = Color.Transparent

    val goal = habitEntity?.repetitionPerDay ?: 1.0

    when {
        state.contains("Disabled") -> {
            bgColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.05f)
            textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            noteIndicatorColor = Color.Transparent
        }

        state.startsWith("skip") -> {
            bgColor = tertiaryColor.copy(alpha = 0.4f)
            textColor = tertiaryColor
            buttonAction = listOf(unSkipHabit, { isDialogVisible.value = true })
            noteIndicatorColor = tertiaryColor
        }

        habitEntity?.isNegative == true -> {
            // NEGATIVE HABIT: Goal is to stay UNDER the limit. 
            // 0 reps = Level 0 (best success), > limit = Level 4 (dark failure)
            if (repetitionsOnThisDay == 0.0) {
                bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                textColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            } else if (repetitionsOnThisDay <= goal) {
                // SUCCESS LEVELS: Lighter shades of habit color.
                val ratio = (repetitionsOnThisDay / goal).toFloat()
                if (ratio <= 0.5) {
                    bgColor = primaryColor.copy(alpha = 0.3f) // Level 1 (Well under)
                } else {
                    bgColor = primaryColor.copy(alpha = 0.5f) // Level 2 (Close to limit)
                }
                textColor = primaryColor
            } else {
                // FAILURE LEVELS: Darker shades of habit color.
                val overage = repetitionsOnThisDay - goal
                // Cap at 2x overage for max intensity
                val overageRatio = (overage / goal).toFloat()
                if (overageRatio <= 1.0) {
                    bgColor = primaryColor.copy(alpha = 0.8f) // Level 3 (Hit the limit/slightly over)
                } else {
                    bgColor = primaryColor.copy(alpha = 1.0f) // Level 4 (FAILED / Way over)
                }
                textColor = if (bgColor.luminance() > 0.5f) Color.Black else Color.White
            }
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            noteIndicatorColor = if (bgColor.luminance() > 0.5f) primaryColor else Color.White
        }

        else -> {
            // POSITIVE HABIT: Goal is to reach or exceed. Darker = Better.
            if (repetitionsOnThisDay == 0.0) {
                bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                textColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            } else {
                val ratio = (repetitionsOnThisDay / goal).toFloat()
                when {
                    ratio < 0.4 -> bgColor = primaryColor.copy(alpha = 0.3f) // Level 1 (Low)
                    ratio < 0.8 -> bgColor = primaryColor.copy(alpha = 0.6f) // Level 2 (Med)
                    ratio < 1.0 -> bgColor = primaryColor.copy(alpha = 0.8f) // Level 3 (High)
                    else -> bgColor = primaryColor.copy(alpha = 1.0f)        // Level 4 (Goal Met/Overage)
                }
                textColor = if (bgColor.luminance() > 0.5f) Color.Black else Color.White
            }
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            noteIndicatorColor = if (bgColor.luminance() > 0.5f) primaryColor else Color.White
        }
    }

    val modifier = if (interactive) {
        Modifier.combinedClickable(onClick = {
            buttonAction[0]()
        }, onLongClick = buttonAction[1])
    } else Modifier

    Box(
        modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(5.dp))
            .background(color = bgColor),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (hasNote && interactive) {
            Surface(
                shape = CircleShape,
                modifier = Modifier
                    .size(10.dp)
                    .padding(2.dp),
                color = noteIndicatorColor.copy(alpha = 0.8f)
            ) {}
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (state.startsWith("skip")) {
                Icon(
                    Icons.Default.DoubleArrow,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            } else if (showDate) {
                Text(
                    text = date.dayOfMonth.toString(), 
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
