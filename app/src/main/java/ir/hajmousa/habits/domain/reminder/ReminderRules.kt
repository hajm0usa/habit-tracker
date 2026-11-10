package ir.hajmousa.habits.domain.reminder

import ir.hajmousa.habits.domain.habit.ScheduleResolver
import ir.hajmousa.habits.domain.habit.CompletionRules
import ir.hajmousa.habits.domain.model.HabitLogRecord
import ir.hajmousa.habits.domain.model.HabitSchedule
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class ReminderOccurrence(val effectiveEpochDay: Long, val triggerEpochMillis: Long)

object ReminderRules {
    private const val MISSED_WINDOW_MILLIS = 3 * 60 * 60 * 1000L

    fun withinMissedWindow(nowEpochMillis: Long, dueEpochMillis: Long): Boolean =
        nowEpochMillis >= dueEpochMillis && nowEpochMillis - dueEpochMillis < MISSED_WINDOW_MILLIS

    fun shouldNotifyHabit(archived: Boolean, scheduled: Boolean, log: HabitLogRecord?, targetValue: Int): Boolean =
        !archived && scheduled && log?.skipped != true && !CompletionRules.isCompleted(log, targetValue)

    fun shouldNotifyTask(done: Boolean, enabled: Boolean, dueEpochDay: Long?, dueMinuteOfDay: Int?, occurrenceDay: Long, occurrenceMinute: Int): Boolean =
        !done && enabled && dueEpochDay == occurrenceDay && dueMinuteOfDay == occurrenceMinute

    fun effectiveDayTriggerMillis(epochDay: Long, minuteOfDay: Int, dayStartHour: Int, zone: ZoneId): Long {
        require(minuteOfDay in 0..1439 && dayStartHour in 0..6)
        val actualDay = if (minuteOfDay < dayStartHour * 60) epochDay + 1 else epochDay
        return LocalDate.ofEpochDay(actualDay)
            .atTime(LocalTime.of(minuteOfDay / 60, minuteOfDay % 60))
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
    }

    fun nextHabitOccurrence(
        fromEffectiveEpochDay: Long,
        minuteOfDay: Int,
        dayStartHour: Int,
        zone: ZoneId,
        nowEpochMillis: Long,
        versions: List<HabitSchedule>
    ): ReminderOccurrence? {
        var day = ScheduleResolver.nextScheduledDay(fromEffectiveEpochDay, versions) ?: return null
        while (true) {
            val trigger = effectiveDayTriggerMillis(day, minuteOfDay, dayStartHour, zone)
            if (trigger > nowEpochMillis) return ReminderOccurrence(day, trigger)
            day = ScheduleResolver.nextScheduledDay(day + 1, versions) ?: return null
        }
    }

    fun calendarDayTriggerMillis(epochDay: Long, minuteOfDay: Int, zone: ZoneId): Long {
        require(minuteOfDay in 0..1439)
        return LocalDate.ofEpochDay(epochDay)
            .atTime(LocalTime.of(minuteOfDay / 60, minuteOfDay % 60))
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
    }

    fun nextFridayOnOrAfter(epochDay: Long): Long {
        var day = epochDay
        while ((LocalDate.ofEpochDay(day).dayOfWeek.value + 1) % 7 != 6) day++
        return day
    }
}
