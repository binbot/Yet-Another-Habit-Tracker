package com.zavedahmad.yaHabit.ui.mainPage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import com.zavedahmad.yaHabit.database.entities.isSkip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavBackStack
import com.zavedahmad.yaHabit.Screen
import com.zavedahmad.yaHabit.database.entities.DayState
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.RoutineEntity
import com.zavedahmad.yaHabit.database.entities.resolveDayState
import com.zavedahmad.yaHabit.ui.theme.LocalOutlineSizes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun RoutineItemReorderable(
    backStack: NavBackStack,
    viewModel: MainPageViewModel,
    routine: RoutineEntity,
    isReorderableMode: Boolean = false
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val isArchived = routine.isArchived
    val alphaValue = if (isArchived) 0.5f else 1f

    val routineItems by viewModel.routineRepository.getItemsForRoutineFlow(routine.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val habits = viewModel.habits.collectAsStateWithLifecycle().value
    val habitMap = remember(habits) { habits.associateBy { it.id } }

    // For progress: count met today
    val today = LocalDate.now()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        colors = CardDefaults.outlinedCardColors(),
        border = BorderStroke(width = LocalOutlineSizes.current.small, color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f)),
        onClick = {
            if (!isReorderableMode) expanded = !expanded
        }
    ) {
        // Color accent bar to make routine color visible in list (parity with habit color theming)
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth().height(4.dp).background(routine.color)
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        routine.name,
                        maxLines = 1,
                        style = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.alpha(alphaValue)
                    )
                    if (routine.description.isNotEmpty()) {
                        Text(routine.description, maxLines = 1, style = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp), modifier = Modifier.alpha(alphaValue))
                    }
                    Text("${routineItems.size} steps • ${routine.frequency.toInt()}/${routine.cycle} per cycle", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isReorderableMode) {
                        // Routine-level skip — marks all steps for today as skipped to help partially completed stats
                        val allSkipped = remember(routineItems, habits) {
                            routineItems.isNotEmpty() && routineItems.all { item ->
                                val h = habitMap[item.habitId]
                                if (h == null) false else {
                                    // check via synchronous? For header we approximate not skipped; detailed check per row handles
                                    false
                                }
                            }
                        }
                        IconButton(onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                val todayS = LocalDate.now()
                                // Determine if routine today is already all skipped
                                val items = viewModel.routineRepository.getItemsForRoutine(routine.id)
                                val allAreSkipped = items.all { ri ->
                                    viewModel.habitRepository.getAllHabitCompletionsById(ri.habitId)?.firstOrNull { it.completionDate == todayS }?.isSkip() == true
                                }
                                items.forEach { ri ->
                                    viewModel.habitRepository.setSkip(todayS, ri.habitId, !allAreSkipped)
                                }
                            }
                        }) {
                            Icon(Icons.Default.DoubleArrow, contentDescription = "skip routine", tint = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "expand")
                    }
                }
            }
            AnimatedVisibility(visible = expanded && !isReorderableMode) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    HorizontalDivider()
                    if (routineItems.isEmpty()) {
                        Text("No habits added — edit routine to add steps", style = MaterialTheme.typography.bodySmall)
                    } else {
                        routineItems.forEach { item ->
                            val habit: HabitEntity? = habitMap[item.habitId]
                            if (habit != null) {
                                RoutineStepRow(
                                    habit = habit,
                                    viewModel = viewModel,
                                    today = today
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            androidx.compose.material3.TextButton(onClick = { backStack.add(Screen.AddRoutinePageRoute(routineId = routine.id)) }) {
                                Text("Edit")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineStepRow(habit: HabitEntity, viewModel: MainPageViewModel, today: LocalDate) {
    val completions by viewModel.habitRepository.getAllHabitCompletionsByIdFlow(habit.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val entry = completions?.firstOrNull { it.completionDate == today }
    val state = resolveDayState(habit, entry, today, today)
    val checked = state == DayState.Absolute || state == DayState.AbsoluteMore || state == DayState.NegativeCount
    val isSkipped = state == DayState.Skip
    val coroutineScope = rememberCoroutineScope()
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(habit.name, style = MaterialTheme.typography.bodyMedium)
            Text("${habit.repetitionPerDay} ${habit.measurementUnit}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    viewModel.habitRepository.setSkip(today, habit.id, !isSkipped)
                }
            }) {
                Icon(Icons.Default.DoubleArrow, contentDescription = if (isSkipped) "unskip" else "skip", tint = if (isSkipped) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Checkbox(
                checked = checked,
                onCheckedChange = {
                    coroutineScope.launch(Dispatchers.IO) {
                        if (checked) viewModel.habitRepository.decrementRepetitions(today, habit.id)
                        else viewModel.habitRepository.incrementRepetitions(today, habit.id)
                    }
                }
            )
        }
    }
}
