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

class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarm = intent.toReminderAlarm() ?: return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val app = context.applicationContext as HabitsApp
            val notifications = NotificationHelper(context)
            try {
                app.container.dayClock.setDayStartHour(app.container.settings.dayStartHour.first())
                when (intent.action) {
                    ReminderContract.ACTION_DONE -> when (alarm.kind) {
                        ReminderKind.TASK -> app.container.tasks.setDone(alarm.id, true)
                        ReminderKind.HABIT -> app.container.habits.completeFromReminder(alarm.id, alarm.effectiveEpochDay)
                        ReminderKind.BACKUP -> return@launch
                    }
                    ReminderContract.ACTION_SNOOZE -> app.container.reminders.scheduleSnooze(alarm)
                    else -> return@launch
                }
                notifications.cancel(alarm.notificationId)
                if (intent.action == ReminderContract.ACTION_DONE) {
                    app.container.reminders.scheduleAll()
                    ir.hajmousa.habits.feature.widget.WidgetUpdater.update(context)
                }
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                Log.e(TAG, "Reminder action failed", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object { private const val TAG = "KarHaAction" }
}
