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
    Failed("failed"),
    FailedDisabled("failedDisabled"),
    Incomplete("incomplete"),
    IncompleteDisabled("incompleteDisabled");

    val isDisabled: Boolean
        get() = this == NotNeededDisabled || this == SkipDisabled || this == PartialDisabled ||
                this == NoteDisabled || this == AbsoluteDisabled || this == AbsoluteMoreDisabled ||
                this == FailedDisabled || this == IncompleteDisabled

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
 * 3. partial (auto-filled placeholder, still needs completion)
 * 4. note-only entry
 * 5. empty/deletable entry
 * 6. completed vs not, sign-aware:
 *    - positive: met target => Absolute, over target => AbsoluteMore, under => Partial
 *    - negative (log-and-track): under or at limit => Absolute, over limit => Failed
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
        return if (date > today) DayState.IncompleteDisabled else DayState.Incomplete
    }

    val baseState = when {
        completion.isNotNeeded() -> DayState.NotNeeded
        completion.isSkip() -> DayState.Skip
        completion.isPartial() -> DayState.Partial
        completion.isOnlyNote() -> DayState.Note
        completion.repetitionsOnThisDay == 0.0 -> DayState.Incomplete
        else -> {
            val completed = habit.isCompleted(completion)
            when {
                habit.isNegative ->
                    if (completed) DayState.Absolute else DayState.Failed
                completed ->
                    if (completion.repetitionsOnThisDay > habit.repetitionPerDay) DayState.AbsoluteMore
                    else DayState.Absolute
                else -> DayState.Partial
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
    DayState.Failed -> DayState.FailedDisabled
    DayState.Incomplete -> DayState.IncompleteDisabled
    else -> this
}