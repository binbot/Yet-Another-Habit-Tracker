package com.zavedahmad.yaHabit.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "RoutineItem",
    primaryKeys = ["routineId", "habitId"],
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["routineId"]), Index(value = ["habitId"])]
)
data class RoutineItemEntity(
    val routineId: Int,
    val habitId: Int,
    val index: Int = 0
)
