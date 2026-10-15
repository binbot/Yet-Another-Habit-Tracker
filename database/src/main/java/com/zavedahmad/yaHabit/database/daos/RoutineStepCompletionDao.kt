package com.zavedahmad.yaHabit.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.zavedahmad.yaHabit.database.entities.RoutineStepCompletionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface RoutineStepCompletionDao {
    @Upsert
    suspend fun upsert(completion: RoutineStepCompletionEntity)

    @Query("SELECT * FROM RoutineStepCompletion WHERE stepId = :stepId AND completionDate = :date")
    suspend fun getForStepAndDate(stepId: Int, date: LocalDate): RoutineStepCompletionEntity?

    @Query("SELECT * FROM RoutineStepCompletion WHERE stepId = :stepId ORDER BY completionDate ASC")
    fun getCompletionsForStepFlow(stepId: Int): Flow<List<RoutineStepCompletionEntity>>

    @Query("SELECT * FROM RoutineStepCompletion WHERE completionDate = :date")
    fun getCompletionsForDateFlow(date: LocalDate): Flow<List<RoutineStepCompletionEntity>>

    @Query("DELETE FROM RoutineStepCompletion WHERE stepId = :stepId AND completionDate = :date")
    suspend fun deleteForStepAndDate(stepId: Int, date: LocalDate)

    @Query("DELETE FROM RoutineStepCompletion WHERE stepId = :stepId")
    suspend fun deleteAllForStep(stepId: Int)
}
