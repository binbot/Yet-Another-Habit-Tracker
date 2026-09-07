package com.zavedahmad.yaHabit.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val scheduler = GlobalContext.get().get<AlarmScheduler>()
                scheduler.rescheduleAll()
            } catch (_: Exception) {
            } finally {
                pending.finish()
            }
        }
    }
}
