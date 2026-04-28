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
import com.zavedahmad.yaHabit.database.entities.isAbsolute
import com.zavedahmad.yaHabit.database.entities.isCompleted
import com.zavedahmad.yaHabit.database.entities.isNotNeeded
import com.zavedahmad.yaHabit.database.entities.isOnlyNote
import com.zavedahmad.yaHabit.database.entities.isPartial
import com.zavedahmad.yaHabit.database.entities.isSkip

@Composable
fun StatsSummaryCard(habitAllData: List<HabitCompletionEntity>?, habitEntity: HabitEntity) {
    val habitColor = habitEntity.color
    
    val totalDays by remember(habitAllData) { derivedStateOf { habitAllData?.size ?: 0 } }
    
    val completedDays by remember(habitAllData) { 
        derivedStateOf { 
            habitAllData?.count { habitEntity.isCompleted(it) && !it.isSkip() && !it.isNotNeeded() } ?: 0 
        } 
    }
    
    val partialDays by remember(habitAllData) { 
        derivedStateOf { 
            habitAllData?.count { it.isPartial() && !it.isSkip() } ?: 0 
        } 
    }
    
    val overageDays by remember(habitAllData) { 
        derivedStateOf { 
            habitAllData?.count { 
                !it.isSkip() && !it.isPartial() && !it.isNotNeeded() && 
                it.isAbsolute() && !habitEntity.isCompleted(it) 
            } ?: 0 
        } 
    }
    
    val skippedDays by remember(habitAllData) { 
        derivedStateOf { 
            habitAllData?.count { it.isSkip() } ?: 0 
        } 
    }
    
    val failedDays by remember(habitAllData) { 
        derivedStateOf { 
            if (habitEntity.isNegative) {
                habitAllData?.count { !habitEntity.isCompleted(it) && !it.isSkip() } ?: 0
            } else {
                0
            }
        } 
    }
    
    val completionRate by remember(habitAllData) { 
        derivedStateOf { 
            if (totalDays > 0) {
                (completedDays.toFloat() / totalDays * 100).toInt()
            } else 0
        } 
    }
    
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
            
            // Primary metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total Days",
                    value = "$totalDays",
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatItem(
                    label = if (habitEntity.isNegative) "Under Limit" else "Completed",
                    value = "$completedDays",
                    color = habitColor
                )
                StatItem(
                    label = "Completion",
                    value = "$completionRate%",
                    color = habitColor
                )
            }
            
            // Secondary metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = if (habitEntity.isNegative) "Over Limit" else "Partial",
                    value = "$partialDays",
                    color = habitColor.copy(alpha = 0.7f)
                )
                StatItem(
                    label = "Over Goal",
                    value = "$overageDays",
                    color = habitColor.copy(alpha = 0.85f)
                )
                StatItem(
                    label = "Skipped",
                    value = "$skippedDays",
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            // Failed metric for negative habits
            if (habitEntity.isNegative) {
                StatItem(
                    label = "Failed (Over Limit)",
                    value = "$failedDays",
                    color = Color(0xFFF44336)
                )
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
