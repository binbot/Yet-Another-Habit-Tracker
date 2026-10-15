package com.zavedahmad.yaHabit.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single, self-contained step inside a routine.
 *
 * Steps are intentionally independent of [HabitEntity]: a "meditate" habit and a "meditate"
 * step coexist without implying each other's completion, and a step such as "wash my hands"
 * can exist without ever appearing in the habit list. Seeding a step from a habit copies only
 * the name/description once; there is no live link back to the habit.
 *
 * A routine is met for a day once every step is done or skipped (skip is neutral).
 */
@Entity(
    tableName = "RoutineStep",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["routineId"])]
)
data class RoutineStepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val routineId: Int,
    val name: String,
    val description: String = "",
    val position: Int = 0
)
