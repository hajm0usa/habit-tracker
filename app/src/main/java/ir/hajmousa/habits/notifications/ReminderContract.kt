package ir.hajmousa.habits.notifications

import java.util.Locale

enum class ReminderKind { TASK, HABIT, BACKUP }

data class ReminderAlarm(
    val kind: ReminderKind,
    val id: Long,
    val minuteOfDay: Int,
    val effectiveEpochDay: Long,
    val triggerEpochMillis: Long,
    val oneShot: Boolean = false,
    val notificationId: Int = reminderNotificationId(kind, id, minuteOfDay)
) {
    val key: String get() = "${kind.name.lowercase(Locale.ROOT)}_${id}_$minuteOfDay"
}

object ReminderContract {
    const val ACTION = "ir.hajmousa.habits.REMINDER_ALARM"
    const val EXTRA_KIND = "kind"
    const val EXTRA_ID = "id"
    const val EXTRA_MINUTE = "minute"
    const val EXTRA_DAY = "day"
    const val EXTRA_TRIGGER = "trigger"
    const val EXTRA_ONE_SHOT = "one_shot"
    const val EXTRA_NOTIFICATION_ID = "notification_id"
    const val ACTION_DONE = "ir.hajmousa.habits.REMINDER_DONE"
    const val ACTION_SNOOZE = "ir.hajmousa.habits.REMINDER_SNOOZE"
}

fun reminderNotificationId(kind: ReminderKind, id: Long, minuteOfDay: Int): Int =
    "${kind.name}:$id:$minuteOfDay".hashCode().and(Int.MAX_VALUE).coerceAtLeast(1)
