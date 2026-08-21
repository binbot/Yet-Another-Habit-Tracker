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
    val bestStreak: Int,
    // True when streaks are counted in whole cycles (flexible schedules,
    // e.g. 3 times per week) rather than consecutive days.
    val cycleBased: Boolean
)

/**
 * Computes summary metrics over the habit's full completion history.
 *
 * Streak semantics:
 * - Negative habits: day-based; unlogged days are clean, streaks break only
 *   on an over-limit day.
 * - Positive habits with frequency < cycle ("3 of 7 days"): streaks count
 *   consecutive met cycles. A cycle is a [HabitEntity.cycle]-day window that
 *   contains at least [HabitEntity.frequency] met days. The still-open current
 *   window gets grace: it does not break the streak until it closes unmet.
 * - Positive habits with frequency >= cycle (effectively daily): day-based,
 *   with today unlogged treated as grace rather than a break.
 */
fun computeCycleMetrics(
    habit: HabitEntity,
    habitAllData: List<HabitCompletionEntity>?,
    today: LocalDate = LocalDate.now()
): CycleMetrics {
    if (habitAllData == null || habitAllData.isEmpty()) {
        return CycleMetrics(0, 0, 0, 0, 0, 0, 0, false)
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

    val earliest = byDate.keys.minOrNull() ?: return CycleMetrics(0, 0, 0, 0, 0, 0, 0, false)
    val floorDate = earliest.coerceAtLeast(today.minusYears(2))

    val cycleBased = !habit.isNegative && habit.cycle > 0 && habit.frequency < habit.cycle

    fun windowMet(windowEnd: LocalDate): Boolean {
        var count = 0
        var d = windowEnd.minusDays(habit.cycle - 1L)
        while (!d.isAfter(windowEnd)) {
            if (dayIsMet(habit, byDate[d])) count++
            d = d.plusDays(1)
        }
        return count >= habit.frequency
    }

    var currentStreak = 0
    var bestStreak = 0

    if (cycleBased) {
        // Current streak in cycles, anchored at today and stepping back one
        // full window at a time. The open current window is grace: skip it
        // instead of breaking when it is not yet met.
        var end = today
        if (!windowMet(end)) end = end.minusDays(habit.cycle.toLong())
        while (!end.isBefore(floorDate)) {
            if (windowMet(end)) {
                currentStreak++
                end = end.minusDays(habit.cycle.toLong())
            } else break
        }

        // Best streak in consecutive non-overlapping cycles anchored at the
        // first tracked day.
        var end2 = earliest.plusDays(habit.cycle - 1L)
        var temp = 0
        while (!end2.isAfter(today)) {
            if (windowMet(end2)) {
                temp++
                if (temp > bestStreak) bestStreak = temp
            } else temp = 0
            end2 = end2.plusDays(habit.cycle.toLong())
        }
    } else {
        // Day-based current streak with a grace day for unlogged todays.
        var checkDate = today
        if (!dayIsMet(habit, byDate[checkDate])) checkDate = checkDate.minusDays(1)
        while (!checkDate.isBefore(floorDate)) {
            if (dayIsMet(habit, byDate[checkDate])) {
                currentStreak++
                checkDate = checkDate.minusDays(1)
            } else break
        }

        // Day-based best streak across the tracked window.
        var temp = 0
        var date: LocalDate = earliest
        while (!date.isAfter(today)) {
            if (dayIsMet(habit, byDate[date])) {
                temp++
                if (temp > bestStreak) bestStreak = temp
            } else temp = 0
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
        bestStreak = bestStreak,
        cycleBased = cycleBased
    )
}
