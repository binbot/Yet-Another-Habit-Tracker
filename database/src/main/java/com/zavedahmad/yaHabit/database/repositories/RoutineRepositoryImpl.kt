package com.zavedahmad.yaHabit.database.repositories

import androidx.room.withTransaction
import com.zavedahmad.yaHabit.database.MainDatabase
import com.zavedahmad.yaHabit.database.daos.HabitDao
import com.zavedahmad.yaHabit.database.daos.RoutineDao
import com.zavedahmad.yaHabit.database.daos.RoutineItemDao
import com.zavedahmad.yaHabit.database.daos.RoutineStepCompletionDao
import com.zavedahmad.yaHabit.database.daos.RoutineStepDao
import com.zavedahmad.yaHabit.database.entities.RoutineEntity
import com.zavedahmad.yaHabit.database.entities.RoutineItemEntity
import com.zavedahmad.yaHabit.database.entities.RoutineStepCompletionEntity
import com.zavedahmad.yaHabit.database.entities.RoutineStepEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class RoutineRepositoryImpl(
    private val routineDao: RoutineDao,
    private val routineItemDao: RoutineItemDao,
    private val routineStepDao: RoutineStepDao,
    private val routineStepCompletionDao: RoutineStepCompletionDao,
    private val habitDao: HabitDao,
    private val db: MainDatabase
) : RoutineRepository {

    override suspend fun addRoutine(routine: RoutineEntity, habitIds: List<Int>): Long {
        val max = routineDao.getMaxIndex()
        val index = max?.let { it + 1 } ?: 0
        val id = routineDao.upsert(routine.copy(index = index))
        val items = habitIds.mapIndexed { idx, hid -> RoutineItemEntity(routineId = id.toInt(), habitId = hid, index = idx) }
        if (items.isNotEmpty()) routineItemDao.upsertAll(items)
        // Transitional: keep first-class steps in sync with the habit-linked items so routines
        // created before the step UI lands still have steps once it arrives.
        seedStepsFromHabits(id.toInt(), habitIds)
        return id
    }

    override suspend fun editRoutine(routine: RoutineEntity, habitIds: List<Int>) {
        db.withTransaction {
            routineDao.upsert(routine)
            routineItemDao.deleteAllForRoutine(routine.id)
            val items = habitIds.mapIndexed { idx, hid -> RoutineItemEntity(routineId = routine.id, habitId = hid, index = idx) }
            if (items.isNotEmpty()) routineItemDao.upsertAll(items)
            seedStepsFromHabits(routine.id, habitIds)
        }
    }

    /** Copies the chosen habits into first-class steps (name/description only, no live link). */
    private suspend fun seedStepsFromHabits(routineId: Int, habitIds: List<Int>) {
        routineStepDao.deleteAllForRoutine(routineId)
        if (habitIds.isEmpty()) return
        val steps = habitIds.mapIndexed { idx, hid ->
            val habit = habitDao.getHabitById(hid)
            RoutineStepEntity(routineId = routineId, name = habit.name, description = habit.description, position = idx)
        }
        routineStepDao.upsertAll(steps)
    }

    override suspend fun deleteRoutine(id: Int) {
        db.withTransaction {
            val r = routineDao.getById(id)
            routineDao.pluck(r.index)
            routineDao.deleteById(id)
        }
    }

    override fun getRoutinesFlowSorted(): Flow<List<RoutineEntity>> = routineDao.getRoutinesFlowSorted()

    override suspend fun getRoutineById(id: Int): RoutineEntity = routineDao.getById(id)

    override fun getRoutineByIdFlow(id: Int): Flow<RoutineEntity?> = routineDao.getByIdFlow(id)

    override fun getItemsForRoutineFlow(routineId: Int): Flow<List<RoutineItemEntity>> = routineItemDao.getItemsForRoutineFlow(routineId)

    override suspend fun getItemsForRoutine(routineId: Int): List<RoutineItemEntity> = routineItemDao.getItemsForRoutine(routineId)

    override fun getRoutinesForHabitFlow(habitId: Int): Flow<List<RoutineItemEntity>> = routineItemDao.getRoutinesForHabitFlow(habitId)

    override suspend fun moveRoutine(fromIndex: Int, toIndex: Int) {
        val entity = routineDao.getByIndex(fromIndex)
        db.withTransaction {
            routineDao.pluck(entity.index)
            routineDao.vacant(toIndex)
            routineDao.changeIndex(toIndex, entity.id)
        }
    }

    override suspend fun moveItemInRoutine(routineId: Int, fromIndex: Int, toIndex: Int) {
        val items = routineItemDao.getItemsForRoutine(routineId)
        val from = items.firstOrNull { it.index == fromIndex } ?: return
        val to = items.firstOrNull { it.index == toIndex } ?: run {
            // if moving to end beyond current, allow
            if (toIndex >= items.size) {
                db.withTransaction {
                    routineItemDao.pluck(routineId, from.index)
                    // place at end
                    val newIdx = items.size - 1
                    routineItemDao.changeIndex(newIdx, routineId, from.habitId)
                }
                return
            }
            return
        }
        db.withTransaction {
            routineItemDao.pluck(routineId, from.index)
            routineItemDao.vacant(routineId, to.index)
            routineItemDao.changeIndex(to.index, routineId, from.habitId)
        }
    }

    override suspend fun archiveRoutine(id: Int, archived: Boolean) {
        routineDao.archive(id, archived)
    }

    // --- first-class routine steps -------------------------------------------------------------

    override fun getStepsForRoutineFlow(routineId: Int): Flow<List<RoutineStepEntity>> =
        routineStepDao.getStepsForRoutineFlow(routineId)

    override suspend fun getStepsForRoutine(routineId: Int): List<RoutineStepEntity> =
        routineStepDao.getStepsForRoutine(routineId)

    override suspend fun addStep(step: RoutineStepEntity): Long {
        val nextPosition = (routineStepDao.getStepsForRoutine(step.routineId).maxOfOrNull { it.position } ?: -1) + 1
        return routineStepDao.upsert(step.copy(position = nextPosition))
    }

    override suspend fun updateStep(step: RoutineStepEntity) {
        routineStepDao.upsert(step)
    }

    override suspend fun deleteStep(stepId: Int) {
        db.withTransaction {
            val step = routineStepDao.getById(stepId) ?: return@withTransaction
            routineStepDao.deleteById(stepId)
            routineStepDao.pluck(step.routineId, step.position)
        }
    }

    override suspend fun moveStepInRoutine(routineId: Int, fromPosition: Int, toPosition: Int) {
        val steps = routineStepDao.getStepsForRoutine(routineId)
        val from = steps.firstOrNull { it.position == fromPosition } ?: return
        db.withTransaction {
            routineStepDao.pluck(routineId, from.position)
            routineStepDao.vacant(routineId, toPosition)
            routineStepDao.changePosition(toPosition, from.id)
        }
    }

    override fun getStepCompletionsForDateFlow(date: LocalDate): Flow<List<RoutineStepCompletionEntity>> =
        routineStepCompletionDao.getCompletionsForDateFlow(date)

    override fun getStepCompletionsForStepFlow(stepId: Int): Flow<List<RoutineStepCompletionEntity>> =
        routineStepCompletionDao.getCompletionsForStepFlow(stepId)

    override suspend fun setStepDone(stepId: Int, date: LocalDate, done: Boolean) {
        upsertStepCompletion(stepId, date, isDone = done)
    }

    override suspend fun setStepSkip(stepId: Int, date: LocalDate, skip: Boolean) {
        upsertStepCompletion(stepId, date, isSkip = skip)
    }

    private suspend fun upsertStepCompletion(
        stepId: Int,
        date: LocalDate,
        isDone: Boolean? = null,
        isSkip: Boolean? = null
    ) {
        val existing = routineStepCompletionDao.getForStepAndDate(stepId, date)
        val done = isDone ?: existing?.isDone ?: false
        val skip = isSkip ?: existing?.isSkip ?: false
        if (!done && !skip) {
            routineStepCompletionDao.deleteForStepAndDate(stepId, date)
        } else {
            routineStepCompletionDao.upsert(
                existing?.copy(isDone = done, isSkip = skip)
                    ?: RoutineStepCompletionEntity(stepId = stepId, completionDate = date, isDone = done, isSkip = skip)
            )
        }
    }
}
