package ir.hajmousa.habits.domain.reminder

import ir.hajmousa.habits.domain.model.HabitSchedule
import ir.hajmousa.habits.domain.model.HabitLogRecord
import ir.hajmousa.habits.domain.model.ScheduleType
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderRulesTest {
    @Test fun missedWindowIsStrictlyLessThanThreeHours() {
        val due = 1_000_000L
        assertTrue(ReminderRules.withinMissedWindow(due + 10, due))
        assertFalse(ReminderRules.withinMissedWindow(due + 3 * 60 * 60 * 1000L, due))
        assertFalse(ReminderRules.withinMissedWindow(due - 1, due))
    }

    @Test fun habitNotificationEligibilityHonorsScheduleArchiveCompletionAndSkip() {
        assertTrue(ReminderRules.shouldNotifyHabit(false, true, null, 1))
        assertFalse(ReminderRules.shouldNotifyHabit(true, true, null, 1))
        assertFalse(ReminderRules.shouldNotifyHabit(false, false, null, 1))
        assertFalse(ReminderRules.shouldNotifyHabit(false, true, HabitLogRecord(1, 2, false), 2))
        assertFalse(ReminderRules.shouldNotifyHabit(false, true, HabitLogRecord(1, 0, true), 1))
    }

    @Test fun taskNotificationEligibilityRequiresCurrentDueReminder() {
        assertTrue(ReminderRules.shouldNotifyTask(false, true, 10, 540, 10, 540))
        assertFalse(ReminderRules.shouldNotifyTask(true, true, 10, 540, 10, 540))
        assertFalse(ReminderRules.shouldNotifyTask(false, false, 10, 540, 10, 540))
        assertFalse(ReminderRules.shouldNotifyTask(false, true, 11, 540, 10, 540))
        assertFalse(ReminderRules.shouldNotifyTask(false, true, 10, 600, 10, 540))
    }

    @Test fun effectiveDayTimeBeforeDayStartBelongsToNextCalendarDay() {
        val day = LocalDate.parse("2026-11-09").toEpochDay()
        val zone = ZoneId.of("Asia/Tehran")
        val expected = LocalDate.parse("2026-11-10").atStartOfDay(zone).plusHours(2).toInstant().toEpochMilli()
        assertEquals(expected, ReminderRules.effectiveDayTriggerMillis(day, 120, 4, zone))
    }

    @Test fun nextOccurrenceUsesScheduleAndReturnsOnlyFutureTime() {
        val day = LocalDate.parse("2024-03-23").toEpochDay()
        val versions = listOf(HabitSchedule(day, ScheduleType.EVERY_N, 0, 3, day, 1, 1))
        val zone = ZoneId.of("UTC")
        val now = ReminderRules.effectiveDayTriggerMillis(day, 9 * 60, 0, zone)
        val next = ReminderRules.nextHabitOccurrence(day, 9 * 60, 0, zone, now, versions)
        assertEquals(day + 3, next?.effectiveEpochDay)
        assertEquals(now + 3 * 24 * 60 * 60 * 1000L, next?.triggerEpochMillis)
    }

    @Test fun weeklyBackupDayIsFridayWithSaturdayFirstWeek() {
        val saturday = LocalDate.parse("2024-03-23").toEpochDay()
        assertEquals(saturday + 6, ReminderRules.nextFridayOnOrAfter(saturday))
        assertEquals(saturday + 6, ReminderRules.nextFridayOnOrAfter(saturday + 6))
        assertEquals(saturday + 13, ReminderRules.nextFridayOnOrAfter(saturday + 7))
    }
}
