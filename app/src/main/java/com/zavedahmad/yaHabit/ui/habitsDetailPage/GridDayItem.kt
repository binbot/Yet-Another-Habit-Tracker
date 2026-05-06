package com.zavedahmad.yaHabit.ui.habitsDetailPage

import android.graphics.Paint
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
import androidx.compose.material.icons.filled.Close
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
import kotlin.collections.List

// Done implement note also
@Composable
fun GridDayItem(
    state: String = "error",
    incrementHabit: () -> Unit = {},
    date: LocalDate,
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
    var textColor = MaterialTheme.colorScheme.onError
    dialogueComposable(isDialogVisible.value, { isDialogVisible.value = false })
    var bgColor: Color
    var noteIndicatorColor: Color
    when (state) {
        "absolute" -> {
            bgColor = primaryColor
            textColor = if (primaryColor.luminance() > 0.5f) Color.Black else Color.White
            buttonAction = listOf(skipHabit, { isDialogVisible.value = true })
            noteIndicatorColor = textColor

        }

        "absoluteMore", "absoluteLess" -> {
            bgColor = primaryColor.copy(0.7f)
            textColor = primaryColor
            buttonAction = listOf(skipHabit, { isDialogVisible.value = true })
            noteIndicatorColor = primaryColor
        }

        "absoluteDisabled" -> {


            bgColor = MaterialTheme.colorScheme.inverseSurface.copy(0.8f)
            textColor = MaterialTheme.colorScheme.onSurface
            noteIndicatorColor = MaterialTheme.colorScheme.surfaceVariant

        }


        "partial" -> {
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })

            bgColor = primaryColor.copy(0.3f)
            textColor = primaryColor
            noteIndicatorColor = primaryColor


        }

        "partialDisabled" -> {


            bgColor = MaterialTheme.colorScheme.inverseSurface.copy(0.1f)
            textColor = MaterialTheme.colorScheme.inverseOnSurface
            noteIndicatorColor = MaterialTheme.colorScheme.surfaceVariant

        }


        "incompleteDisabled" -> {

            bgColor = MaterialTheme.colorScheme.inverseSurface.copy(0.05f)
            textColor = MaterialTheme.colorScheme.inverseOnSurface
            noteIndicatorColor = MaterialTheme.colorScheme.tertiaryContainer

        }

        "incomplete", "empty" -> {
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            bgColor = MaterialTheme.colorScheme.surfaceVariant
            noteIndicatorColor = tertiaryColor


        }

        "skip" -> {
            buttonAction = listOf(unSkipHabit, { isDialogVisible.value = true })
            bgColor = tertiaryColor.copy(alpha = 0.5f)
            textColor = tertiaryColor
            noteIndicatorColor = tertiaryColor

        }
        "notneeded" -> {
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            textColor = primaryColor.copy(alpha = 0.5f)
            noteIndicatorColor = primaryColor.copy(alpha = 0.2f)
        }
        "failed" -> {
            bgColor = secondaryColor.copy(alpha = 0.6f)
            textColor = secondaryColor
            buttonAction = listOf(incrementHabit, { isDialogVisible.value = true })
            noteIndicatorColor = secondaryColor
        }
        else -> {
            bgColor = MaterialTheme.colorScheme.error
            noteIndicatorColor = MaterialTheme.colorScheme.onError

        }


    }

    val modifier = if (interactive) {
        Modifier.combinedClickable(onClick = {
            buttonAction[0]()
            println("$state This is state")
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
                    .size(15.dp)
                    .padding(3.dp),

                shadowElevation = 100.dp,
                color = noteIndicatorColor
            ) {}

        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            
            // Show icon based on state - but NOT for negative habits showing "failed" (X doesn't make sense)
            when (state) {
                "notneeded" -> {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "not needed",
                        tint = textColor.copy(alpha = 0.5f)
                    )
                }
                "skip" -> {
                    Icon(
                        Icons.Default.DoubleArrow,
                        contentDescription = "skipped",
                        tint = textColor
                    )
                }
                "failed" -> {
                    // Only show X for positive habits (partial) - negative habits should show color only
                    if (habitEntity?.isNegative != true) {
                        Text(
                            "X",
                            color = textColor,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
                else -> {
                    if (showDate) {
                        Text(date.dayOfMonth.toString(), color = textColor)
                    }
                }
            }
        }
    }
}
