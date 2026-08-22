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
import com.zavedahmad.yaHabit.database.entities.isPartial
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

    // One entry per date; prefer the real log over auto-filled placeholders.
    val byDate = remember(habitData) {
        val result = HashMap<LocalDate, HabitCompletionEntity>()
        habitData?.forEach { entry ->
            val existing = result[entry.completionDate]
            if (existing == null || (existing.isPartial() && !entry.isPartial())) {
                result[entry.completionDate] = entry
            }
        }
        result
    }

    if (habitData == null || habitEntity == null) return

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
                val entry = byDate[day.date]
                val cls = classifyDay(habitEntity, entry, day.date, dateToday)
                val cs = MaterialTheme.colorScheme

                // Discrete GitHub-style steps: how much of the daily target
                // was actually logged, so measurable habits show gradations.
                val heatAlphas = floatArrayOf(0.15f, 0.30f, 0.50f, 0.70f, 1.00f)
                val level = heatLevel(habitEntity, entry)

                val bg = when {
                    cls == DayClass.SKIP -> cs.tertiaryContainer
                    cls == DayClass.EXCUSED -> cs.surfaceVariant.copy(alpha = 0.45f)
                    level != null && level > 0 -> cs.primary.copy(alpha = heatAlphas[level])
                    level != null && level < 0 ->
                        FailedRed.copy(alpha = 0.35f + 0.15f * -level)   // negative habit, over limit
                    level != null -> cs.primary.copy(alpha = heatAlphas[0]) // logged but zero reps
                    cls == DayClass.MET -> cs.primary   // negative habit clean day (incl. unlogged)
                    else -> cs.inverseSurface.copy(alpha = 0.05f)          // untracked gap / future
                }

                val firstLog = remember(habitData) { byDate.keys.minOrNull() }
                val outsideTrackingEra = firstLog != null && day.date.isBefore(firstLog)
                val futureOutsideCurrentWeek = cls == DayClass.NEUTRAL &&
                    day.date.isAfter(dateToday) &&
                    !heatMapWeek.days.any { it.date == LocalDate.now() }
                val hideCell = outsideTrackingEra || futureOutsideCurrentWeek

                if (!hideCell) {
                    Box(
                        Modifier
                            .height((gridHeight / 8).dp)
                            .aspectRatio(1f),
                    ) {
                        Box(Modifier.padding((gridHeight / 80).dp)) {
                            GridDayItem(
                                bg = bg,
                                date = day.date,
                                showDate = showDate,
                                interactive = interactive && cls != DayClass.NEUTRAL,
                                hasNote = entry?.hasNote() == true,
                                isSkipCell = cls == DayClass.SKIP,
                                incrementHabit = { incrementHabit(day.date) },
                                unSkipHabit = { unSkipHabit(day.date) },
                                dialogueComposable = { visible, onDismiss ->
                                    dialogueComposable(visible, onDismiss, entry, day.date)
                                }
                            )
                        }
                    }
                }
            })
    }
}
