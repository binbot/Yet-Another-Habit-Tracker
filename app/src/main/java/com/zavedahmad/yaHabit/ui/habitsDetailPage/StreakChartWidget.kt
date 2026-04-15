package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.isPartial
import com.zavedahmad.yaHabit.database.entities.isSkip
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import com.zavedahmad.yaHabit.database.entities.isCompleted

@Composable
fun StreakChartWidget(habitAllData: List<HabitCompletionEntity>?, habitEntity: HabitEntity) {
    if (habitAllData != null) {
        val completedDates = habitAllData
            .filter { habitEntity.isCompleted(it) }
            .map { it.completionDate }
            .distinct()
            .sortedDescending()

        var currentStreak = 0
        if (completedDates.isNotEmpty()) {
            var checkDate = LocalDate.now()
            // If today isn't logged, check yesterday to see if the streak is still 'active'
            if (!completedDates.contains(checkDate)) {
                checkDate = checkDate.minusDays(1)
            }

            for (date in completedDates) {
                if (completedDates.contains(checkDate)) {
                    currentStreak++
                    checkDate = checkDate.minusDays(1)
                } else {
                    break
                }
            }
        }

        // Calculate Best Streak
        val sortedDates = completedDates.sorted()
        var bestStreak = 0
        var tempStreak = 0
        var lastDate: LocalDate? = null

        for (date in sortedDates) {
            if (lastDate != null && date == lastDate.plusDays(1)) {
                tempStreak++
            } else {
                tempStreak = 1
            }
            if (tempStreak > bestStreak) bestStreak = tempStreak
            lastDate = date
        }

        Column(Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalFireDepartment, "Current Streak", tint = Color(0xFFFF9800))
                Spacer(Modifier.width(8.dp))
                Text("Current Streak: ", style = MaterialTheme.typography.titleMedium)
                Text("$currentStreak Days", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Stars, "Best Streak", tint = Color(0xFFFFD700))
                Spacer(Modifier.width(8.dp))
                Text("Best Streak: ", style = MaterialTheme.typography.titleMedium)
                Text("$bestStreak Days", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }
}