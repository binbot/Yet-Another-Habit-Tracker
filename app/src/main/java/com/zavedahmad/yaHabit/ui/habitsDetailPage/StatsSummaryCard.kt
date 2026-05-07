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
    val habitColor = habitEntity.color
    val primaryColor = MaterialTheme.colorScheme.primary
    
    // For positive habits, these are "failures". For negative habits, these are "successes".
    // Let's call them "Target Met" and "Target Not Met" internally.
    val data = habitAllData ?: emptyList()
    
    val metTargetDays = data.filter { habitEntity.isCompleted(it) && !it.isSkip() }
    val missedTargetDays = data.filter { !habitEntity.isCompleted(it) && !it.isSkip() }

    val avgRepsOnMet = if (metTargetDays.isNotEmpty()) metTargetDays.map { it.repetitionsOnThisDay }.average() else 0.0
    val maxRepsOnMet = if (metTargetDays.isNotEmpty()) metTargetDays.maxOf { it.repetitionsOnThisDay } else 0.0
    val minRepsOnMet = if (metTargetDays.isNotEmpty()) metTargetDays.minOf { it.repetitionsOnThisDay } else 0.0

    val avgRepsOnMissed = if (missedTargetDays.isNotEmpty()) missedTargetDays.map { it.repetitionsOnThisDay }.average() else 0.0
    val maxRepsOnMissed = if (missedTargetDays.isNotEmpty()) missedTargetDays.maxOf { it.repetitionsOnThisDay } else 0.0
    val minRepsOnMissed = if (missedTargetDays.isNotEmpty()) missedTargetDays.minOf { it.repetitionsOnThisDay } else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Summary Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Section 1: Target Met (using full primary color)
            Text(
                text = if (habitEntity.isNegative) "Under Limit (Success)" else "Goal Met (Success)",
                fontSize = 12.sp,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Min", value = String.format(Locale.US, "%.1f", minRepsOnMet), color = primaryColor)
                StatItem(label = "Max", value = String.format(Locale.US, "%.1f", maxRepsOnMet), color = primaryColor)
                StatItem(label = "Avg", value = String.format(Locale.US, "%.1f", avgRepsOnMet), color = primaryColor)
            }

            // Section 2: Target Not Met (using secondary variation of habit color)
            if (missedTargetDays.isNotEmpty()) {
                Text(
                    text = if (habitEntity.isNegative) "Over Limit (Failures)" else "Incomplete (Partial)",
                    fontSize = 12.sp,
                    color = primaryColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Min",
                        value = String.format(Locale.US, "%.1f", minRepsOnMissed),
                        color = primaryColor.copy(alpha = 0.7f)
                    )
                    StatItem(
                        label = "Max",
                        value = String.format(Locale.US, "%.1f", maxRepsOnMissed),
                        color = primaryColor.copy(alpha = 0.7f)
                    )
                    StatItem(
                        label = "Avg",
                        value = String.format(Locale.US, "%.1f", avgRepsOnMissed),
                        color = primaryColor.copy(alpha = 0.7f)
                    )
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
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
