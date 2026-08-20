package com.zavedahmad.yaHabit.database.entities

import java.time.LocalDate

/**
 * Single source of truth for how a habit day should be rendered.
 * Shared between the app's day cells and the home screen widget so both
 * always agree on the state of a given (habit, completion, date).
 */
enum class DayState(val stateString: String) {
    Error("error"),
    NotNeeded("notneeded"),
    NotNeededDisabled("notneededDisabled"),
    Skip("skip"),
    SkipDisabled("skipDisabled"),
    Partial("partial"),
    PartialDisabled("partialDisabled"),
    Note("note"),
    NoteDisabled("noteDisabled"),
    Absolute("absolute"),
    AbsoluteDisabled("absoluteDisabled"),
    AbsoluteMore("absoluteMore"),
    AbsoluteMoreDisabled("absoluteMoreDisabled"),
    NegativeCount("negativeCount"),
    NegativeCountDisabled("negativeCountDisabled"),
    Failed("failed"),
    FailedDisabled("failedDisabled"),
    Incomplete("incomplete"),
    IncompleteDisabled("incompleteDisabled");

    val isDisabled: Boolean
        get() = this == NotNeededDisabled || this == SkipDisabled || this == PartialDisabled ||
                this == NoteDisabled || this == AbsoluteDisabled || this == AbsoluteMoreDisabled ||
                this == NegativeCountDisabled || this == FailedDisabled || this == IncompleteDisabled

    companion object {
        fun fromString(stateString: String): DayState? = entries.firstOrNull { it.stateString == stateString }
    }
}

/**
 * Resolves the visual/interactive state of a single day for a habit.
 *
 * Order matters:
 * 1. isNotNeeded (quota already met for the cycle)
 * 2. skip
 * 3. note-only entry
 * 4. sign-specific completion state:
 *    - negative (log-and-track): no log => clean day (Absolute checkmark),
 *      count within limit => NegativeCount (number), over limit => Failed (red)
 *    - positive: partial placeholder => Partial, met target => Absolute,
 *      over target => AbsoluteMore, under => Partial
 *
 * Dates in the future resolve to their disabled variant.
 */
fun resolveDayState(
    habit: HabitEntity,
    completion: HabitCompletionEntity?,
    date: LocalDate,
    today: LocalDate
): DayState {
    if (completion == null) {
        return when {
            date > today -> DayState.IncompleteDisabled
            habit.isNegative -> DayState.Absolute
            else -> DayState.Incomplete
        }
    }

    val baseState = when {
        completion.isNotNeeded() -> DayState.NotNeeded
        completion.isSkip() -> DayState.Skip
        completion.isOnlyNote() -> DayState.Note
        habit.isNegative -> when {
            completion.repetitionsOnThisDay == 0.0 -> DayState.Absolute
            completion.repetitionsOnThisDay <= habit.repetitionPerDay -> DayState.NegativeCount
            else -> DayState.Failed
        }
        completion.isPartial() -> DayState.Partial
        completion.repetitionsOnThisDay == 0.0 -> DayState.Incomplete
        else -> {
            if (habit.isCompleted(completion)) {
                if (completion.repetitionsOnThisDay > habit.repetitionPerDay) DayState.AbsoluteMore
                else DayState.Absolute
            } else {
                DayState.Partial
            }
        }
    }

    return if (date > today) baseState.disabled() else baseState
}

private fun DayState.disabled(): DayState = when (this) {
    DayState.NotNeeded -> DayState.NotNeededDisabled
    DayState.Skip -> DayState.SkipDisabled
    DayState.Partial -> DayState.PartialDisabled
    DayState.Note -> DayState.NoteDisabled
    DayState.Absolute -> DayState.AbsoluteDisabled
    DayState.AbsoluteMore -> DayState.AbsoluteMoreDisabled
    DayState.NegativeCount -> DayState.NegativeCountDisabled
    DayState.Failed -> DayState.FailedDisabled
    DayState.Incomplete -> DayState.IncompleteDisabled
    else -> this
}