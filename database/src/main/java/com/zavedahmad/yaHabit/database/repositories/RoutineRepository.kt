package com.zavedahmad.yaHabit.database.repositories

import com.zavedahmad.yaHabit.database.entities.RoutineEntity
import com.zavedahmad.yaHabit.database.entities.RoutineItemEntity
import com.zavedahmad.yaHabit.database.entities.RoutineStepCompletionEntity
import com.zavedahmad.yaHabit.database.entities.RoutineStepEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface RoutineRepository {
    suspend fun addRoutine(routine: RoutineEntity): Long
    suspend fun editRoutine(routine: RoutineEntity)
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

    // --- first-class routine steps -------------------------------------------------------------

    fun getStepsForRoutineFlow(routineId: Int): Flow<List<RoutineStepEntity>>
    suspend fun getStepsForRoutine(routineId: Int): List<RoutineStepEntity>
    suspend fun addStep(step: RoutineStepEntity): Long
    suspend fun updateStep(step: RoutineStepEntity)
    suspend fun deleteStep(stepId: Int)
    suspend fun moveStepInRoutine(routineId: Int, fromPosition: Int, toPosition: Int)

    /**
     * Reconciles the routine's steps with [steps]: existing rows (id > 0) are updated in place so
     * their completion history is preserved, new rows (id == 0) are inserted, and rows missing
     * from [steps] are deleted. [steps] order becomes the step order.
     */
    suspend fun setStepsForRoutine(routineId: Int, steps: List<RoutineStepEntity>)

    fun getStepCompletionsForDateFlow(date: LocalDate): Flow<List<RoutineStepCompletionEntity>>
    fun getStepCompletionsForStepFlow(stepId: Int): Flow<List<RoutineStepCompletionEntity>>
    suspend fun setStepDone(stepId: Int, date: LocalDate, done: Boolean)
    suspend fun setStepSkip(stepId: Int, date: LocalDate, skip: Boolean)
}
