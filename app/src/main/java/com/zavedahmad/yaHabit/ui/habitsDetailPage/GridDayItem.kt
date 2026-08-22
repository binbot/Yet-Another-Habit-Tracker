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
import com.zavedahmad.yaHabit.database.entities.DayState
import java.time.LocalDate

private val FailedRed = Color(0xFFF44336)

/**
 * Heat-map cell colours. Met days shade by [intensity] (reps / target),
 * so heavier days read darker - GitHub-contribution style.
 */
private data class GridVisuals(val bg: Color, val noteDot: Color, val text: Color)

private fun resolveGridVisuals(state: DayState, cs: ColorScheme, intensity: Float): GridVisuals {
    val bg = when (state) {
        DayState.Absolute, DayState.AbsoluteMore ->
            cs.primary.copy(alpha = 0.45f + 0.55f * intensity.coerceIn(0f, 1f))
        DayState.Partial ->
            cs.primary.copy(alpha = 0.12f + 0.28f * intensity.coerceIn(0f, 1f))
        DayState.NegativeCount ->
            cs.primary.copy(alpha = 0.60f)
        DayState.Failed -> FailedRed.copy(alpha = 0.90f)
        DayState.FailedDisabled -> FailedRed.copy(alpha = 0.25f)
        DayState.Skip -> cs.tertiaryContainer
        DayState.NotNeeded -> cs.surfaceVariant.copy(alpha = 0.5f)
        DayState.Note -> cs.secondaryContainer
        DayState.Incomplete -> cs.surfaceVariant.copy(alpha = 0.6f)
        DayState.IncompleteDisabled -> cs.inverseSurface.copy(alpha = 0.05f)
        DayState.AbsoluteDisabled -> cs.inverseSurface.copy(alpha = 0.12f)
        DayState.AbsoluteMoreDisabled -> cs.inverseSurface.copy(alpha = 0.10f)
        DayState.PartialDisabled, DayState.NegativeCountDisabled ->
            cs.inverseSurface.copy(alpha = 0.08f)
        DayState.NotNeededDisabled -> cs.surfaceVariant.copy(alpha = 0.25f)
        DayState.NoteDisabled -> cs.secondaryContainer.copy(alpha = 0.3f)
        DayState.SkipDisabled -> cs.tertiaryContainer.copy(alpha = 0.3f)
        DayState.Error -> cs.error
    }
    val text = if (bg.luminance() > 0.5f) Color(0xFF1C1B1F) else Color.White
    return GridVisuals(bg, noteDot = if (bg.luminance() > 0.5f) cs.tertiary else cs.tertiaryContainer, text = text)
}

@Composable
fun GridDayItem(
    state: DayState,
    intensity: Float = 1f,
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
    val visuals = remember(state, cs, intensity) { resolveGridVisuals(state, cs, intensity) }

    val primaryAction: () -> Unit = when {
        state == DayState.Skip -> unSkipHabit
        state.isDisabled || state == DayState.Error -> ({})
        else -> incrementHabit
    }

    val modifier = if (interactive) {
        Modifier.combinedClickable(
            onClick = primaryAction,
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
