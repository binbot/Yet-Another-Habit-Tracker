package com.zavedahmad.yaHabit.di

import android.app.Application

import com.zavedahmad.yaHabit.notification.NotificationHelper
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin


class MainApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(databaseUtilsModule, mainDBModule, habitRepositoryModule)

        }
        // Create notification channel early (minSdk 26, no compat branching needed)
        get().get<NotificationHelper>().createChannel()
    }
}