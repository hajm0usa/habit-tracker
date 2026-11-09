package ir.hajmousa.habits.core.date

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class DayClockTest {
    @Test fun appliesEffectiveDayBoundaryInConfiguredZone() {
        val zone = ZoneId.of("Asia/Tehran")
        fun clock(instant: String) = Clock.fixed(Instant.parse(instant), zone)
        assertEquals(java.time.LocalDate.of(2024, 3, 19), DayClock(clock("2024-03-20T00:29:00Z"), 4).today())
        assertEquals(java.time.LocalDate.of(2024, 3, 20), DayClock(clock("2024-03-20T00:31:00Z"), 4).today())
    }
}
