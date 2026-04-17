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
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.hasNote
import com.zavedahmad.yaHabit.database.entities.isCompleted
import com.zavedahmad.yaHabit.database.entities.state

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
    Column {

        DaysOfWeekTitle(daysOfWeek(firstDayOfWeek = firstDayOfWeek))
        WeekCalendar(dayContent = { day ->

            var suffix = ""
            var hasNote = false
            var dayState = ""
            if (habitData != null) {
                val datesMatching = habitData.filter { it.completionDate == day.date }
                val hasMultipleEntries = datesMatching.size > 1
                val habitCompletionEntity = datesMatching.firstOrNull()

                if (hasMultipleEntries) {
                    dayState = "error"
                } else if (habitCompletionEntity != null) {
                    hasNote = habitCompletionEntity.hasNote()
                    suffix = if (day.date > dateToday) {
                        "Disabled"
                    } else {
                        ""
                    }

                    val isCompleted = habitEntity.isCompleted(habitCompletionEntity)

                    if (habitEntity.isNegative) {
                        dayState = if (isCompleted) "absolute" else "failed"
                    } else {
                        dayState = if (isCompleted) {
                            if (habitCompletionEntity.repetitionsOnThisDay > habitEntity.repetitionPerDay) "absoluteMore" else "absolute"
                        } else {
                            "partial"
                        }
                    }

                    dayState += suffix
                } else {
                    if (day.date > dateToday) {
                        dayState = "incompleteDisabled"
                        suffix = "Disabled"
                    } else {
                        dayState = if (habitEntity.isNegative) "absolute" else "incomplete"
                    }

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
                    }, interactive = suffix != "Disabled",
                    dialogueComposable = { visible, onDismiss ->
                        dialogueComposable(visible, onDismiss, habitCompletionEntity, day.date)
                    })

            } else {
                DayItem(
                    date = day.date,
                    state = "incomplete",
                    repetitionsOnThisDay = 0.0,
                    skipHabit = {},
                    unSkipHabit = {},
                    dialogueComposable = { a, b -> }
                )
            }
        }, state = state)
    }
}
