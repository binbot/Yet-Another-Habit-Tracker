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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.isCompleted
import com.zavedahmad.yaHabit.database.entities.isNotNeeded
import com.zavedahmad.yaHabit.database.entities.isSkip
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie

@Composable
fun PieChartDetail(
    habitAllData : List<HabitCompletionEntity>?, 
    habitEntity: HabitEntity,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color
) {
    // For positive habits: "Perfect" = exact goal, "Extra" = exceeded, "Partial" = some but not all
    // For negative habits: "Under Limit" = success, "Over Limit" = failure
    
    val totalLoggedDays = habitAllData?.filter { !it.isSkip() && !it.isNotNeeded() } ?: emptyList()

    val numberOfSuccess by remember(habitAllData) { derivedStateOf { 
        if (habitEntity.isNegative) {
            totalLoggedDays.count { habitEntity.isCompleted(it) }
        } else {
            totalLoggedDays.count { it.repetitionsOnThisDay == habitEntity.repetitionPerDay }
        }
    } }
    
    val numberOfOverGoal by remember(habitAllData) { derivedStateOf { 
        if (habitEntity.isNegative) {
            totalLoggedDays.count { !habitEntity.isCompleted(it) } // Exceeded limit
        } else {
            totalLoggedDays.count { it.repetitionsOnThisDay > habitEntity.repetitionPerDay }
        }
    } }

    val numberOfPartial by remember(habitAllData) { derivedStateOf { 
        if (habitEntity.isNegative) 0 
        else totalLoggedDays.count { it.repetitionsOnThisDay > 0 && it.repetitionsOnThisDay < habitEntity.repetitionPerDay }
    } }
    
    val numberOfSkips by remember(habitAllData) { derivedStateOf { 
        habitAllData?.count { it.isSkip() } ?: 0 
    } }
    
    val colorSuccess = primaryColor
    val colorOverGoal = secondaryColor
    val colorPartial = primaryColor.copy(alpha = 0.5f)
    val colorSkip = tertiaryColor
    
    val data = remember(habitAllData) {
        derivedStateOf {
            val list = mutableListOf<Pie>()
            if (numberOfSuccess > 0) list.add(Pie(label = if (habitEntity.isNegative) "Under Limit" else "Perfect", data = numberOfSuccess.toDouble(), color = colorSuccess, selectedColor = colorSuccess))
            if (numberOfOverGoal > 0) list.add(Pie(label = if (habitEntity.isNegative) "Over Limit" else "Extra", data = numberOfOverGoal.toDouble(), color = colorOverGoal, selectedColor = colorOverGoal))
            if (numberOfPartial > 0) list.add(Pie(label = "Partial", data = numberOfPartial.toDouble(), color = colorPartial, selectedColor = colorPartial))
            if (numberOfSkips > 0) list.add(Pie(label = "Skipped", data = numberOfSkips.toDouble(), color = colorSkip, selectedColor = colorSkip))
            list
        }
    }
    
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            if (habitEntity.isNegative) {
                Text("Under Limit: $numberOfSuccess", color = colorSuccess)
                Text("Over Limit: $numberOfOverGoal", color = colorOverGoal)
            } else {
                Text("Perfect: $numberOfSuccess", color = colorSuccess)
                Text("Extra: $numberOfOverGoal", color = colorOverGoal)
                Text("Partial: $numberOfPartial", color = colorPartial)
            }
            Text("Skipped: $numberOfSkips", color = colorSkip)
        }
        Surface (Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.onSurface, shape = CircleShape)){
            Box(Modifier.padding(8.dp)){
                PieChart(
                    modifier = Modifier.size(200.dp),
                    data = data.value,
                    style = Pie.Style.Stroke(30.dp)
                )
            }
        }
    }
}
