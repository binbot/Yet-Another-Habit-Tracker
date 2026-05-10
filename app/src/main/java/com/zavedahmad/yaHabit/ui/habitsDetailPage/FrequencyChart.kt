package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.core.yearMonth
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.isCompleted
import com.zavedahmad.yaHabit.database.entities.isOnlyNote
import com.zavedahmad.yaHabit.database.entities.isPartial
import com.zavedahmad.yaHabit.database.entities.isSkip
import com.zavedahmad.yaHabit.database.entities.isNotNeeded
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import java.time.Year
import java.time.YearMonth

@Composable
fun FrequencyChart(
    habitAllData: List<HabitCompletionEntity>?, 
    habitEntity: HabitEntity
) {
    val yearToShow = remember { mutableStateOf(Year.now()) }
    val currentYearData by remember(habitAllData, yearToShow.value) {
        derivedStateOf {
            habitAllData?.filter { Year.from(it.completionDate) == yearToShow.value } ?: emptyList()
        }
    }

    val allMonths by remember(yearToShow.value) {
        derivedStateOf {
            (1..12).map { i ->
                YearMonth.of(
                    yearToShow.value.value,
                    i
                )
            }
        }
    }

    val successColor = MaterialTheme.colorScheme.primary
    val failColor = MaterialTheme.colorScheme.secondary
    val skipColor = MaterialTheme.colorScheme.tertiary

    val data by remember(currentYearData, allMonths) {
        derivedStateOf {
            allMonths.map { month ->
                val monthData = currentYearData.filter { it.completionDate.yearMonth == month && !it.isOnlyNote() }
                
                val successCount = monthData.count { habitEntity.isCompleted(it) && !it.isSkip() && !it.isNotNeeded() }
                val failCount = monthData.count { !habitEntity.isCompleted(it) && !it.isSkip() && !it.isNotNeeded() && (it.repetitionsOnThisDay > 0 || it.isPartial()) }
                val skippedCount = monthData.count { it.isSkip() }

                Bars(
                    label = month.month.name.slice(0..2), values = listOf(
                        Bars.Data(value = successCount.toDouble(), color = SolidColor(successColor)),
                        Bars.Data(value = failCount.toDouble(), color = SolidColor(failColor)),
                        Bars.Data(value = skippedCount.toDouble(), color = SolidColor(skipColor))
                    )
                )
            }
        }
    }

    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        // Legend
        Row(
            Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(if (habitEntity.isNegative) "Success" else "Goal Met", successColor)
            Spacer(Modifier.width(12.dp))
            LegendItem(if (habitEntity.isNegative) "Failure" else "Partial", failColor)
            Spacer(Modifier.width(12.dp))
            LegendItem("Skip", skipColor)
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { yearToShow.value = yearToShow.value.minusYears(1) }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "")
            }
            Text(yearToShow.value.toString(), style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { yearToShow.value = yearToShow.value.plusYears(1) }) {
                Icon(Icons.AutoMirrored.Default.ArrowForwardIos, "", modifier = Modifier.size(16.dp))
            }
        }

        ColumnChart(
            modifier = Modifier
                .height(240.dp)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            data = data,
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
            ),
            indicatorProperties = HorizontalIndicatorProperties(
                enabled = true,
                textStyle = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface),
                contentBuilder = { value -> value.toInt().toString() }
            ),
            labelHelperProperties = LabelHelperProperties(enabled = false)
        )
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, shape = CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}
