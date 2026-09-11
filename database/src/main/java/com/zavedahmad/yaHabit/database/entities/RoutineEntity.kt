package com.zavedahmad.yaHabit.database.entities

import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zavedahmad.yaHabit.database.enums.HabitStreakType

@Entity(tableName = "RoutineTable")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val index: Int = 0,
    val name: String,
    val description: String = "",
    val color: Color,
    val streakType: HabitStreakType,
    val frequency: Double,
    val cycle: Int,
    @ColumnInfo(defaultValue = "0")
    val isArchived: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val reminderEnabled: Boolean = false,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null
)
