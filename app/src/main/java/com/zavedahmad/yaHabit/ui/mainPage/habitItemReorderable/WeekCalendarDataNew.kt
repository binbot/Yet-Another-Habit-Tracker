package com.zavedahmad.yaHabit.ui.mainPage.habitItemReorderable

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import com.zavedahmad.yaHabit.database.entities.DayState
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.hasNote
import com.zavedahmad.yaHabit.database.entities.resolveDayState

import com.zavedahmad.yaHabit.ui.components.DaysOfWeekTitle
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeekCalendarDataNew(

    incrementHabit: (date: LocalDate) -> Unit,
    deleteRepetitionsForDate: (date: LocalDate) -> Unit,
    initialWeekString: String? = null,
    habitEntity: HabitEntity,
    skipHabitForDate: (date: LocalDate) -> Unit,
    habitData: List<HabitCompletionEntity>?,
    firstDayOfWeek: DayOfWeek,
    unSkipHabit: (date: LocalDate) -> Unit,
    dialogueComposable: @Composable (Boolean, () -> Unit, HabitCompletionEntity?, LocalDate) -> Unit
) {
    val todayDate = LocalDate.now()
    val daysOfWeek = daysOfWeek()

    val state = rememberWeekCalendarState(
        startDate = todayDate.minusDays(10),
        endDate = todayDate,
        firstVisibleWeekDate = todayDate,
        firstDayOfWeek = firstDayOfWeek
    )


    LaunchedEffect(firstDayOfWeek) {

        state.firstDayOfWeek = firstDayOfWeek
        state.scrollToWeek(todayDate)

    }
    LaunchedEffect(state.firstVisibleWeek) {
        if (state.firstVisibleWeek.days.any { it.date == todayDate }) {
//            state.startDate = state.firstVisibleWeek.days.first().date.minusDays(14)

        } else {
            state.startDate = state.firstVisibleWeek.days.first().date.minusDays(14)

        }

    }


    val dateToday = LocalDate.now()
    val habitDataByDate = remember(habitData) {
        habitData?.groupBy { it.completionDate }
    }
    Column {

        DaysOfWeekTitle(daysOfWeek(firstDayOfWeek = firstDayOfWeek))
        WeekCalendar(dayContent = { day ->

            var hasNote = false
            var dayState: DayState
            if (habitData != null) {
                val datesMatching = habitDataByDate?.get(day.date) ?: emptyList()
                val hasMultipleEntries = datesMatching.size > 1
                val habitCompletionEntity = datesMatching.firstOrNull()

                if (hasMultipleEntries) {
                    dayState = DayState.Error
                } else {
                    hasNote = habitCompletionEntity?.hasNote() == true
                    dayState = resolveDayState(
                        habit = habitEntity,
                        completion = habitCompletionEntity,
                        date = day.date,
                        today = dateToday
                    )
                }

                DayItem(
                    hasNote = hasNote,
                    repetitionsOnThisDay = habitCompletionEntity?.repetitionsOnThisDay ?: 0.0,
                    unSkipHabit = { unSkipHabit(day.date) },
                    date = day.date,
                    state = dayState,
                    skipHabit = { skipHabitForDate(day.date) },
                    incrementHabit = {
                        incrementHabit(day.date)
                    },
                    deleteHabit = {
                        deleteRepetitionsForDate(day.date)
                    }, interactive = !dayState.isDisabled,
                    dialogueComposable = { visible, onDismiss ->
                        dialogueComposable(visible, onDismiss, habitCompletionEntity, day.date)
                    })

            } else {
                DayItem(
                    date = day.date,
                    state = DayState.Incomplete,
                    repetitionsOnThisDay = 0.0,
                    skipHabit = {},
                    unSkipHabit = {},
                    dialogueComposable = { a, b -> }
                )
            }
        }, state = state)
    }
}
