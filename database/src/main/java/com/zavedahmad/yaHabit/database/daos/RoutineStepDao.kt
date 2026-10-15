package com.zavedahmad.yaHabit.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.zavedahmad.yaHabit.database.entities.RoutineStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineStepDao {
    @Upsert
    suspend fun upsert(step: RoutineStepEntity): Long

    @Upsert
    suspend fun upsertAll(steps: List<RoutineStepEntity>)

    @Query("SELECT * FROM RoutineStep WHERE routineId = :routineId ORDER BY `position` ASC")
    fun getStepsForRoutineFlow(routineId: Int): Flow<List<RoutineStepEntity>>

    @Query("SELECT * FROM RoutineStep WHERE routineId = :routineId ORDER BY `position` ASC")
    suspend fun getStepsForRoutine(routineId: Int): List<RoutineStepEntity>

    @Query("SELECT * FROM RoutineStep WHERE id = :stepId")
    suspend fun getById(stepId: Int): RoutineStepEntity?

    @Query("DELETE FROM RoutineStep WHERE routineId = :routineId")
    suspend fun deleteAllForRoutine(routineId: Int)

    @Query("DELETE FROM RoutineStep WHERE id = :stepId")
    suspend fun deleteById(stepId: Int)

    @Query("UPDATE RoutineStep SET `position` = `position` - 1 WHERE routineId = :routineId AND `position` > :position")
    suspend fun pluck(routineId: Int, position: Int)

    @Query("UPDATE RoutineStep SET `position` = `position` + 1 WHERE routineId = :routineId AND `position` >= :position")
    suspend fun vacant(routineId: Int, position: Int)

    @Query("UPDATE RoutineStep SET `position` = :position WHERE id = :stepId")
    suspend fun changePosition(position: Int, stepId: Int)
}
