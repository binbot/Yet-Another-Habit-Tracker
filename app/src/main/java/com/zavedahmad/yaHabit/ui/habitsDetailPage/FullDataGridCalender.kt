package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HeatMapCalendar
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapWeekHeaderPosition
import com.kizitonwose.calendar.compose.heatmapcalendar.rememberHeatMapCalendarState
import com.kizitonwose.calendar.core.yearMonth
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.hasNote
import com.zavedahmad.yaHabit.database.entities.isCompleted
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun FullDataGridCalender(
    incrementHabit: (date: LocalDate) -> Unit = {},
    deleteHabit: (date: LocalDate) -> Unit = {},
    initialMonthString: String? = null,
    habitData: List<HabitCompletionEntity>? = null, 
    gridHeight: Int = 190,
    showDate: Boolean = false,
    interactive: Boolean = false,
    firstDayOfWeek: DayOfWeek,
    skipHabit: (date: LocalDate) -> Unit,
    unSkipHabit: (date: LocalDate) -> Unit,
    habitEntity: HabitEntity? = null,
    primaryColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
    secondaryColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
    tertiaryColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.MaterialTheme.colorScheme.tertiary,
    dialogueComposable: @Composable (Boolean, () -> Unit, HabitCompletionEntity?, LocalDate) -> Unit
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val dateToday = LocalDate.now()

    val calendarState = rememberHeatMapCalendarState(
        startMonth = startMonth,
        endMonth = currentMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek,
    )
    
    LaunchedEffect(calendarState.firstVisibleMonth) {
        calendarState.startMonth = calendarState.firstVisibleMonth.yearMonth.minusMonths(12)
        if (calendarState.firstVisibleMonth.yearMonth < YearMonth.now().minusMonths(12)) {
            calendarState.endMonth = calendarState.firstVisibleMonth.yearMonth.plusMonths(12)
        } else {
            calendarState.endMonth = YearMonth.now()
        }
    }
    
    if (habitData != null) {
        Column {
            HeatMapCalendar(
                state = calendarState,
                modifier = Modifier
                    .height(gridHeight.dp)
                    .fillMaxWidth(),
                weekHeaderPosition = HeatMapWeekHeaderPosition.End,
                weekHeader = { weekDay ->
                    Row(
                        Modifier.height((gridHeight / 8).dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = weekDay.name.slice(0..2),
                            fontSize = 15.sp
                        )
                    }
                },
                monthHeader = { month ->
                    val monthName = month.yearMonth.month.toString().slice(0..2)
                    if (LocalDate.now().yearMonth != month.yearMonth) {
                        Column(
                            Modifier.height((gridHeight / 8).dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(text = monthName, fontSize = 15.sp, textAlign = TextAlign.Start)
                        }
                    } else if (LocalDate.now().dayOfMonth > 15) {
                        Column(
                            Modifier.height((gridHeight / 8).dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(text = monthName, fontSize = 15.sp, textAlign = TextAlign.Start)
                        }
                    } else {
                        Spacer(Modifier.height((gridHeight / 8).dp))
                    }
                },
                dayContent = { day, heatMapWeek ->
                    var dayState = ""
                    var hasNote = false
                    val datesMatching = habitData.filter { it.completionDate == day.date }
                    var habitCompletionEntity: HabitCompletionEntity? = null
                    val isFuture = day.date > dateToday
                    val suffix = if (isFuture) "Disabled" else ""
                    
                    if (datesMatching.size == 1) {
                        habitCompletionEntity = datesMatching[0]
                        hasNote = habitCompletionEntity.hasNote()
                        
                        val isCompleted = habitEntity?.isCompleted(habitCompletionEntity) ?: (habitCompletionEntity.repetitionsOnThisDay > 0)

                        if (habitEntity?.isNegative == true) {
                            dayState = if (isCompleted) "absolute" else "failed"
                        } else {
                            dayState = if (isCompleted) {
                                if (habitEntity != null && habitCompletionEntity.repetitionsOnThisDay > habitEntity.repetitionPerDay) "absoluteMore" else "absolute"
                            } else {
                                "partial"
                            }
                        }
                        dayState += suffix
                    } else {
                        dayState = if (isFuture) "incompleteDisabled" else {
                            if (habitEntity?.isNegative == true) "absolute" else "incomplete"
                        }
                    }
                    
                    val shouldShow = (dayState != "incompleteDisabled" && dayState != "absoluteDisabled" && dayState != "partialDisabled") || 
                                     heatMapWeek.days.any { it.date == dateToday }

                    if (shouldShow) {
                        Box(
                            Modifier.height((gridHeight / 8).dp).aspectRatio(1f)
                        ) {
                            Box(Modifier.padding((gridHeight / 80).dp)) {
                                GridDayItem(
                                    hasNote = hasNote,
                                    state = dayState,
                                    repetitionsOnThisDay = habitCompletionEntity?.repetitionsOnThisDay ?: 0.0,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    tertiaryColor = tertiaryColor,
                                    incrementHabit = { 
                                        if (dayState == "failed" || dayState == "skip") {
                                            deleteHabit(day.date)
                                        } else if (dayState == "absolute" || dayState == "absoluteMore") {
                                            skipHabit(day.date)
                                        } else {
                                            incrementHabit(day.date)
                                        }
                                    },
                                    deleteHabit = { deleteHabit(day.date) },
                                    date = day.date,
                                    showDate = showDate,
                                    interactive = !isFuture,
                                    dialogueComposable = { visible, onDismiss ->
                                        dialogueComposable(visible, onDismiss, habitCompletionEntity, day.date)
                                    },
                                    skipHabit = { skipHabit(day.date) },
                                    unSkipHabit = { unSkipHabit(day.date) },
                                    habitEntity = habitEntity
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}
