package com.zavedahmad.yaHabit.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.zavedahmad.yaHabit.database.entities.RoutineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineItemDao {
    @Upsert
    suspend fun upsert(item: RoutineItemEntity)

    @Upsert
    suspend fun upsertAll(items: List<RoutineItemEntity>)

    @Query("SELECT * FROM RoutineItem WHERE routineId = :routineId ORDER BY `index` ASC")
    fun getItemsForRoutineFlow(routineId: Int): Flow<List<RoutineItemEntity>>

    @Query("SELECT * FROM RoutineItem WHERE routineId = :routineId ORDER BY `index` ASC")
    suspend fun getItemsForRoutine(routineId: Int): List<RoutineItemEntity>

    @Query("SELECT * FROM RoutineItem WHERE habitId = :habitId")
    fun getRoutinesForHabitFlow(habitId: Int): Flow<List<RoutineItemEntity>>

    @Query("DELETE FROM RoutineItem WHERE routineId = :routineId AND habitId = :habitId")
    suspend fun delete(routineId: Int, habitId: Int)

    @Query("DELETE FROM RoutineItem WHERE routineId = :routineId")
    suspend fun deleteAllForRoutine(routineId: Int)

    @Query("UPDATE RoutineItem SET `index` = `index` -1 WHERE routineId = :routineId AND `index` > :index")
    suspend fun pluck(routineId: Int, index: Int)

    @Query("UPDATE RoutineItem SET `index` = `index` +1 WHERE routineId = :routineId AND `index` >= :index")
    suspend fun vacant(routineId: Int, index: Int)

    @Query("UPDATE RoutineItem SET `index` = :index WHERE routineId = :r AND habitId = :h")
    suspend fun changeIndex(index: Int, r: Int, h: Int)
}
