package com.zavedahmad.yaHabit.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "habit_reminders"
        const val CHANNEL_NAME = "Habit Reminders"
    }

    fun createChannel() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for habits that are still due"
            }
            nm.createNotificationChannel(channel)
        }
    }
}
