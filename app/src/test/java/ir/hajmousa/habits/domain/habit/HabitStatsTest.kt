package ir.hajmousa.habits.domain.habit

import ir.hajmousa.habits.domain.model.HabitLogRecord
import ir.hajmousa.habits.domain.model.HabitSchedule
import ir.hajmousa.habits.domain.model.ScheduleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HabitStatsTest {
    private val daily = listOf(HabitSchedule(0, ScheduleType.DAILY, 0, null, null, 1, 1))

    @Test fun rateExcludesSkippedFutureAndPendingToday() {
        val rate = HabitStats.successRate(1, 5, 5, daily, mapOf(
            1L to HabitLogRecord(1, 1, false),
            2L to HabitLogRecord(2, 0, true),
            4L to HabitLogRecord(4, 1, false)
        ))
        assertEquals(2, rate.completed)
        assertEquals(3, rate.eligible)
    }

    @Test fun emptyRateHasNoPercentage() {
        assertNull(HabitStats.successRate(1, 5, 5, emptyList(), emptyMap()).percent)
    }

    @Test fun dayStatusDistinguishesAllCalendarStates() {
        assertEquals(HabitDayStatus.DONE, HabitStats.dayStatus(4, 5, daily, mapOf(4L to HabitLogRecord(4, 1, false))))
        assertEquals(HabitDayStatus.MISSED, HabitStats.dayStatus(4, 5, daily, emptyMap()))
        assertEquals(HabitDayStatus.SKIPPED, HabitStats.dayStatus(4, 5, daily, mapOf(4L to HabitLogRecord(4, 0, true))))
        assertEquals(HabitDayStatus.PENDING, HabitStats.dayStatus(5, 5, daily, emptyMap()))
        assertEquals(HabitDayStatus.UNSCHEDULED, HabitStats.dayStatus(4, 5, emptyList(), emptyMap()))
    }
}
