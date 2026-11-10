package ir.hajmousa.habits.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import ir.hajmousa.habits.R
import ir.hajmousa.habits.core.date.DayClock
import ir.hajmousa.habits.data.db.AppDatabase
import ir.hajmousa.habits.data.settings.SettingsStore
import ir.hajmousa.habits.domain.habit.CompletionRules
import ir.hajmousa.habits.domain.habit.ScheduleResolver
import ir.hajmousa.habits.domain.reminder.ReminderRules
import ir.hajmousa.habits.data.repo.toModel
import java.time.Clock
import java.time.ZoneId
import kotlinx.coroutines.CancellationException

class ReminderScheduler(
    private val context: Context,
    private val database: AppDatabase,
    private val dayClock: DayClock,
    private val settings: SettingsStore,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val notifications = NotificationHelper(context)
    private val zone: ZoneId get() = dayClock.zoneId()

    suspend fun scheduleAll(showMissed: Boolean = false) {
        val now = clock.millis()
        val today = dayClock.today().toEpochDay()
        val tasks = database.taskDao().allTasks()
        tasks.forEach { task ->
            val minute = task.dueMinuteOfDay
            if (minute == null) return@forEach
            val alarm = ReminderAlarm(ReminderKind.TASK, task.id, minute, task.dueEpochDay ?: today, task.dueEpochDay?.let {
                ReminderRules.calendarDayTriggerMillis(it, minute, zone)
            } ?: 0L)
            if (!task.reminderEnabled || task.isDone || task.dueEpochDay == null) {
                cancel(alarm)
            } else if (alarm.triggerEpochMillis > now) {
                schedule(alarm)
            } else {
                if (showMissed && ReminderRules.withinMissedWindow(now, alarm.triggerEpochMillis)) deliver(alarm, now)
                cancel(alarm)
            }
        }

        val habits = database.habitDao().allHabits().associateBy { it.id }
        val versionRows = habits.values.associate { habit ->
            habit.id to database.habitDao().versions(habit.id).map { it.toModel() }
        }
        database.habitDao().allReminders().forEach { reminder ->
            val habit = habits[reminder.habitId]
            val habitVersions = versionRows[reminder.habitId].orEmpty()
            val alarmKey = ReminderAlarm(ReminderKind.HABIT, reminder.habitId, reminder.minuteOfDay, today, 0L)
            if (habit == null || habit.archived || habitVersions.isEmpty()) {
                cancel(alarmKey)
                return@forEach
            }
            if (showMissed) {
                for (missedDay in today downTo today - 1) {
                    if (!ScheduleResolver.isScheduled(missedDay, habitVersions)) continue
                    val due = ReminderRules.effectiveDayTriggerMillis(missedDay, reminder.minuteOfDay, dayClock.dayStartHour, zone)
                    if (ReminderRules.withinMissedWindow(now, due)) {
                        deliver(alarmKey.copy(effectiveEpochDay = missedDay, triggerEpochMillis = due), now)
                        break
                    }
                }
            }
            var fromDay = today
            var next = ReminderRules.nextHabitOccurrence(fromDay, reminder.minuteOfDay, dayClock.dayStartHour, zone, now, habitVersions)
            while (next != null && next.effectiveEpochDay == today) {
                val version = ScheduleResolver.versionOn(habitVersions, today)
                val log = database.habitDao().findLog(reminder.habitId, today)
                if (log == null || (!log.skipped && !CompletionRules.isCompleted(log.toModel(), version?.targetValue ?: 1))) break
                fromDay = today + 1
                next = ReminderRules.nextHabitOccurrence(fromDay, reminder.minuteOfDay, dayClock.dayStartHour, zone, now, habitVersions)
            }
            if (next == null) cancel(alarmKey) else schedule(alarmKey.copy(effectiveEpochDay = next.effectiveEpochDay, triggerEpochMillis = next.triggerEpochMillis))
        }

        val backupKey = ReminderAlarm(ReminderKind.BACKUP, 0, settings.getWeeklyBackupMinute(), today, 0L)
        if (!settings.isWeeklyBackupEnabled()) {
            cancel(backupKey)
        } else {
            if (showMissed) {
                for (missedDay in today downTo today - 1) {
                    if (ReminderRules.nextFridayOnOrAfter(missedDay) != missedDay) continue
                    val missedAt = ReminderRules.effectiveDayTriggerMillis(missedDay, backupKey.minuteOfDay, dayClock.dayStartHour, zone)
                    if (ReminderRules.withinMissedWindow(now, missedAt)) {
                        deliver(backupKey.copy(effectiveEpochDay = missedDay, triggerEpochMillis = missedAt), now)
                        break
                    }
                }
            }
            val friday = ReminderRules.nextFridayOnOrAfter(today)
            val fridayAt = ReminderRules.effectiveDayTriggerMillis(friday, backupKey.minuteOfDay, dayClock.dayStartHour, zone)
            val nextDay = if (fridayAt > now) friday else friday + 7
            val nextAt = ReminderRules.effectiveDayTriggerMillis(nextDay, backupKey.minuteOfDay, dayClock.dayStartHour, zone)
            schedule(backupKey.copy(effectiveEpochDay = nextDay, triggerEpochMillis = nextAt))
        }
    }

    suspend fun onAlarm(alarm: ReminderAlarm) {
        deliver(alarm, clock.millis())
        if (!alarm.oneShot) scheduleAll()
    }

    fun scheduleSnooze(original: ReminderAlarm) {
        val trigger = clock.millis() + 10 * 60 * 1000L
        schedule(original.copy(triggerEpochMillis = trigger, oneShot = true))
    }

    fun cancelTask(id: Long, minuteOfDay: Int?) {
        if (minuteOfDay != null) cancel(ReminderAlarm(ReminderKind.TASK, id, minuteOfDay, 0, 0))
    }

    fun cancelHabit(id: Long, minutes: List<Int>) = minutes.forEach { minute ->
        cancel(ReminderAlarm(ReminderKind.HABIT, id, minute, 0, 0))
    }

    fun cancelBackup(minuteOfDay: Int) = cancel(ReminderAlarm(ReminderKind.BACKUP, 0, minuteOfDay, 0, 0))

    private suspend fun deliver(alarm: ReminderAlarm, now: Long): Boolean {
        if (!ReminderRules.withinMissedWindow(now, alarm.triggerEpochMillis)) return false
        val title: String
        val message: String
        when (alarm.kind) {
            ReminderKind.TASK -> {
                val task = database.taskDao().find(alarm.id) ?: return false
                if (!ReminderRules.shouldNotifyTask(task.isDone, task.reminderEnabled, task.dueEpochDay, task.dueMinuteOfDay, alarm.effectiveEpochDay, alarm.minuteOfDay)) return false
                title = context.getString(R.string.task_notification_title)
                message = context.getString(R.string.task_notification_text, task.title)
            }
            ReminderKind.HABIT -> {
                if (!database.habitDao().hasReminder(alarm.id, alarm.minuteOfDay)) return false
                val habit = database.habitDao().find(alarm.id) ?: return false
                val versions = database.habitDao().versions(alarm.id).map { it.toModel() }
                val scheduled = ScheduleResolver.isScheduled(alarm.effectiveEpochDay, versions)
                val version = ScheduleResolver.versionOn(versions, alarm.effectiveEpochDay) ?: return false
                val log = database.habitDao().findLog(alarm.id, alarm.effectiveEpochDay)?.toModel()
                if (!ReminderRules.shouldNotifyHabit(habit.archived, scheduled, log, version.targetValue)) return false
                title = context.getString(R.string.habit_notification_title)
                message = context.getString(R.string.habit_notification_text, habit.name)
            }
            ReminderKind.BACKUP -> {
                if (!settings.isWeeklyBackupEnabled() || alarm.minuteOfDay != settings.getWeeklyBackupMinute()) return false
                title = context.getString(R.string.backup_notification_title)
                message = context.getString(R.string.backup_notification_title)
            }
        }
        val occurrenceToken = if (alarm.oneShot) alarm.triggerEpochMillis else alarm.effectiveEpochDay
        val deliveryKey = if (alarm.oneShot) "${alarm.key}_snooze" else alarm.key
        if (!settings.claimDelivery(deliveryKey, occurrenceToken)) return false
        return try {
            val shown = notifications.show(alarm, title, message)
            if (!shown) settings.releaseDelivery(deliveryKey, occurrenceToken)
            shown
        } catch (error: Exception) {
            settings.releaseDelivery(deliveryKey, occurrenceToken)
            if (error is CancellationException) throw error
            Log.w(TAG, "Notification permission is unavailable", error)
            false
        }
    }

    private fun schedule(alarm: ReminderAlarm) {
        val pendingIntent = alarmPendingIntent(alarm, PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        val exactAllowed = Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms()
        if (!exactAllowed) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarm.triggerEpochMillis, pendingIntent)
            return
        }
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarm.triggerEpochMillis, pendingIntent)
        } catch (error: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarm.triggerEpochMillis, pendingIntent)
        }
    }

    private fun cancel(alarm: ReminderAlarm) {
        val pendingIntent = alarmPendingIntent(alarm, PendingIntent.FLAG_NO_CREATE) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun alarmPendingIntent(alarm: ReminderAlarm, mode: Int): PendingIntent? {
        val uri = "karha://alarm/${alarm.key}${if (alarm.oneShot) "/snooze" else ""}"
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ReminderContract.ACTION
            data = Uri.parse(uri)
            putExtra(ReminderContract.EXTRA_KIND, alarm.kind.name)
            putExtra(ReminderContract.EXTRA_ID, alarm.id)
            putExtra(ReminderContract.EXTRA_MINUTE, alarm.minuteOfDay)
            putExtra(ReminderContract.EXTRA_DAY, alarm.effectiveEpochDay)
            putExtra(ReminderContract.EXTRA_TRIGGER, alarm.triggerEpochMillis)
            putExtra(ReminderContract.EXTRA_ONE_SHOT, alarm.oneShot)
            putExtra(ReminderContract.EXTRA_NOTIFICATION_ID, alarm.notificationId)
        }
        val flags = mode or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    companion object {
        private const val TAG = "KarHaReminder"
    }
}
