package com.zavedahmad.yaHabit.database

import com.zavedahmad.yaHabit.database.entities.HabitEntity

interface HabitReminderScheduler {
    suspend fun schedule(habit: HabitEntity)
    fun cancel(habitId: Int)
    suspend fun rescheduleAll()
    suspend fun onHabitCompleted(habitId: Int)
    suspend fun isGlobalEnabled(): Boolean
}
