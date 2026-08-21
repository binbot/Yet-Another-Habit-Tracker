package com.zavedahmad.yaHabit.ui.habitsDetailPage

import androidx.compose.ui.graphics.Color
import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.isCompleted
import com.zavedahmad.yaHabit.database.entities.isPartial
import com.zavedahmad.yaHabit.database.entities.isSkip
import java.time.LocalDate

/**
 * Sign-aware "did this day count toward the habit's goal" check.
 *
 * Positive habits: a logged day that meets the target counts; unlogged does not.
 * Negative habits (log-and-track): unlogged or within-limit days count as clean;
 * only days logged over the limit fail.
 */
fun dayIsMet(habit: HabitEntity, completion: HabitCompletionEntity?): Boolean {
    return if (habit.isNegative) {
        val reps = completion?.repetitionsOnThisDay ?: 0.0
        reps <= habit.repetitionPerDay
    } else {
        completion != null && !completion.isSkip() && habit.isCompleted(completion)
    }
}

data class CycleMetrics(
    val metDays: Int,
    val failedDays: Int,
    val skipDays: Int,
    val trackedDays: Int,
    val successRate: Int,
    val currentStreak: Int,
    val bestStreak: Int
)

/**
 * Computes summary metrics over the habit's full completion history.
 * Negative habits treat unlogged days within [start, today] as clean.
 */
fun computeCycleMetrics(
    habit: HabitEntity,
    habitAllData: List<HabitCompletionEntity>?,
    today: LocalDate = LocalDate.now()
): CycleMetrics {
    if (habitAllData == null || habitAllData.isEmpty()) {
        return CycleMetrics(0, 0, 0, 0, 0, 0, 0)
    }

    // One entry per date; prefer the real log over auto-filled partial
    // placeholders (which have reps = 0 and would read as "not met").
    val byDate = HashMap<LocalDate, HabitCompletionEntity>()
    for (entry in habitAllData) {
        val existing = byDate[entry.completionDate]
        if (existing == null || (existing.isPartial() && !entry.isPartial())) {
            byDate[entry.completionDate] = entry
        }
    }

    var metDays = 0
    var failedDays = 0
    var skipDays = 0
    for (entry in habitAllData) {
        when {
            entry.isSkip() -> skipDays++
            dayIsMet(habit, entry) -> metDays++
            else -> failedDays++
        }
    }

    // Current streak: walk backwards from today. Today not being logged yet
    // does not break a streak (grace day) - we simply start from yesterday.
    // The walk never goes back past when tracking began. For negative habits
    // every unlogged past day is clean, so this stops only at an over-limit day.
    val floorDate = byDate.keys.minOrNull()?.coerceAtLeast(today.minusYears(2))
    var currentStreak = 0
    if (floorDate != null) {
        var checkDate = today
        if (!dayIsMet(habit, byDate[checkDate])) {
            checkDate = checkDate.minusDays(1)
        }
        while (!checkDate.isBefore(floorDate)) {
            if (dayIsMet(habit, byDate[checkDate])) {
                currentStreak++
                checkDate = checkDate.minusDays(1)
            } else break
        }
    }

    // Best streak: walk forward across the tracked window (first log .. today).
    val earliest = byDate.keys.minOrNull()
    var bestStreak = 0
    if (earliest != null) {
        var temp = 0
        var date: LocalDate = earliest
        while (!date.isAfter(today)) {
            if (dayIsMet(habit, byDate[date])) {
                temp++
                if (temp > bestStreak) bestStreak = temp
            } else {
                temp = 0
            }
            date = date.plusDays(1)
        }
    }

    val trackedDays = metDays + failedDays
    val successRate = if (trackedDays > 0) (metDays * 100 / trackedDays) else 0

    return CycleMetrics(
        metDays = metDays,
        failedDays = failedDays,
        skipDays = skipDays,
        trackedDays = trackedDays,
        successRate = successRate,
        currentStreak = currentStreak,
        bestStreak = bestStreak
    )
}
