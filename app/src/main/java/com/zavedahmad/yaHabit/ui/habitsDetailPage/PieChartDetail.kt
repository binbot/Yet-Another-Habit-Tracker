package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.isPartial
import com.zavedahmad.yaHabit.database.entities.isSkip
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie

private val FailedRed = Color(0xFFF44336)

@Composable
fun PieChartDetail(habitAllData: List<HabitCompletionEntity>?, habitEntity: HabitEntity) {
    // Slice counts over logged entries only, sign-aware:
    // - positive: met = target reached; missed = logged under target
    // - negative: met = clean / within limit; missed = over limit
    val counts = remember(habitAllData, habitEntity.id) {
        var met = 0; var missed = 0; var pending = 0; var skipped = 0
        habitAllData?.forEach { entry ->
            when {
                entry.isSkip() -> skipped++
                entry.isPartial() -> pending++
                dayIsMet(habitEntity, entry) -> met++
                else -> missed++
            }
        }
        listOf(met, missed, pending, skipped)
    }

    val isNegative = habitEntity.isNegative
    val labelMet = if (isNegative) "Clean" else "Met"
    val labelMissed = if (isNegative) "Over Limit" else "Missed"
    val labelPending = if (isNegative) "Pending" else "Pending"

    val colorMet = habitEntity.color
    val colorMissed = FailedRed
    val colorPending = habitEntity.color.copy(alpha = 0.4f)
    val colorSkipped = MaterialTheme.colorScheme.outline

    val data = remember(counts, colorMet, colorSkipped) {
        listOf(
            Pie(label = labelMet, data = counts[0].toDouble(), color = colorMet, selectedColor = Color.Green),
            Pie(label = labelMissed, data = counts[1].toDouble(), color = colorMissed, selectedColor = Color.Red),
            Pie(label = labelPending, data = counts[2].toDouble(), color = colorPending, selectedColor = Color.Yellow),
            Pie(label = "Skipped", data = counts[3].toDouble(), color = colorSkipped, selectedColor = Color.Blue),
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("$labelMet: ${counts[0]}", color = colorMet)
            Text("$labelMissed: ${counts[1]}", color = colorMissed)
            Text("Pending: ${counts[2]}", color = colorPending)
            Text("Skipped: ${counts[3]}", color = colorSkipped)
        }
        Surface(
            Modifier.border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            )
        ) {
            Box(Modifier.padding(8.dp)) {
                PieChart(
                    modifier = Modifier.size(200.dp),
                    data = data,
                    style = Pie.Style.Stroke(30.dp)
                )
            }
        }
    }
}
