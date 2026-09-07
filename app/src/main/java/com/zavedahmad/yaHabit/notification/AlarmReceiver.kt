package com.zavedahmad.yaHabit.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zavedahmad.yaHabit.database.entities.DayState
import com.zavedahmad.yaHabit.database.entities.resolveDayState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.time.LocalDate
import java.time.LocalTime

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habitId = intent.getIntExtra(AlarmScheduler.EXTRA_HABIT_ID, -1)
                if (habitId == -1) return@launch
                val scheduler: AlarmScheduler = GlobalContext.get().get()
                val helper: NotificationHelper = GlobalContext.get().get()
                val habitDao = GlobalContext.get().get<com.zavedahmad.yaHabit.database.daos.HabitDao>()
                val completionDao = GlobalContext.get().get<com.zavedahmad.yaHabit.database.daos.HabitCompletionDao>()

                if (!scheduler.isGlobalEnabled()) return@launch

                val today = LocalDate.now()
                // 1/day de-dupe
                if (scheduler.wasNotifiedToday(habitId, today)) {
                    // Already notified, just reschedule for tomorrow
                    val h = try { habitDao.getHabitById(habitId) } catch (_: Exception) { null }
                    if (h != null) scheduler.schedule(h)
                    return@launch
                }
                // Quiet window: 22:00-07:00 -> defer (reschedule)
                val nowTime = LocalTime.now()
                if (nowTime.hour >= 22 || nowTime.hour < 7) {
                    val h = try { habitDao.getHabitById(habitId) } catch (_: Exception) { null }
                    if (h != null) scheduler.schedule(h)
                    return@launch
                }

                val habit = try { habitDao.getHabitById(habitId) } catch (_: Exception) { null } ?: return@launch
                if (habit.isArchived) {
                    scheduler.cancel(habitId)
                    return@launch
                }
                if (!habit.reminderEnabled) {
                    scheduler.cancel(habitId)
                    return@launch
                }

                val completion = completionDao.getEntryOfCertainHabitIdAndDate(habitId, today)
                val state = resolveDayState(habit, completion, today, today)
                val doneStates = setOf(
                    DayState.Absolute,
                    DayState.AbsoluteMore,
                    DayState.Skip,
                    DayState.NotNeeded,
                    DayState.NegativeCount
                )
                if (state in doneStates) {
                    // Already done -> never notify, reschedule tomorrow
                    scheduler.schedule(habit)
                    return@launch
                }
                // Show notification (opportune moment)
                helper.showHabitReminder(habit)
                scheduler.markNotified(habitId, today)
                // Schedule next day
                scheduler.schedule(habit)
            } catch (_: Exception) {
            } finally {
                pending.finish()
            }
        }
    }
}
