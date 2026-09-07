package com.zavedahmad.yaHabit.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Stub for step/notify-channel-perms: rescheduling will be added in scheduler step.
        // No-op ensures manifest-registered receiver is valid and does not crash on BOOT_COMPLETED.
    }
}
