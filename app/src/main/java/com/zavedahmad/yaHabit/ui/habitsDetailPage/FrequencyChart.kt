package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import java.time.Year
import java.time.YearMonth

@Composable
fun FrequencyChart(
    habitAllData: List<HabitCompletionEntity>?, 
    habitEntity: HabitEntity,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color
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

    val successColor = primaryColor
    val partialColor = primaryColor.copy(alpha = 0.5f)
    val skipColor = tertiaryColor

    val data by remember(currentYearData, allMonths) {
        derivedStateOf {
            allMonths.map { month ->
                val monthData = currentYearData.filter { it.completionDate.yearMonth == month && !it.isOnlyNote() }
                
                val successCount = monthData.count { habitEntity.isCompleted(it) && !it.isSkip() && !it.isNotNeeded() }
                val partialCount = monthData.count { !habitEntity.isNegative && it.isPartial() && !it.isSkip() }
                val skippedCount = monthData.count { it.isSkip() }

                Bars(
                    label = month.month.name.slice(0..2), values = listOf(
                        Bars.Data(value = successCount.toDouble(), color = SolidColor(successColor)),
                        Bars.Data(value = partialCount.toDouble(), color = SolidColor(partialColor)),
                        Bars.Data(value = skippedCount.toDouble(), color = SolidColor(skipColor))
                    )
                )
            }
        }
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { yearToShow.value = yearToShow.value.minusYears(1) }) {
            Icon(
                Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = "",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
            )
        }
        Text(yearToShow.value.toString(), fontSize = 20.sp)
        IconButton(onClick = { yearToShow.value = yearToShow.value.plusYears(1) }) {
            Icon(
                Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
            )
        }
    }

    ColumnChart(
        modifier = Modifier
            .height(250.dp)
            .fillMaxWidth(),
        data = data,
        labelProperties = LabelProperties(
            enabled = true
        ),
        indicatorProperties = HorizontalIndicatorProperties(
            enabled = true,
            textStyle = TextStyle(fontSize = 10.sp)
        ),
        labelHelperProperties = LabelHelperProperties(enabled = false)
    )
}
