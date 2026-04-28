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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.core.yearMonth
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.isAbsolute
import com.zavedahmad.yaHabit.database.entities.isCompleted
import com.zavedahmad.yaHabit.database.entities.isNotNeeded
import com.zavedahmad.yaHabit.database.entities.isOnlyNote
import com.zavedahmad.yaHabit.database.entities.isPartial
import com.zavedahmad.yaHabit.database.entities.isSkip
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import java.time.Year
import java.time.YearMonth

@Composable
fun FrequencyChart(habitAllData: List<HabitCompletionEntity>?, habitEntity: HabitEntity) {
    val yearToShow = remember { mutableStateOf(Year.now()) }
    val currentYearData by remember(habitAllData) {
        derivedStateOf {
            habitAllData?.filter { Year.from(it.completionDate) == yearToShow.value } ?: emptyList()
        }
    }

    val allMonths by remember {
        derivedStateOf {
            (1..12).map { i ->
                YearMonth.of(
                    yearToShow.value.value,
                    i
                )
            }
        }
    }

    val habitColor = habitEntity.color
    val successColor = habitColor
    val overageColor = habitColor.copy(alpha = 0.75f)
    val partialColor = habitColor.copy(alpha = 0.5f)
    val skipColor = MaterialTheme.colorScheme.outline
    val notNeededColor = MaterialTheme.colorScheme.surfaceVariant

    val data by remember(habitAllData, yearToShow) {
        derivedStateOf {

                allMonths.map { month ->
                    val monthData = currentYearData.filter { it.completionDate.yearMonth == month && !it.isOnlyNote() }
                    
                    val completedCount = monthData.count { habitEntity.isCompleted(it) && !it.isSkip() && !it.isNotNeeded() }
                    val overageCount = monthData.count { 
                        !it.isSkip() && !it.isPartial() && !it.isNotNeeded() && 
                        it.isAbsolute() && !habitEntity.isCompleted(it) 
                    }
                    val partialCount = monthData.count { it.isPartial() && !it.isSkip() }
                    val skippedCount = monthData.count { it.isSkip() }
                    val notNeededCount = monthData.count { it.isNotNeeded() }

                    Bars(
                        label = month.month.name.slice(0..2), values = listOf(
                            Bars.Data(value = completedCount.toDouble(), color = SolidColor(successColor)),
                            Bars.Data(value = overageCount.toDouble(), color = SolidColor(overageColor)),
                            Bars.Data(value = partialCount.toDouble(), color = SolidColor(partialColor)),
                            Bars.Data(value = skippedCount.toDouble(), color = SolidColor(skipColor)),
                            Bars.Data(value = notNeededCount.toDouble(), color = SolidColor(notNeededColor))
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
                    Text(
                        label, style = TextStyle(fontSize = 10.sp)
                    )
                }

            ), labelHelperProperties = LabelHelperProperties(enabled = false)


        )


    }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(onClick = {
            yearToShow.value = yearToShow.value.minusYears(1)
        }) {
            Icon(
                Icons.Default.ArrowBackIosNew,
                contentDescription = "",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
            )
        }
        Text(
            text = yearToShow.value.toString(),
            Modifier.clickable(onClick = { yearToShow.value = Year.now() }),
        )
        Card(onClick = {
            yearToShow.value = yearToShow.value.plusYears(1)
        }) {
            Icon(
                Icons.AutoMirrored.Default.ArrowForwardIos,
                contentDescription = "",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
            )
        }
    }
}