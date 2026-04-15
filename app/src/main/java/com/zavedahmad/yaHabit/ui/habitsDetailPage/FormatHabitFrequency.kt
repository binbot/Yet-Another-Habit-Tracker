package com.zavedahmad.yaHabit.ui.habitsDetailPage

import com.zavedahmad.yaHabit.database.enums.HabitStreakType

import com.zavedahmad.yahabit.common.formatNumber.formatNumberToReadable

fun formatHabitFrequency(streakType: HabitStreakType, frequency: Double, cycle: Int, formatFrequencyNumber : Boolean = false): String {
    val formattedNumber =  if (formatFrequencyNumber){
  formatNumberToReadable(frequency)}else{frequency.toString()}

    return when (streakType) {
        HabitStreakType.DAILY  -> {
             "Every day"
        }
        HabitStreakType.WEEKLY ->  {
            if (frequency == 1.0) "Once per Week" else "$formattedNumber times per Week"
        }

        HabitStreakType.MONTHLY ->  {
            if (frequency == 1.0) "Once per Month" else "$formattedNumber times per Month"
        }
        else -> {
            "$formattedNumber times per $cycle Days"
        }
    }
}