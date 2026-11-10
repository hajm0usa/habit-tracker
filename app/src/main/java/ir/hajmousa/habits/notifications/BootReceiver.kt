package ir.hajmousa.habits.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import ir.hajmousa.habits.HabitsApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val missedCheck = intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val container = (context.applicationContext as HabitsApp).container
                container.dayClock.setDayStartHour(container.settings.dayStartHour.first())
                container.reminders.scheduleAll(showMissed = missedCheck)
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                Log.e(TAG, "Unable to reschedule reminders", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object { private const val TAG = "KarHaBoot" }
}
