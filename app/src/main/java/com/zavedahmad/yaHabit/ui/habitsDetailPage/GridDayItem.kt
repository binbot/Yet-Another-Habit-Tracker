package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.unit.sp
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
    
    val isDark = isSystemInDarkTheme()
    
    // EXTREMELY HIGH CONTRAST BUCKETS (GitHub Style)
    // No more subtle blending. Each level has a massive, perceptible jump.
    val emptyColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE0E0E0)
    var bgColor: Color = emptyColor
    
    val goal = habitEntity?.repetitionPerDay ?: 1.0

    when {
        state.contains("Disabled") -> {
            bgColor = emptyColor.copy(alpha = 0.3f)
        }

        state.startsWith("skip") -> {
            bgColor = tertiaryColor
            buttonAction = listOf(unSkipHabit, { isDialogVisible.value = true })
        }

        habitEntity?.isNegative == true -> {
            // NEGATIVE HABIT (e.g. Limit Coffee)
            if (repetitionsOnThisDay == 0.0) {
                bgColor = primaryColor // High Success = Bold Primary
            } else if (repetitionsOnThisDay <= goal) {
                // SUCCESS LEVELS: Getting lighter as we approach the limit
                val ratio = (repetitionsOnThisDay / goal).toFloat()
                bgColor = when {
                    ratio <= 0.4 -> primaryColor.copy(alpha = 0.6f)
                    else -> primaryColor.copy(alpha = 0.3f)
                }
            } else {
                // FAILURE: Solid Secondary Color (Very dark/high contrast)
                bgColor = secondaryColor
            }
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
        }

        else -> {
            // POSITIVE HABIT (e.g. Water)
            if (repetitionsOnThisDay == 0.0) {
                bgColor = emptyColor
            } else {
                val ratio = (repetitionsOnThisDay / goal).toFloat()
                // EXPLICIT HIGH-CONTRAST Tiers
                bgColor = when {
                    ratio < 0.30 -> primaryColor.copy(alpha = 0.20f) // Level 1 (Faint)
                    ratio < 0.60 -> primaryColor.copy(alpha = 0.50f) // Level 2 (Medium)
                    ratio < 1.00 -> primaryColor.copy(alpha = 0.80f) // Level 3 (Strong)
                    else -> if (repetitionsOnThisDay > goal) secondaryColor else primaryColor // Level 4 (Perfect/Extra)
                }
            }
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
        }
    }

    val textColor = if (bgColor.luminance() > 0.5f) Color.Black else Color.White
    val modifier = if (interactive) {
        Modifier.combinedClickable(onClick = {
            buttonAction[0]()
        }, onLongClick = buttonAction[1])
    } else Modifier

    // Wrap in a solid background Box to ensure alpha doesn't wash out
    Box(
        modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(4.dp))
            .background(color = if (isDark) Color(0xFF121212) else Color.White)
            .background(color = bgColor),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (hasNote && interactive) {
            Surface(
                shape = CircleShape,
                modifier = Modifier
                    .size(10.dp)
                    .padding(2.dp),
                color = textColor.copy(alpha = 0.8f)
            ) {}
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (state.startsWith("skip")) {
                Icon(
                    Icons.Default.DoubleArrow,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
            } else if (showDate) {
                Text(
                    text = date.dayOfMonth.toString(), 
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp
                )
            }
        }
    }
}
