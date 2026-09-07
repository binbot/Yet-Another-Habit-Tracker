package com.zavedahmad.yaHabit.ui.settingsScreen

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zavedahmad.yaHabit.database.HabitReminderScheduler
import com.zavedahmad.yaHabit.database.PreferenceEntity
import com.zavedahmad.yaHabit.database.daos.PreferencesDao
import com.zavedahmad.yaHabit.database.repositories.ImportExportRepository
import com.zavedahmad.yaHabit.database.repositories.PreferencesRepository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek



class SettingsViewModel (
    val preferencesDao: PreferencesDao,
    val preferencesRepository: PreferencesRepository,
    val importExportRepository: ImportExportRepository,
    val reminderScheduler: HabitReminderScheduler? = null
) : ViewModel() {
    private fun resolveScheduler(): HabitReminderScheduler? {
        reminderScheduler?.let { return it }
        return try { org.koin.core.context.GlobalContext.get().get<HabitReminderScheduler>() } catch (_: Exception) { null }
    }


    private val _themeMode = MutableStateFlow<PreferenceEntity?>(null)
    val themeMode = _themeMode.asStateFlow()

    private val _dynamicColor = MutableStateFlow<PreferenceEntity?>(null)
    val dynamicColor = _dynamicColor.asStateFlow()

    private val _amoledTheme = MutableStateFlow<PreferenceEntity?>(null)
    val amoledTheme = _amoledTheme.asStateFlow()

    private val _firstDayOfWeek = MutableStateFlow<DayOfWeek?>(null)
    val firstDayOfWeek = _firstDayOfWeek.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow<PreferenceEntity?>(null)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()
    private val _defaultReminderHour = MutableStateFlow<PreferenceEntity?>(null)
    val defaultReminderHour = _defaultReminderHour.asStateFlow()
    private val _defaultReminderMinute = MutableStateFlow<PreferenceEntity?>(null)
    val defaultReminderMinute = _defaultReminderMinute.asStateFlow()

    init {

        collectThemeMode()
        collectDynamicColor()
        collectAmoledTheme()
        collectFirstDayOfWeek()
        collectNotificationsEnabled()
        collectDefaultReminderHour()
        collectDefaultReminderMinute()
    }
    fun exportDatabase(context: Context, uri: Uri, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            importExportRepository.exportDatabase(uri =  uri , onComplete = onComplete)
        }
    }

    fun importDatabase(context: Context, uri: Uri, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            importExportRepository.importDatabase(uri = uri , onComplete = onComplete)
        }
    }

    fun setFirstWeekOfDay(dayOfWeek: DayOfWeek) {
        viewModelScope.launch {
            preferencesRepository.setFirstDayOfWeek(dayOfWeek)
        }
    }
    fun collectFirstDayOfWeek(){
        viewModelScope.launch { preferencesRepository.getFirstDayOfWeekFlow().collect { _firstDayOfWeek.value = it } }

    }
    fun setTheme(value: String) {
        viewModelScope.launch {
            preferencesDao.updatePreference(PreferenceEntity("ThemeMode", value))
        }

    }

    fun collectThemeMode() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesDao.getPreferenceFlow("ThemeMode").collect { preference ->
                _themeMode.value = preference ?: PreferenceEntity("ThemeMode", "system")
            }
        }
    }

    fun setDynamicColor(value: String) {
        viewModelScope.launch {
            preferencesDao.updatePreference(PreferenceEntity("DynamicColor", value))
        }
    }

    fun collectDynamicColor() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesDao.getPreferenceFlow("DynamicColor").collect { preference ->
                _dynamicColor.value = preference ?: PreferenceEntity("DynamicColor", "false")
            }
        }
    }

    fun setAmoledTheme(value: String) {
        viewModelScope.launch {
            preferencesDao.updatePreference(PreferenceEntity("AmoledTheme", value))
        }
    }

    fun collectAmoledTheme() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesDao.getPreferenceFlow("AmoledTheme").collect { preference ->
                _amoledTheme.value = preference ?: PreferenceEntity("AmoledTheme", "false")
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDao.updatePreference(PreferenceEntity("notificationsEnabled", enabled.toString()))
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (enabled) resolveScheduler()?.rescheduleAll() else {
                    // Will cancel via rescheduleAll when disabled check
                    resolveScheduler()?.rescheduleAll()
                }
            } catch (_: Exception) {}
        }
    }

    fun collectNotificationsEnabled() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesDao.getPreferenceFlow("notificationsEnabled").collect { pref ->
                _notificationsEnabled.value = pref ?: PreferenceEntity("notificationsEnabled", "false")
            }
        }
    }

    fun setDefaultReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesDao.updatePreference(PreferenceEntity("defaultReminderHour", hour.toString()))
            preferencesDao.updatePreference(PreferenceEntity("defaultReminderMinute", minute.toString()))
        }
        // Reschedule to apply new default to future habits? Existing habits keep their own time.
        viewModelScope.launch(Dispatchers.IO) {
            try { resolveScheduler()?.rescheduleAll() } catch (_: Exception) {}
        }
    }

    fun collectDefaultReminderHour() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesDao.getPreferenceFlow("defaultReminderHour").collect { pref ->
                _defaultReminderHour.value = pref ?: PreferenceEntity("defaultReminderHour", "9")
            }
        }
    }

    fun collectDefaultReminderMinute() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesDao.getPreferenceFlow("defaultReminderMinute").collect { pref ->
                _defaultReminderMinute.value = pref ?: PreferenceEntity("defaultReminderMinute", "0")
            }
        }
    }

}