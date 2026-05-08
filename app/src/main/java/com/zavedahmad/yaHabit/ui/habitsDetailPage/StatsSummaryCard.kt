package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.isCompleted
import com.zavedahmad.yaHabit.database.entities.isSkip
import java.util.Locale

@Composable
fun StatsSummaryCard(habitAllData: List<HabitCompletionEntity>?, habitEntity: HabitEntity) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    val data = habitAllData ?: emptyList()
    val activeDays = data.filter { !it.isSkip() }
    val totalDaysCount = activeDays.size
    
    val metTargetDays = activeDays.filter { habitEntity.isCompleted(it) }
    val missedTargetDays = activeDays.filter { !habitEntity.isCompleted(it) }
    
    val completionRate = if (totalDaysCount > 0) (metTargetDays.size.toDouble() / totalDaysCount.toDouble() * 100).toInt() else 0
    val totalVolume = activeDays.sumOf { it.repetitionsOnThisDay }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Summary Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$completionRate% Score",
                    style = MaterialTheme.typography.titleMedium,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "Across $totalDaysCount active days • Total volume: ${String.format(Locale.US, "%.1f", totalVolume)} ${habitEntity.measurementUnit}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Section 1: Success (using full primary color)
            Text(
                text = if (habitEntity.isNegative) "Success (Under Limit)" else "Success (Goal Met)",
                fontSize = 12.sp,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val metValues = metTargetDays.map { it.repetitionsOnThisDay }
                StatItem(label = "Days", value = "${metTargetDays.size}", color = primaryColor)
                StatItem(label = "Avg Reps", value = if (metValues.isNotEmpty()) String.format(Locale.US, "%.1f", metValues.average()) else "-", color = primaryColor)
                StatItem(label = "Best Day", value = if (metValues.isNotEmpty()) String.format(Locale.US, "%.1f", if (habitEntity.isNegative) metValues.minOrNull() else metValues.maxOrNull()) else "-", color = primaryColor)
            }

            // Section 2: Partial/Failure (using secondary variation)
            if (missedTargetDays.isNotEmpty()) {
                Text(
                    text = if (habitEntity.isNegative) "Relapses (Over Limit)" else "Partial Progress",
                    fontSize = 12.sp,
                    color = secondaryColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val missedValues = missedTargetDays.map { it.repetitionsOnThisDay }
                    StatItem(label = "Days", value = "${missedTargetDays.size}", color = secondaryColor)
                    StatItem(label = "Avg Reps", value = if (missedValues.isNotEmpty()) String.format(Locale.US, "%.1f", missedValues.average()) else "-", color = secondaryColor)
                    StatItem(label = "Worst Overage", value = if (missedValues.isNotEmpty()) String.format(Locale.US, "%.1f", missedValues.maxOrNull()) else "-", color = secondaryColor)
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
