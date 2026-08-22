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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HeatMapCalendar
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapWeekHeaderPosition
import com.kizitonwose.calendar.compose.heatmapcalendar.rememberHeatMapCalendarState
import com.kizitonwose.calendar.core.yearMonth
import com.zavedahmad.yaHabit.database.entities.DayState
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.hasNote
import com.zavedahmad.yaHabit.database.entities.isPartial
import com.zavedahmad.yaHabit.database.entities.resolveDayState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun FullDataGridCalender(
    incrementHabit: (date: LocalDate) -> Unit = {},
    deleteHabit: (date: LocalDate) -> Unit = {},
    initialMonthString: String? = null,
    habitData: List<HabitCompletionEntity>? = null, gridHeight: Int = 190,
    showDate: Boolean = false,
    interactive: Boolean = false,
    firstDayOfWeek: DayOfWeek,
    skipHabit: (date: LocalDate) -> Unit,
    unSkipHabit: (date: LocalDate) -> Unit,
    habitEntity: HabitEntity? = null,
    dialogueComposable: @Composable (Boolean, () -> Unit, HabitCompletionEntity?, LocalDate) -> Unit
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = currentMonth.minusMonths(12)
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

    // One entry per date (real log preferred over partial placeholder),
    // plus a duplicate flag for the error state.
    val entriesByDate = remember(habitData) {
        val result = HashMap<LocalDate, Pair<HabitCompletionEntity?, Boolean>>()
        habitData?.groupBy { it.completionDate }?.forEach { (date, entries) ->
            val best = entries.firstOrNull { !it.isPartial() } ?: entries.first()
            result[date] = Pair(best, entries.size > 1)
        }
        result
    }

    if (habitData == null) return

    Column {
        HeatMapCalendar(
            weekHeaderPosition = HeatMapWeekHeaderPosition.End,
            weekHeader = { weekDay ->
                Row(
                    Modifier.height((gridHeight / 8).dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Spacer(Modifier.width(5.dp))
                    Text(weekDay.name.slice(0..2), fontSize = 15.sp)
                }
            },
            monthHeader = {
                if (LocalDate.now().yearMonth != it.yearMonth) {
                    Column(
                        Modifier.height((gridHeight / 8).dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            it.yearMonth.month.toString().slice(0..2),
                            fontSize = 15.sp, textAlign = TextAlign.Start
                        )
                    }
                } else {
                    if (LocalDate.now().dayOfMonth > 15) {
                        Column(
                            Modifier.height((gridHeight / 8).dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                it.yearMonth.month.toString().slice(0..2),
                                fontSize = 15.sp, textAlign = TextAlign.Start
                            )
                        }
                    } else {
                        Spacer(Modifier.height((gridHeight / 8).dp))
                    }
                }
            },
            modifier = Modifier
                .height(gridHeight.dp)
                .fillMaxWidth(),
            state = calendarState,
            dayContent = { day, heatMapWeek ->
                val match = entriesByDate[day.date]
                val entity = match?.first
                val hasMultiple = match?.second == true

                val state: DayState = when {
                    hasMultiple -> DayState.Error
                    habitEntity != null -> resolveDayState(habitEntity, entity, day.date, dateToday)
                    entity != null && entity.repetitionsOnThisDay > 0 -> DayState.Absolute
                    day.date > dateToday -> DayState.IncompleteDisabled
                    else -> DayState.Incomplete
                }

                // Intensity = how much of the target was logged. Negative habits
                // shade uniformly (any within-limit day is equally clean).
                val intensity = when {
                    habitEntity == null || habitEntity.isNegative -> 1f
                    entity == null || habitEntity.repetitionPerDay <= 0.0 -> 0f
                    else ->
                        (entity.repetitionsOnThisDay / habitEntity.repetitionPerDay)
                            .toFloat().coerceIn(0f, 1f)
                }

                val hideCell = state.isDisabled &&
                    !heatMapWeek.days.any { it.date == LocalDate.now() }

                if (!hideCell) {
                    Box(
                        Modifier
                            .height((gridHeight / 8).dp)
                            .aspectRatio(1f),
                    ) {
                        Box(Modifier.padding((gridHeight / 80).dp)) {
                            GridDayItem(
                                state = state,
                                intensity = intensity,
                                date = day.date,
                                showDate = showDate,
                                interactive = !state.isDisabled && interactive && state != DayState.Error,
                                hasNote = entity?.hasNote() == true,
                                incrementHabit = { incrementHabit(day.date) },
                                unSkipHabit = { unSkipHabit(day.date) },
                                dialogueComposable = { visible, onDismiss ->
                                    dialogueComposable(visible, onDismiss, entity, day.date)
                                }
                            )
                        }
                    }
                }
            })
    }
}
