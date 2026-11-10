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

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarm = intent.toReminderAlarm() ?: return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                (context.applicationContext as HabitsApp).container.reminders.onAlarm(alarm)
            } catch (error: Exception) {
                Log.e(TAG, "Unable to handle reminder alarm", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object { private const val TAG = "KarHaAlarm" }
}

internal fun Intent.toReminderAlarm(): ReminderAlarm? = runCatching {
    ReminderAlarm(
        kind = ReminderKind.valueOf(getStringExtra(ReminderContract.EXTRA_KIND) ?: return null),
        id = getLongExtra(ReminderContract.EXTRA_ID, -1).takeIf { it >= 0 } ?: return null,
        minuteOfDay = getIntExtra(ReminderContract.EXTRA_MINUTE, -1).takeIf { it in 0..1439 } ?: return null,
        effectiveEpochDay = getLongExtra(ReminderContract.EXTRA_DAY, Long.MIN_VALUE).takeIf { it != Long.MIN_VALUE } ?: return null,
        triggerEpochMillis = getLongExtra(ReminderContract.EXTRA_TRIGGER, -1).takeIf { it >= 0 } ?: return null,
        oneShot = getBooleanExtra(ReminderContract.EXTRA_ONE_SHOT, false),
        notificationId = getIntExtra(ReminderContract.EXTRA_NOTIFICATION_ID, 0)
    )
}.getOrNull()
