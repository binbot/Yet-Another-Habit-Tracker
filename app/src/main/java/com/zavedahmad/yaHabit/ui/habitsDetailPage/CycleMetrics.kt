package com.zavedahmad.yaHabit.ui.habitsDetailPage

import com.zavedahmad.yaHabit.database.entities.HabitCompletionEntity
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.isCompleted
import com.zavedahmad.yaHabit.database.entities.isNotNeeded
import com.zavedahmad.yaHabit.database.entities.isPartial
import com.zavedahmad.yaHabit.database.entities.isSkip
import java.time.LocalDate

/**
 * Outcome of a single calendar day, sign- and quota-aware.
 * This is the single source of truth for stats, streaks, charts and heatmap.
 */
enum class DayClass { MET, MISSED, OVER_LIMIT, SKIP, EXCUSED, NEUTRAL }

/** True when every day is expected (frequency >= cycle): unlogged past days are misses. */
fun isStrictSchedule(habit: HabitEntity): Boolean =
    !habit.isNegative && habit.cycle > 0 && habit.frequency >= habit.cycle

/**
 * Classifies one date. Key semantics:
 * - Negative habits: any day without an over-limit log is clean (MET).
 * - Strict positive habits: an unlogged past day is MISSED.
 * - Flexible positive habits (e.g. 1 of 7): unlogged days are NEUTRAL,
 *   never counted against the user.
 * - Auto-placeholders (partial=true) on past dates read as scheduled-but-not-done.
 * - Future days are NEUTRAL.
 */
fun classifyDay(
    habit: HabitEntity,
    entry: HabitCompletionEntity?,
    date: LocalDate,
    today: LocalDate
): DayClass {
    if (date > today) return DayClass.NEUTRAL
    if (entry?.isSkip() == true) return DayClass.SKIP
    if (entry?.isNotNeeded() == true) return DayClass.EXCUSED

    return if (habit.isNegative) {
        val reps = entry?.repetitionsOnThisDay ?: 0.0
        if (reps <= habit.repetitionPerDay) DayClass.MET else DayClass.OVER_LIMIT
    } else {
        when {
            entry == null ->
                if (isStrictSchedule(habit)) DayClass.MISSED else DayClass.NEUTRAL
            habit.isCompleted(entry) -> DayClass.MET
            else -> DayClass.MISSED
        }
    }
}

data class CycleMetrics(
    val metDays: Int,
    val missedDays: Int,
    val skipDays: Int,
    val excusedDays: Int,
    // metDays + missedDays - the honest denominator for success rate.
    val trackedDays: Int,
    val successRate: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    // Streak unit: cycles for flexible schedules, days otherwise.
    val cycleBased: Boolean,
    val strictSchedule: Boolean
)

/**
 * Computes summary metrics over the full tracked window (first log .. today)
 * by classifying every single date - not just rows that exist in the DB.
 *
 * Streak semantics:
 * - Flexible positive habits: streaks count consecutive met cycles; the open
 *   current window gets grace.
 * - Everything else: day-based, today-unlogged gets grace.
 */
fun computeCycleMetrics(
    habit: HabitEntity,
    habitAllData: List<HabitCompletionEntity>?,
    today: LocalDate = LocalDate.now()
): CycleMetrics {
    if (habitAllData == null || habitAllData.isEmpty()) {
        return CycleMetrics(0, 0, 0, 0, 0, 0, 0, 0, false, false)
    }

    // One entry per date; prefer the real log over auto-filled placeholders.
    val byDate = HashMap<LocalDate, HabitCompletionEntity>()
    for (entry in habitAllData) {
        val existing = byDate[entry.completionDate]
        if (existing == null || (existing.isPartial() && !entry.isPartial())) {
            byDate[entry.completionDate] = entry
        }
    }

    val earliest = byDate.keys.minOrNull()
        ?: return CycleMetrics(0, 0, 0, 0, 0, 0, 0, 0, false, false)
    val floorDate = earliest.coerceAtLeast(today.minusYears(2))

    var metDays = 0
    var missedDays = 0
    var skipDays = 0
    var excusedDays = 0

    var d = floorDate
    while (!d.isAfter(today)) {
        when (classifyDay(habit, byDate[d], d, today)) {
            DayClass.MET -> metDays++
            DayClass.MISSED, DayClass.OVER_LIMIT -> missedDays++
            DayClass.SKIP -> skipDays++
            DayClass.EXCUSED -> excusedDays++
            DayClass.NEUTRAL -> {}
        }
        d = d.plusDays(1)
    }

    val strict = isStrictSchedule(habit)
    val cycleBased = !habit.isNegative && habit.cycle > 0 && habit.frequency < habit.cycle

    fun windowMet(windowEnd: LocalDate): Boolean {
        var count = 0
        var w = windowEnd.minusDays(habit.cycle - 1L)
        while (!w.isAfter(windowEnd)) {
            if (classifyDay(habit, byDate[w], w, today) == DayClass.MET) count++
            w = w.plusDays(1)
        }
        return count >= habit.frequency
    }

    var currentStreak = 0
    var bestStreak = 0

    if (cycleBased) {
        var end = today
        if (!windowMet(end)) end = end.minusDays(habit.cycle.toLong())
        while (!end.isBefore(floorDate)) {
            if (windowMet(end)) {
                currentStreak++
                end = end.minusDays(habit.cycle.toLong())
            } else break
        }

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
        var checkDate = today
        if (classifyDay(habit, byDate[checkDate], checkDate, today) != DayClass.MET) {
            checkDate = checkDate.minusDays(1)
        }
        while (!checkDate.isBefore(floorDate)) {
            if (classifyDay(habit, byDate[checkDate], checkDate, today) == DayClass.MET) {
                currentStreak++
                checkDate = checkDate.minusDays(1)
            } else break
        }

        var temp = 0
        var date: LocalDate = floorDate
        while (!date.isAfter(today)) {
            if (classifyDay(habit, byDate[date], date, today) == DayClass.MET) {
                temp++
                if (temp > bestStreak) bestStreak = temp
            } else temp = 0
            date = date.plusDays(1)
        }
    }

    val trackedDays = metDays + missedDays
    val successRate = if (trackedDays > 0) (metDays * 100 / trackedDays) else 0

    return CycleMetrics(
        metDays = metDays,
        missedDays = missedDays,
        skipDays = skipDays,
        excusedDays = excusedDays,
        trackedDays = trackedDays,
        successRate = successRate,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        cycleBased = cycleBased,
        strictSchedule = strict
    )
}
