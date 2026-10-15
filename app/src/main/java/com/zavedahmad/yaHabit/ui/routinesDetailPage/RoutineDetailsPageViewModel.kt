package com.zavedahmad.yaHabit.ui.routinesDetailPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zavedahmad.yaHabit.Screen
import com.zavedahmad.yaHabit.database.entities.RoutineEntity
import com.zavedahmad.yaHabit.database.entities.RoutineStepCompletionEntity
import com.zavedahmad.yaHabit.database.entities.RoutineStepEntity
import com.zavedahmad.yaHabit.database.repositories.RoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RoutineDetailsPageViewModel(
    val navKey: Screen.RoutineDetailsPageRoute,
    val routineRepository: RoutineRepository
) : ViewModel() {

    private val _routine = MutableStateFlow<RoutineEntity?>(null)
    val routine = _routine.asStateFlow()

    private val _steps = MutableStateFlow<List<RoutineStepEntity>>(emptyList())
    val steps = _steps.asStateFlow()

    private val _completions = MutableStateFlow<List<RoutineStepCompletionEntity>>(emptyList())
    val completions = _completions.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            routineRepository.getRoutineByIdFlow(navKey.routineId).collect { _routine.value = it }
        }
        viewModelScope.launch(Dispatchers.IO) {
            routineRepository.getStepsForRoutineFlow(navKey.routineId).collect { _steps.value = it }
        }
        viewModelScope.launch(Dispatchers.IO) {
            routineRepository.getStepCompletionsForRoutineFlow(navKey.routineId).collect { _completions.value = it }
        }
    }
}
