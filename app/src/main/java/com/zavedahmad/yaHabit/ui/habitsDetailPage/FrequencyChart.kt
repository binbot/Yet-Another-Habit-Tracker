package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.animation.core.snap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.core.yearMonth
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import java.time.Year
import java.time.YearMonth

private val MissedRed = Color(0xFFF44336)

@Composable
fun FrequencyChart(habitAllData: List<HabitCompletionEntity>?, habitEntity: HabitEntity) {
    val yearToShow = remember { mutableStateOf(Year.now()) }

    val today = remember { java.time.LocalDate.now() }

    val data by remember(habitAllData, yearToShow, habitEntity.id) {
        derivedStateOf {
            // Enumerate actual calendar days per month so unlogged misses
            // count for strict schedules; NEUTRAL/SKIP/EXCUSED are excluded.
            val byDate = habitAllData?.associateBy { it.completionDate } ?: emptyMap()

            (1..12).map { i ->
                val month = YearMonth.of(yearToShow.value.value, i)
                var metCount = 0
                var missedCount = 0
                var day = month.atDay(1)
                val lastDay = month.atEndOfMonth()
                while (!day.isAfter(lastDay) && !day.isAfter(today)) {
                    when (classifyDay(habitEntity, byDate[day], day, today)) {
                        DayClass.MET -> metCount++
                        DayClass.MISSED, DayClass.OVER_LIMIT -> missedCount++
                        else -> {}
                    }
                    day = day.plusDays(1)
                }

                Bars(
                    label = month.month.name.slice(0..2),
                    values = listOf(
                        Bars.Data(value = metCount.toDouble(), color = SolidColor(habitEntity.color)),
                        Bars.Data(value = missedCount.toDouble(), color = SolidColor(MissedRed.copy(alpha = 0.5f)))
                    )
                )
            }
        }
    }

    Column {
        ColumnChart(
            modifier = Modifier
                .height(250.dp)
                .fillMaxWidth(),
            data = data,
            animationSpec = snap(),
            animationDelay = 0,
            animationMode = AnimationMode.Together(),
            indicatorProperties = HorizontalIndicatorProperties(
                enabled = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
            ),
            labelProperties = LabelProperties(
                enabled = true,
                builder = { modifier, label, shouldRotate, index ->
                    Text(label, style = TextStyle(fontSize = 10.sp))
                }
            ),
            labelHelperProperties = LabelHelperProperties(enabled = false)
        )
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(onClick = { yearToShow.value = yearToShow.value.minusYears(1) }) {
            Icon(
                Icons.Default.ArrowBackIosNew,
                contentDescription = "previous year",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
            )
        }
        Text(
            text = yearToShow.value.toString(),
            Modifier.clickable(onClick = { yearToShow.value = Year.now() }),
        )
        Card(onClick = { yearToShow.value = yearToShow.value.plusYears(1) }) {
            Icon(
                Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "next year",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
            )
        }
    }
}
