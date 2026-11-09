package ir.hajmousa.habits.domain.habit

import ir.hajmousa.habits.domain.model.HabitLogRecord
import ir.hajmousa.habits.domain.model.HabitSchedule
import ir.hajmousa.habits.domain.model.ScheduleType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitRulesTest {
    private fun day(date: String) = LocalDate.parse(date).toEpochDay()
    private fun schedule(
        from: Long,
        type: ScheduleType = ScheduleType.DAILY,
        target: Int = 1,
        mask: Int = 0,
        interval: Int? = null,
        anchor: Long? = null
    ) = HabitSchedule(from, type, mask, interval, anchor, target, 1)

    @Test fun resolvesLatestEffectiveVersionAndNoVersionBeforeCreation() {
        val versions = listOf(schedule(10), schedule(20, target = 3))
        assertNull(ScheduleResolver.versionOn(versions, 9))
        assertEquals(1, ScheduleResolver.versionOn(versions, 19)?.targetValue)
        assertEquals(3, ScheduleResolver.versionOn(versions, 20)?.targetValue)
    }

    @Test fun weekdayMaskStartsOnSaturdayAndEveryNStaysAnchored() {
        val saturday = day("2024-03-23")
        val weekdays = schedule(saturday, ScheduleType.WEEKDAYS, mask = 1 shl 0)
        assertTrue(ScheduleResolver.isScheduled(saturday, listOf(weekdays)))
        assertFalse(ScheduleResolver.isScheduled(saturday + 1, listOf(weekdays)))
        val everyThree = schedule(saturday, ScheduleType.EVERY_N, interval = 3, anchor = saturday)
        assertTrue(ScheduleResolver.isScheduled(saturday + 6, listOf(everyThree)))
        assertFalse(ScheduleResolver.isScheduled(saturday + 7, listOf(everyThree)))
        assertFalse(ScheduleResolver.isScheduled(saturday - 1, listOf(everyThree)))
    }

    @Test fun skippedDoesNotCompleteAndOverTargetCompletes() {
        assertFalse(CompletionRules.isCompleted(HabitLogRecord(1, 10, true), 8))
        assertTrue(CompletionRules.isCompleted(HabitLogRecord(1, 10, false), 8))
        assertFalse(CompletionRules.isCompleted(null, 1))
        assertEquals(12, CompletionRules.adjustValue(10, 2, 1))
        assertEquals(0, CompletionRules.adjustValue(1, 2, -1))
        assertEquals(Int.MAX_VALUE, CompletionRules.adjustValue(Int.MAX_VALUE, 1, 1))
    }

    @Test fun currentStreakIgnoresPendingTodayAndSkippedDays() {
        val today = day("2024-03-26")
        val version = schedule(today - 5)
        val logs = (today - 3..today - 1).associateWith { HabitLogRecord(it, 1, false) } +
            (today - 4 to HabitLogRecord(today - 4, 1, true))
        assertEquals(3, StreakCalculator.current(today - 5, today, listOf(version), logs))
        assertEquals(3, StreakCalculator.current(today - 5, today, listOf(version), logs + (today to HabitLogRecord(today, 0, true))))
    }

    @Test fun partialPastDayBreaksStreakAndVersionedTargetAppliesToItsDate() {
        val today = day("2024-03-26")
        val created = today - 3
        val versions = listOf(schedule(created, target = 1), schedule(today - 1, target = 3))
        val logs = mapOf(
            created to HabitLogRecord(created, 1, false),
            today - 1 to HabitLogRecord(today - 1, 2, false)
        )
        assertEquals(0, StreakCalculator.current(created, today, versions, logs))
        assertEquals(1, StreakCalculator.best(created, today, versions, logs))
    }

    @Test fun bestStreakSurvivesScheduleGapsAndResetsOnFailedScheduledDay() {
        val today = day("2024-03-26")
        val created = today - 5
        val weekdaySchedule = schedule(created, ScheduleType.WEEKDAYS, mask = (1 shl 0) or (1 shl 2))
        val scheduled = (created..today).filter { ScheduleResolver.isScheduled(it, listOf(weekdaySchedule)) }
        val logs = scheduled.dropLast(1).associateWith { HabitLogRecord(it, 1, false) }
        assertTrue(StreakCalculator.best(created, today, listOf(weekdaySchedule), logs) >= 1)
    }
}
