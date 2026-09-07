package com.zavedahmad.yaHabit.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.zavedahmad.yaHabit.database.HabitReminderScheduler
import com.zavedahmad.yaHabit.database.daos.HabitCompletionDao
import com.zavedahmad.yaHabit.database.daos.HabitDao
import com.zavedahmad.yaHabit.database.daos.PreferencesDao
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmScheduler(
    private val context: Context,
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao,
    private val preferencesDao: PreferencesDao
) : HabitReminderScheduler {
    companion object {
        const val EXTRA_HABIT_ID = "habitId"
        private const val PREFS_LAST = "notif_last"
        private const val QUIET_START = 22
        private const val QUIET_END = 7
    }

    override suspend fun isGlobalEnabled(): Boolean {
        val pref = preferencesDao.getPreference("notificationsEnabled")
        return pref?.value == "true"
    }

    private fun isInQuietWindow(now: LocalTime): Boolean {
        val h = now.hour
        return h >= QUIET_START || h < QUIET_END
    }

    private fun nextQuietEnd(now: LocalDateTime): LocalDateTime {
        var candidate = now.withHour(QUIET_END).withMinute(0).withSecond(0).withNano(0)
        if (!candidate.isAfter(now)) candidate = candidate.plusDays(1)
        return candidate
    }

    override suspend fun schedule(habit: com.zavedahmad.yaHabit.database.entities.HabitEntity) {
        if (!habit.reminderEnabled) {
            cancel(habit.id)
            return
        }
        val hour = habit.reminderHour
        val minute = habit.reminderMinute
        if (hour == null || minute == null) {
            cancel(habit.id)
            return
        }
        if (!isGlobalEnabled()) {
            cancel(habit.id)
            return
        }
        // Don't schedule archived habits
        if (habit.isArchived) {
            cancel(habit.id)
            return
        }
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = computeNextTrigger(hour, minute)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_HABIT_ID, habit.id)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            habit.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Respect quiet window at schedule time: if trigger falls in quiet, defer to quiet end
        val quietAdjusted = if (isInQuietWindow(LocalTime.of(hour, minute))) {
            // If user picks quiet time, push to quiet end (07:00) today or tomorrow
            val now = LocalDateTime.now()
            val base = computeNextTrigger(hour, minute)
            val baseLdt = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(base), ZoneId.systemDefault())
            if (isInQuietWindow(baseLdt.toLocalTime())) nextQuietEnd(baseLdt) else baseLdt
        } else LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(triggerAt), ZoneId.systemDefault())

        val finalMillis = quietAdjusted.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, finalMillis, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, finalMillis, pi)
        }
    }

    override fun cancel(habitId: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
        pi.cancel()
    }

    override suspend fun rescheduleAll() {
        if (!isGlobalEnabled()) {
            // Cancel all if globally disabled
            val all = habitDao.getAllHabits()
            all.forEach { cancel(it.id) }
            return
        }
        val all = habitDao.getAllHabits()
        for (h in all) {
            schedule(h)
        }
    }

    override suspend fun onHabitCompleted(habitId: Int) {
        // Called after completion to suppress today's alarm if now completed
        // Check if habit today is already done; if so cancel today's pending alarm and re-schedule for tomorrow
        val habit = try { habitDao.getHabitById(habitId) } catch (_: Exception) { null } ?: return
        val today = LocalDate.now()
        val completion = habitCompletionDao.getEntryOfCertainHabitIdAndDate(habitId, today)
        val state = com.zavedahmad.yaHabit.database.entities.resolveDayState(habit, completion, today, today)
        val doneStates = setOf(
            com.zavedahmad.yaHabit.database.entities.DayState.Absolute,
            com.zavedahmad.yaHabit.database.entities.DayState.AbsoluteMore,
            com.zavedahmad.yaHabit.database.entities.DayState.Skip,
            com.zavedahmad.yaHabit.database.entities.DayState.NotNeeded,
            com.zavedahmad.yaHabit.database.entities.DayState.NegativeCount
        )
        // Negative habit failure is not done, so keep notifying? For now treat Failed as still due
        if (state in doneStates) {
            // Cancel today and schedule next day
            cancel(habitId)
            // Re-schedule for tomorrow (compute will pick tomorrow if today's time passed)
            // Small delay to let alarm be recreated
            schedule(habit)
        }
    }

    private fun computeNextTrigger(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        var trigger = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute))
        if (!trigger.isAfter(now)) trigger = trigger.plusDays(1)
        // If trigger in quiet window, defer to quiet end
        if (isInQuietWindow(trigger.toLocalTime())) {
            trigger = nextQuietEnd(trigger)
        }
        return trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun markNotified(habitId: Int, date: LocalDate) {
        context.getSharedPreferences(PREFS_LAST, Context.MODE_PRIVATE)
            .edit().putLong("last_$habitId", date.toEpochDay()).apply()
    }

    fun wasNotifiedToday(habitId: Int, date: LocalDate): Boolean {
        val last = context.getSharedPreferences(PREFS_LAST, Context.MODE_PRIVATE)
            .getLong("last_$habitId", Long.MIN_VALUE)
        return last == date.toEpochDay()
    }
}
