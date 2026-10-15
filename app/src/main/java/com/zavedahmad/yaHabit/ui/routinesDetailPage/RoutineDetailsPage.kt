package com.zavedahmad.yaHabit.ui.routinesDetailPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import com.zavedahmad.yaHabit.database.entities.RoutineStepCompletionEntity
import com.zavedahmad.yaHabit.database.entities.RoutineStepEntity
import java.time.LocalDate

private const val RECENT_DAYS = 14

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailsPage(viewModel: RoutineDetailsPageViewModel, backStack: NavBackStack) {
    val routine by viewModel.routine.collectAsStateWithLifecycle()
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val completions by viewModel.completions.collectAsStateWithLifecycle()
    val accent = routine?.color ?: MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(routine?.name ?: "Routine") },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(accent)
            )
            val description = routine?.description.orEmpty()
            if (description.isNotEmpty()) {
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val freq = routine?.frequency?.toInt()
            val cycle = routine?.cycle
            if (freq != null && cycle != null) {
                Text("$freq/$cycle per cycle", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            RoutineSummaryCard(steps = steps, completions = completions, accent = accent)

            Spacer(Modifier.height(4.dp))
            Text("Step completions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (steps.isEmpty()) {
                Text("No steps yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val byStep = remember(completions) { completions.groupBy { it.stepId } }
                steps.forEach { step ->
                    StepStatCard(
                        step = step,
                        completions = byStep[step.id].orEmpty(),
                        accent = accent
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RoutineSummaryCard(
    steps: List<RoutineStepEntity>,
    completions: List<RoutineStepCompletionEntity>,
    accent: Color
) {
    val metDates = remember(steps, completions) { routineMetDates(steps, completions) }
    val (current, best) = remember(metDates) { streaks(metDates, LocalDate.now()) }
    val days = remember { (0 until RECENT_DAYS).map { LocalDate.now().minusDays(it.toLong()) }.reversed() }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.outlinedCardColors()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBlock("Completed", metDates.size.toString(), "times", accent)
                StatBlock("Current streak", current.toString(), if (current == 1) "day" else "days", MaterialTheme.colorScheme.onSurface)
                StatBlock("Best streak", best.toString(), if (best == 1) "day" else "days", MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                days.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(if (date in metDates) accent else MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "routine counts as met on a day when every step is done or skipped • last $RECENT_DAYS days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String, unit: String, valueColor: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = valueColor, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(4.dp))
            Text(
                unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

/** Dates on which every one of the routine's current steps was done or skipped. */
private fun routineMetDates(
    steps: List<RoutineStepEntity>,
    completions: List<RoutineStepCompletionEntity>
): Set<LocalDate> {
    if (steps.isEmpty()) return emptySet()
    return completions.groupBy { it.completionDate }
        .filter { (_, entries) ->
            val byStep = entries.associateBy { it.stepId }
            steps.all { step ->
                val c = byStep[step.id]
                c?.isDone == true || c?.isSkip == true
            }
        }
        .keys
}

/** Returns (currentStreakEndingTodayOrYesterday, bestStreak). */
private fun streaks(metDates: Set<LocalDate>, today: LocalDate): Pair<Int, Int> {
    if (metDates.isEmpty()) return 0 to 0
    val sorted = metDates.sorted()
    var best = 1
    var run = 1
    for (i in 1 until sorted.size) {
        run = if (sorted[i] == sorted[i - 1].plusDays(1)) run + 1 else 1
        if (run > best) best = run
    }
    var current = 0
    var cursor = if (today in metDates) today else today.minusDays(1)
    while (cursor in metDates) {
        current++
        cursor = cursor.minusDays(1)
    }
    return current to best
}

@Composable
private fun StepStatCard(
    step: RoutineStepEntity,
    completions: List<RoutineStepCompletionEntity>,
    accent: Color
) {
    val doneCount = completions.count { it.isDone }
    val skipCount = completions.count { it.isSkip }
    val byDate = remember(completions) { completions.associateBy { it.completionDate } }
    val days = remember { (0 until RECENT_DAYS).map { LocalDate.now().minusDays(it.toLong()) }.reversed() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(step.name, style = MaterialTheme.typography.titleMedium)
                    if (step.description.isNotEmpty()) {
                        Text(step.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$doneCount", style = MaterialTheme.typography.headlineSmall, color = accent, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.size(4.dp))
                    Text(if (doneCount == 1) "time" else "times", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                days.forEach { date ->
                    val entry = byDate[date]
                    val cellColor = when {
                        entry?.isDone == true -> accent
                        entry?.isSkip == true -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(cellColor)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "last $RECENT_DAYS days" + if (skipCount > 0) " • $skipCount skipped" else "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
