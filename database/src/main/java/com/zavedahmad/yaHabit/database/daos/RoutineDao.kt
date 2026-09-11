package com.zavedahmad.yaHabit.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.zavedahmad.yaHabit.database.entities.RoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Upsert
    suspend fun upsert(routine: RoutineEntity): Long

    @Query("SELECT * FROM RoutineTable ORDER BY `index` ASC")
    fun getRoutinesFlowSorted(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM RoutineTable WHERE id = :id")
    suspend fun getById(id: Int): RoutineEntity

    @Query("SELECT * FROM RoutineTable WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<RoutineEntity?>

    @Query("SELECT MAX(`index`) FROM RoutineTable")
    suspend fun getMaxIndex(): Int?

    @Query("DELETE FROM RoutineTable WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE RoutineTable SET `index` = `index` -1 WHERE `index` > :index")
    suspend fun pluck(index: Int)

    @Query("UPDATE RoutineTable SET `index` = `index` +1 WHERE `index` >= :index")
    suspend fun vacant(index: Int)

    @Query("UPDATE RoutineTable SET `index` = :index WHERE id = :id")
    suspend fun changeIndex(index: Int, id: Int)

    @Query("UPDATE RoutineTable SET isArchived = :v WHERE id = :id")
    suspend fun archive(id: Int, v: Boolean)

    @Query("SELECT * FROM RoutineTable WHERE `index` = :index")
    suspend fun getByIndex(index: Int): RoutineEntity
}
