package com.zavedahmad.yaHabit.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Per-day completion of a single [RoutineStepEntity].
 *
 * v1 is yes/no only: [isDone] is the tick, [isSkip] marks the step as deliberately skipped for
 * the day (neutral — it still counts as satisfied when deciding whether the routine is met).
 * One row per (step, day).
 */
@Entity(
    tableName = "RoutineStepCompletion",
    foreignKeys = [
        ForeignKey(
            entity = RoutineStepEntity::class,
            parentColumns = ["id"],
            childColumns = ["stepId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["stepId", "completionDate"], unique = true)]
)
data class RoutineStepCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val stepId: Int,
    val completionDate: LocalDate,
    val isDone: Boolean = false,
    val isSkip: Boolean = false
)
