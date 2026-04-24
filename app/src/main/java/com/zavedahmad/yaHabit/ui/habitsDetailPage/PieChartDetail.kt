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
import com.zavedahmad.yaHabit.database.entities.hasNote
import com.zavedahmad.yaHabit.database.entities.isAbsolute
import com.zavedahmad.yaHabit.database.entities.isCompleted
import com.zavedahmad.yaHabit.database.entities.isNotNeeded
import com.zavedahmad.yaHabit.database.entities.isPartial
import com.zavedahmad.yaHabit.database.entities.isSkip
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie

@Composable
fun PieChartDetail(habitAllData : List<HabitCompletionEntity>?, habitEntity: HabitEntity) {
    val numberOfSuccess by remember(habitAllData) { derivedStateOf { (habitAllData?.filter { habitEntity.isCompleted(it) && !it.isSkip() && !it.isNotNeeded() }?.size ?: 0) } }
    val numberOfPartial by remember(habitAllData) { derivedStateOf { (habitAllData?.filter { it.isPartial() && !it.isSkip() }?.size ?: 0) } }
    val numberOfOverage by remember(habitAllData) { derivedStateOf { (habitAllData?.filter { !it.isSkip() && !it.isPartial() && !it.isNotNeeded() && it.isAbsolute() && !habitEntity.isCompleted(it) }?.size ?: 0) } }
    val numberOfSkips by remember(habitAllData) { derivedStateOf { habitAllData?.filter { it.isSkip() }?.size ?: 0 } }

    val habitColor = habitEntity.color
    val colorSuccess = habitColor
    val colorPartial = habitColor.copy(alpha = 0.5f)
    val colorOverage = habitColor.copy(alpha = 0.75f)
    val colorSkip = MaterialTheme.colorScheme.outline

    val data = remember(habitAllData) {
        mutableStateOf(
            listOf(
                Pie(label = if (habitEntity.isNegative) "Failed" else "Complete", data = numberOfSuccess.toDouble(), color = colorSuccess, selectedColor = Color.Green),
                Pie(label = if (habitEntity.isNegative) "Success" else "Partial", data = numberOfPartial.toDouble(), color = colorPartial, selectedColor = Color.Red),
                Pie(label = "Over Goal", data = numberOfOverage.toDouble(), color = colorOverage, selectedColor = Color.Yellow),
                Pie(label = "Skipped", data = numberOfSkips.toDouble(), color = colorSkip, selectedColor = Color.Blue),
            )
        )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(if (habitEntity.isNegative) "Under Limit: $numberOfSuccess" else "Completed: $numberOfSuccess", color = colorSuccess)
            Text(if (habitEntity.isNegative) "Over Limit: $numberOfPartial" else "Partial: $numberOfPartial", color = colorPartial)
            Text("Over Goal: $numberOfOverage", color = colorOverage)
            Text("Skipped: $numberOfSkips", color = colorSkip)
        }
        Surface (Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.onSurface, shape = CircleShape)){
            Box(Modifier.padding(8.dp)){
    PieChart(
        modifier = Modifier.size(200.dp),
        data = data.value,
        style = Pie.Style.Stroke( 30.dp)
    )}}}
}
