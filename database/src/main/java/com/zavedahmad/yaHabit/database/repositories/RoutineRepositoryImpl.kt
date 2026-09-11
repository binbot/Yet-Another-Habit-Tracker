package com.zavedahmad.yaHabit.database.repositories

import androidx.room.withTransaction
import com.zavedahmad.yaHabit.database.MainDatabase
import com.zavedahmad.yaHabit.database.daos.RoutineDao
import com.zavedahmad.yaHabit.database.daos.RoutineItemDao
import com.zavedahmad.yaHabit.database.entities.RoutineEntity
import com.zavedahmad.yaHabit.database.entities.RoutineItemEntity
import kotlinx.coroutines.flow.Flow

class RoutineRepositoryImpl(
    private val routineDao: RoutineDao,
    private val routineItemDao: RoutineItemDao,
    private val db: MainDatabase
) : RoutineRepository {

    override suspend fun addRoutine(routine: RoutineEntity, habitIds: List<Int>): Long {
        val max = routineDao.getMaxIndex()
        val index = max?.let { it + 1 } ?: 0
        val id = routineDao.upsert(routine.copy(index = index))
        val items = habitIds.mapIndexed { idx, hid -> RoutineItemEntity(routineId = id.toInt(), habitId = hid, index = idx) }
        if (items.isNotEmpty()) routineItemDao.upsertAll(items)
        return id
    }

    override suspend fun editRoutine(routine: RoutineEntity, habitIds: List<Int>) {
        db.withTransaction {
            routineDao.upsert(routine)
            routineItemDao.deleteAllForRoutine(routine.id)
            val items = habitIds.mapIndexed { idx, hid -> RoutineItemEntity(routineId = routine.id, habitId = hid, index = idx) }
            if (items.isNotEmpty()) routineItemDao.upsertAll(items)
        }
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
}
