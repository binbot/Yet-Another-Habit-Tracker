package com.zavedahmad.yaHabit.database.repositories

import com.zavedahmad.yaHabit.database.entities.RoutineEntity
import com.zavedahmad.yaHabit.database.entities.RoutineItemEntity
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    suspend fun addRoutine(routine: RoutineEntity, habitIds: List<Int>): Long
    suspend fun editRoutine(routine: RoutineEntity, habitIds: List<Int>)
    suspend fun deleteRoutine(id: Int)
    fun getRoutinesFlowSorted(): Flow<List<RoutineEntity>>
    suspend fun getRoutineById(id: Int): RoutineEntity
    fun getRoutineByIdFlow(id: Int): Flow<RoutineEntity?>
    fun getItemsForRoutineFlow(routineId: Int): Flow<List<RoutineItemEntity>>
    suspend fun getItemsForRoutine(routineId: Int): List<RoutineItemEntity>
    fun getRoutinesForHabitFlow(habitId: Int): Flow<List<RoutineItemEntity>>
    suspend fun moveRoutine(fromIndex: Int, toIndex: Int)
    suspend fun moveItemInRoutine(routineId: Int, fromIndex: Int, toIndex: Int)
    suspend fun archiveRoutine(id: Int, archived: Boolean)
}
