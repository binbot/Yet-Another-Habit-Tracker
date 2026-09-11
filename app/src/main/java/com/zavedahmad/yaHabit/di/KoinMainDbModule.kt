package com.zavedahmad.yaHabit.di

import androidx.room.Room
import com.zavedahmad.yaHabit.Screen
import com.zavedahmad.yaHabit.database.MainDatabase
import com.zavedahmad.yaHabit.database.repositories.ImportExportRepository
import com.zavedahmad.yaHabit.database.repositories.PreferencesRepository
import com.zavedahmad.yaHabit.ui.aboutPage.AboutPageViewModel
import com.zavedahmad.yaHabit.ui.addHabitPage.AddHabitPageViewModel
import com.zavedahmad.yaHabit.ui.habitsDetailPage.HabitDetailsPageViewModel
import com.zavedahmad.yaHabit.ui.mainPage.MainPageViewModel
import com.zavedahmad.yaHabit.ui.settingsScreen.SettingsViewModel
import com.zavedahmad.yaHabit.database.HabitReminderScheduler
import com.zavedahmad.yaHabit.database.repositories.RoutineRepository
import com.zavedahmad.yaHabit.database.repositories.RoutineRepositoryImpl
import com.zavedahmad.yaHabit.notification.AlarmScheduler
import com.zavedahmad.yaHabit.notification.NotificationHelper
import com.zavedahmad.yaHabit.widgets.overviewWidget.HabitWidgetRepository
import com.zavedahmad.yaHabit.widgets.overviewWidget.WidgetUpdaterImpl
import com.zavedahmad.yahabit.common.WidgetUpdater
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val mainDBModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = MainDatabase::class.java,
            name = "main_database"
        ).build()
    }
    singleOf(::HabitWidgetRepository)
    singleOf(::WidgetUpdaterImpl).bind<WidgetUpdater>()
    single { get<MainDatabase>().preferencesDao() }
    single { get<MainDatabase>().habitDao() }
    single { get<MainDatabase>().habitCompletionDao() }
    single { get<MainDatabase>().routineDao() }
    single { get<MainDatabase>().routineItemDao() }
    single { NotificationHelper(androidContext()) }
    single { AlarmScheduler(androidContext(), get(), get(), get()) }
    single<HabitReminderScheduler> { get<AlarmScheduler>() }
    single { PreferencesRepository(get(), get()) }
    single { ImportExportRepository(
        habitDao = get(),
        context = androidContext(),
        databaseUtils = get()
    ) }
    singleOf(::RoutineRepositoryImpl).bind<RoutineRepository>()
    viewModel { MainPageViewModel(get(), get(), get()) }

    viewModel { (navKey: Screen.AddHabitPageRoute) ->
        AddHabitPageViewModel(
            navKey = navKey,
            habitDao = get(),
            habitRepositoryImpl = get(),
            preferencesDao = get(),
            preferencesRepository = get()
        )
    }
    viewModel { (navKey: Screen.HabitDetailsPageRoute) ->
        HabitDetailsPageViewModel(
            navKey = navKey,
            habitCompletionDao = get(),
            habitDao = get(),
            preferencesDao = get(),
            habitRepository = get(),
            preferencesRepository = get()
        )
    }
    viewModel {
        SettingsViewModel(
            preferencesDao = get(),
            preferencesRepository = get(),
            importExportRepository = get()
        )
    }
    viewModel {
        AboutPageViewModel(
            preferencesRepository = get()
        )
    }
    viewModel { (navKey: com.zavedahmad.yaHabit.Screen.AddRoutinePageRoute) ->
        com.zavedahmad.yaHabit.ui.addRoutinePage.AddRoutinePageViewModel(
            navKey = navKey,
            routineRepository = get(),
            habitDao = get()
        )
    }

}