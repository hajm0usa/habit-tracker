package ir.hajmousa.habits.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import ir.hajmousa.habits.MainActivity
import ir.hajmousa.habits.R

class NotificationHelper(private val context: Context) {
    fun createChannels() {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(CHANNEL_HABITS, context.getString(R.string.habits), NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_TASKS, context.getString(R.string.tasks), NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_BACKUP, context.getString(R.string.backup_notification_title), NotificationManager.IMPORTANCE_DEFAULT)
            )
        )
    }

    fun show(alarm: ReminderAlarm, title: String, message: String): Boolean {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        if (!notificationManager.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        if (Build.VERSION.SDK_INT >= 26 && context.getSystemService(NotificationManager::class.java)
                .getNotificationChannel(channel(alarm.kind))?.importance == NotificationManager.IMPORTANCE_NONE
        ) return false
        val key = "${alarm.key}_${alarm.triggerEpochMillis}"
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = Notification.Builder(context, channel(alarm.kind))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_REMINDER)

        if (alarm.kind != ReminderKind.BACKUP) {
            builder.addAction(action(android.R.drawable.ic_menu_save, context.getString(R.string.action_done), alarm, key, ReminderContract.ACTION_DONE))
        }
        builder.addAction(action(android.R.drawable.ic_lock_idle_alarm, context.getString(R.string.snooze_10_minutes), alarm, key, ReminderContract.ACTION_SNOOZE))
        notificationManager.notify(alarm.notificationId, builder.build())
        return true
    }

    fun cancel(notificationId: Int) = context.getSystemService(NotificationManager::class.java).cancel(notificationId)

    private fun action(icon: Int, label: String, alarm: ReminderAlarm, key: String, action: String): Notification.Action {
        val intent = Intent(context, ActionReceiver::class.java).apply {
            this.action = action
            data = Uri.parse("karha://action/$action/$key")
            putExtra(ReminderContract.EXTRA_KIND, alarm.kind.name)
            putExtra(ReminderContract.EXTRA_ID, alarm.id)
            putExtra(ReminderContract.EXTRA_MINUTE, alarm.minuteOfDay)
            putExtra(ReminderContract.EXTRA_DAY, alarm.effectiveEpochDay)
            putExtra(ReminderContract.EXTRA_TRIGGER, alarm.triggerEpochMillis)
            putExtra(ReminderContract.EXTRA_ONE_SHOT, alarm.oneShot)
            putExtra(ReminderContract.EXTRA_NOTIFICATION_ID, alarm.notificationId)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            (action + key).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Action.Builder(icon, label, pending).build()
    }

    private fun channel(kind: ReminderKind) = when (kind) {
        ReminderKind.HABIT -> CHANNEL_HABITS
        ReminderKind.TASK -> CHANNEL_TASKS
        ReminderKind.BACKUP -> CHANNEL_BACKUP
    }

    companion object {
        const val CHANNEL_HABITS = "habit_reminders"
        const val CHANNEL_TASKS = "task_reminders"
        const val CHANNEL_BACKUP = "backup_reminders"
    }
}
