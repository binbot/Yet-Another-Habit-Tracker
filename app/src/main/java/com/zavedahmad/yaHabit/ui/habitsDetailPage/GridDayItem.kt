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
import androidx.compose.ui.text.style.TextAlign
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
    
    // HIGH CONTRAST PALETTE (No more subtle alpha blending)
    val emptyGrey = Color(0xFFEEEEEE) // Light grey for empty days
    val darkGrey = Color(0xFF757575)  // Darker grey for disabled/future
    
    var bgColor: Color = emptyGrey
    var textColor: Color = Color.Black
    var noteIndicatorColor: Color = Color.Transparent

    val goal = habitEntity?.repetitionPerDay ?: 1.0

    when {
        state.contains("Disabled") -> {
            bgColor = emptyGrey.copy(alpha = 0.5f)
            textColor = darkGrey.copy(alpha = 0.5f)
            noteIndicatorColor = Color.Transparent
        }

        state.startsWith("skip") -> {
            bgColor = tertiaryColor.copy(alpha = 0.4f)
            textColor = tertiaryColor
            buttonAction = listOf(unSkipHabit, { isDialogVisible.value = true })
            noteIndicatorColor = tertiaryColor
        }

        habitEntity?.isNegative == true -> {
            // NEGATIVE HABIT (e.g. Limit Coffee)
            if (repetitionsOnThisDay == 0.0) {
                bgColor = primaryColor // High success = Solid Color
                textColor = if (bgColor.luminance() > 0.5f) Color.Black else Color.White
            } else if (repetitionsOnThisDay <= goal) {
                // Success Variation - clearly fading out as you approach the limit
                val ratio = (repetitionsOnThisDay / goal).toFloat()
                bgColor = primaryColor.copy(alpha = (1.0f - (0.7f * ratio)).coerceIn(0.1f, 1.0f))
                textColor = primaryColor
            } else {
                // FAILED: Solid Dark Failure Color (Secondary theme color)
                bgColor = secondaryColor
                textColor = if (bgColor.luminance() > 0.5f) Color.Black else Color.White
            }
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            noteIndicatorColor = if (bgColor.luminance() > 0.5f) primaryColor else Color.White
        }

        else -> {
            // POSITIVE HABIT (e.g. Water)
            if (repetitionsOnThisDay == 0.0) {
                bgColor = emptyGrey
                textColor = darkGrey
            } else {
                val ratio = (repetitionsOnThisDay / goal).toFloat()
                // EXPLICIT COLOR BUCKETS (mHabit/GitHub style)
                bgColor = when {
                    ratio < 0.33 -> primaryColor.copy(alpha = 0.25f) // Level 1: Faint
                    ratio < 0.66 -> primaryColor.copy(alpha = 0.55f) // Level 2: Medium
                    ratio < 1.00 -> primaryColor.copy(alpha = 0.85f) // Level 3: Strong
                    else -> primaryColor                             // Level 4: Solid (Goal Met)
                }
                // If exceeded, use the secondary color to make it "pop"
                if (repetitionsOnThisDay > goal) {
                    bgColor = secondaryColor
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

    // We use a Surface with a solid card-like background underneath to ensure 
    // the alpha variations don't just blend into the screen background.
    Box(
        modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(4.dp))
            .background(color = Color.White) // Solid base for contrast
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
