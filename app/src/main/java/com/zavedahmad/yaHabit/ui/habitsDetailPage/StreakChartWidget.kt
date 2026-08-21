package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity

@Composable
fun StreakChartWidget(habitAllData: List<HabitCompletionEntity>?, habitEntity: HabitEntity) {
    val metrics = computeCycleMetrics(habitEntity, habitAllData)
    val habitColor = habitEntity.color

    Column(
        Modifier.fillMaxWidth().padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocalFireDepartment, "Current Streak", tint = habitColor)
            Spacer(Modifier.width(8.dp))
            Text("Current Streak: ", style = MaterialTheme.typography.titleMedium)
            Text(
                "${metrics.currentStreak} Days",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Stars, "Best Streak", tint = habitColor.copy(alpha = 0.7f))
            Spacer(Modifier.width(8.dp))
            Text("Best Streak: ", style = MaterialTheme.typography.titleMedium)
            Text(
                "${metrics.bestStreak} Days",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            if (habitEntity.isNegative) {
                "Clean days: ${metrics.metDays} of ${metrics.trackedDays} (${metrics.successRate}%)"
            } else {
                "Met goal on ${metrics.metDays} of ${metrics.trackedDays} logged days (${metrics.successRate}%)"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
