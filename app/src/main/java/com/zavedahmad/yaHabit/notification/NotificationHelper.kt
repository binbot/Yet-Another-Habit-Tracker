package com.zavedahmad.yaHabit.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.zavedahmad.yaHabit.MainActivity
import com.zavedahmad.yaHabit.database.entities.HabitEntity

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "habit_reminders"
        const val CHANNEL_NAME = "Habit Reminders"
        const val GROUP_KEY = "habit_reminders_group"
        const val SUMMARY_ID = 0
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

    fun showHabitReminder(habit: HabitEntity) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Respect DND: if notifications are blocked at channel level, skip silently
        if (nm.getNotificationChannel(CHANNEL_ID)?.importance == NotificationManager.IMPORTANCE_NONE) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            context,
            habit.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.zavedahmad.yaHabit.R.mipmap.ic_launcher)
            .setColor(habit.color.toArgb())
            .setColorized(true)
            .setContentTitle(habit.name)
            .setContentText("Still due today — tap to open")
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY)
            .build()
        nm.notify(habit.id, notification)

        // Group summary when >3 habit reminders are active (polish spec)
        try {
            val active = nm.activeNotifications.filter { it.notification.group == GROUP_KEY && it.id != SUMMARY_ID }
            if (active.size > 3) {
                showSummaryIfNeeded(active.size)
            } else if (active.size <= 1) {
                nm.cancel(SUMMARY_ID)
            }
        } catch (_: Exception) {
            // activeNotifications may throw on some OEMs; ignore
        }
    }

    fun showSummaryIfNeeded(count: Int) {
        if (count <= 1) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            context,
            SUMMARY_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val summary = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.zavedahmad.yaHabit.R.mipmap.ic_launcher)
            .setContentTitle("Habits due")
            .setContentText("$count habits still due today")
            .setStyle(NotificationCompat.InboxStyle().setSummaryText("$count habits"))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .build()
        nm.notify(SUMMARY_ID, summary)
    }

    fun cancel(habitId: Int) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(habitId)
        try {
            val remaining = nm.activeNotifications.filter { it.notification.group == GROUP_KEY && it.id != SUMMARY_ID }
            if (remaining.size <= 1) nm.cancel(SUMMARY_ID)
            else if (remaining.size > 3) showSummaryIfNeeded(remaining.size)
        } catch (_: Exception) {}
    }

    fun cancelAll() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancelAll()
    }
}
