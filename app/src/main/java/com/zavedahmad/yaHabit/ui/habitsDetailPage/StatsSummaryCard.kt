package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    
    // "Days Tracked" = days with any entry (not days since creation)
    val daysTracked by remember(habitAllData) { 
        derivedStateOf { habitAllData?.size ?: 0 } 
    }
    
    // Completed days
    val completedDays by remember(habitAllData) { 
        derivedStateOf { 
            habitAllData?.count { habitEntity.isCompleted(it) && !it.isSkip() } ?: 0 
        } 
    }
    
    // Completion rate (completed / tracked)
    val completionRate by remember(habitAllData) { 
        derivedStateOf { 
            if (daysTracked > 0) {
                (completedDays.toFloat() / daysTracked * 100).toInt()
            } else 0
        } 
    }
    
    // Failed days data (for negative habits: days over limit, for positive: partial days)
    val failedDaysData by remember(habitAllData) {
        derivedStateOf {
            habitAllData?.filter { 
                if (habitEntity.isNegative) {
                    !habitEntity.isCompleted(it) && !it.isSkip()
                } else {
                    !habitEntity.isCompleted(it) && !it.isSkip() && !it.isNotNeeded
                }
            } ?: emptyList()
        }
    }
    val failedDaysCount = failedDaysData.size
    
    // Repetition stats for failed days (min, max, avg)
    val avgRepsOnFailed = if (failedDaysCount > 0) {
        failedDaysData.map { it.repetitionsOnThisDay }.average()
    } else 0.0
    val maxRepsOnFailed = failedDaysData.maxOfOrNull { it.repetitionsOnThisDay } ?: 0.0
    val minRepsOnFailed = failedDaysData.minOfOrNull { it.repetitionsOnThisDay } ?: 0.0
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = habitColor.copy(alpha = 0.1f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Summary Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = habitColor
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Row 1: Days Tracked + Completion Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Days Tracked",
                    value = "$daysTracked",
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatItem(
                    label = "Completion Rate",
                    value = "$completionRate%",
                    color = habitColor
                )
            }
            
            // Row 2: Completed days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = if (habitEntity.isNegative) "Under Limit" else "Completed",
                    value = "$completedDays",
                    color = habitColor
                )
            }
            
            // Row 3: Failed days details (only if there are failures)
            if (failedDaysCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (habitEntity.isNegative) "Over Limit Days:" else "Partial Days:",
                    style = MaterialTheme.typography.bodySmall,
                    color = habitColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Min",
                        value = "${minRepsOnFailed.toInt()}",
                        color = habitColor.copy(alpha = 0.6f)
                    )
                    StatItem(
                        label = "Max",
                        value = "${maxRepsOnFailed.toInt()}",
                        color = habitColor.copy(alpha = 0.9f)
                    )
                    StatItem(
                        label = "Avg",
                        value = "${String.format(Locale.US, "%.1f", avgRepsOnFailed)}",
                        color = habitColor.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
