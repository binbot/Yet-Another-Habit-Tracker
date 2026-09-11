package com.zavedahmad.yaHabit.ui.addRoutinePage

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.zavedahmad.yahabit.common.HabitColors
import androidx.lifecycle.viewModelScope
import com.zavedahmad.yaHabit.Screen
import com.zavedahmad.yaHabit.database.daos.HabitDao
import com.zavedahmad.yaHabit.database.entities.HabitEntity
import com.zavedahmad.yaHabit.database.entities.RoutineEntity
import com.zavedahmad.yaHabit.database.enums.HabitStreakType
import com.zavedahmad.yaHabit.database.repositories.RoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    private val _selectedHabitIds = MutableStateFlow<List<Int>>(emptyList())
    val selectedHabitIds = _selectedHabitIds.asStateFlow()
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
    fun toggleHabitSelection(id: Int) {
        _selectedHabitIds.value = if (id in _selectedHabitIds.value) _selectedHabitIds.value - id else _selectedHabitIds.value + id
    }
    fun setSelectedHabits(ids: List<Int>) { _selectedHabitIds.value = ids }

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
        viewModelScope.launch(Dispatchers.IO) {
            if (navKey.routineId != null) routineRepository.editRoutine(routine, _selectedHabitIds.value)
            else routineRepository.addRoutine(routine, _selectedHabitIds.value)
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
                _selectedHabitIds.value = routineRepository.getItemsForRoutine(r.id).map { it.habitId }
            }
        } else {
            _frequency.value = 5.0
            _cycle.value = 7
        }
    }
}
