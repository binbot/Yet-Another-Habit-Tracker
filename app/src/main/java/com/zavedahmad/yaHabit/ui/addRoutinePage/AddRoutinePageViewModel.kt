package com.zavedahmad.yaHabit.ui.addRoutinePage

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.zavedahmad.yahabit.common.HabitColors
import androidx.lifecycle.viewModelScope
import com.zavedahmad.yaHabit.Screen
import com.zavedahmad.yaHabit.database.daos.HabitDao
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.RoutineEntity
import com.zavedahmad.yaHabit.database.entities.RoutineStepEntity
import com.zavedahmad.yaHabit.database.enums.HabitStreakType
import com.zavedahmad.yaHabit.database.repositories.RoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Editable step in the routine editor. [id] == 0 means it has not been persisted yet.
 */
data class StepDraft(
    val id: Int = 0,
    val name: String = "",
    val description: String = ""
)

class AddRoutinePageViewModel(
    val navKey: Screen.AddRoutinePageRoute,
    val routineRepository: RoutineRepository,
    val habitDao: HabitDao
) : ViewModel() {
    val colors = HabitColors.palette
    private val _selectedColor = MutableStateFlow(colors[0])
    val selectedColor = _selectedColor.asStateFlow()
    private val _routineName = MutableStateFlow("")
    val routineName = _routineName.asStateFlow()
    private val _routineDescription = MutableStateFlow("")
    val routineDescription = _routineDescription.asStateFlow()
    private val _frequency = MutableStateFlow<Double?>(null)
    val routineFrequency = _frequency.asStateFlow()
    private val _cycle = MutableStateFlow<Int?>(null)
    val routineCycle = _cycle.asStateFlow()
    private val _streakType = MutableStateFlow(HabitStreakType.WEEKLY)
    val streakType = _streakType.asStateFlow()
    private val _steps = MutableStateFlow<List<StepDraft>>(emptyList())
    val steps = _steps.asStateFlow()
    private val _existingRoutine = MutableStateFlow<RoutineEntity?>(null)
    val existingRoutine = _existingRoutine.asStateFlow()
    private val _habits = MutableStateFlow<List<HabitEntity>>(emptyList())
    val habits = _habits.asStateFlow()

    init { getRoutineDetails(); collectHabits() }

    private fun collectHabits() {
        viewModelScope.launch(Dispatchers.IO) {
            habitDao.getHabitsFlowSortedByIndex().collect { _habits.value = it }
        }
    }

    fun setColor(c: Color) { _selectedColor.value = c }
    fun setRoutineName(n: String) { _routineName.value = n }
    fun setRoutineDescription(d: String) { _routineDescription.value = d }
    fun setFrequency(f: Double) { _frequency.value = f }
    fun setCycle(c: Int) { _cycle.value = c }
    fun setStreakType(t: HabitStreakType) {
        _streakType.value = t
        if (t == HabitStreakType.DAILY) { _frequency.value = 1.0; _cycle.value = 1 }
    }

    // --- steps ----------------------------------------------------------------------------------

    /** Adds a brand-new step. Returns false when the name is blank. */
    fun addStep(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        _steps.value = _steps.value + StepDraft(name = trimmed)
        return true
    }

    /** Seeds a step from an existing habit (one-time copy of name/description, no live link). */
    fun addStepFromHabit(habit: HabitEntity) {
        _steps.value = _steps.value + StepDraft(name = habit.name, description = habit.description)
    }

    fun updateStepName(index: Int, name: String) {
        _steps.value = _steps.value.toMutableList().also {
            if (index in it.indices) it[index] = it[index].copy(name = name)
        }
    }

    fun removeStep(index: Int) {
        _steps.value = _steps.value.toMutableList().also {
            if (index in it.indices) it.removeAt(index)
        }
    }

    fun moveStep(from: Int, to: Int) {
        val list = _steps.value.toMutableList()
        if (from !in list.indices || to !in list.indices || from == to) return
        val item = list.removeAt(from)
        list.add(to, item)
        _steps.value = list
    }

    fun saveRoutine() {
        val routine = RoutineEntity(
            id = navKey.routineId ?: 0,
            name = routineName.value,
            description = routineDescription.value,
            color = _selectedColor.value,
            streakType = _streakType.value,
            frequency = _frequency.value ?: 5.0,
            cycle = _cycle.value ?: 7
        )
        val drafts = _steps.value.filter { it.name.isNotBlank() }
        viewModelScope.launch(Dispatchers.IO) {
            if (navKey.routineId != null) {
                routineRepository.editRoutine(routine)
                routineRepository.setStepsForRoutine(routine.id, drafts.mapIndexed { idx, d ->
                    RoutineStepEntity(id = d.id, routineId = routine.id, name = d.name.trim(), description = d.description, position = idx)
                })
            } else {
                val newId = routineRepository.addRoutine(routine).toInt()
                routineRepository.setStepsForRoutine(newId, drafts.mapIndexed { idx, d ->
                    RoutineStepEntity(routineId = newId, name = d.name.trim(), description = d.description, position = idx)
                })
            }
        }
    }

    private fun getRoutineDetails() {
        if (navKey.routineId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val r = routineRepository.getRoutineById(navKey.routineId)
                _existingRoutine.value = r
                _routineName.value = r.name
                _routineDescription.value = r.description
                _selectedColor.value = r.color
                _frequency.value = r.frequency
                _cycle.value = r.cycle
                _streakType.value = r.streakType
                _steps.value = routineRepository.getStepsForRoutine(r.id).map {
                    StepDraft(id = it.id, name = it.name, description = it.description)
                }
            }
        } else {
            _frequency.value = 5.0
            _cycle.value = 7
        }
    }
}
