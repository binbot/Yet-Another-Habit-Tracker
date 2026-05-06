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
import androidx.compose.material.icons.filled.Check
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
    
    // Pull the TRUE palette from the MaterialTheme (Source of Truth)
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    
    var bgColor: Color = Color.Transparent
    var textColor: Color = Color.Transparent
    var noteIndicatorColor: Color = Color.Transparent

    val goal = habitEntity?.repetitionPerDay ?: 1.0

    when {
        state.startsWith("absolute") -> {
            bgColor = primaryColor
            textColor = if (primaryColor.luminance() > 0.5f) Color.Black else Color.White
            buttonAction = listOf(skipHabit, { isDialogVisible.value = true })
            noteIndicatorColor = textColor
        }

        state.startsWith("partial") -> {
            // Success progress intensity for positive habits
            val ratio = if (goal > 0) (repetitionsOnThisDay / goal).toFloat().coerceIn(0.1f, 0.9f) else 0.5f
            bgColor = primaryColor.copy(alpha = 0.2f + 0.6f * ratio)
            textColor = primaryColor
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            noteIndicatorColor = primaryColor
        }

        state.startsWith("failed") -> {
            // Failure intensity for negative habits (staying in the theme color)
            val overageRatio = if (goal > 0) ((repetitionsOnThisDay - goal) / goal).toFloat().coerceIn(0.1f, 1.0f) else 0.5f
            bgColor = secondaryColor.copy(alpha = 0.4f + 0.6f * overageRatio)
            textColor = if (bgColor.luminance() > 0.5f) Color.Black else Color.White
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            noteIndicatorColor = textColor
        }

        state.startsWith("skip") -> {
            bgColor = tertiaryColor.copy(alpha = 0.4f)
            textColor = tertiaryColor
            buttonAction = listOf(unSkipHabit, { isDialogVisible.value = true })
            noteIndicatorColor = tertiaryColor
        }

        state.contains("Disabled") -> {
            bgColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.05f)
            textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            noteIndicatorColor = Color.Transparent
        }

        else -> { // incomplete / empty
            bgColor = surfaceColor.copy(alpha = 0.5f)
            textColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            noteIndicatorColor = tertiaryColor
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
                    .size(14.dp)
                    .padding(3.dp),
                color = noteIndicatorColor
            ) {}
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                state.startsWith("skip") -> {
                    Icon(
                        Icons.Default.DoubleArrow,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                state == "absoluteMore" -> {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                showDate -> {
                    Text(
                        text = date.dayOfMonth.toString(), 
                        color = textColor,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
