package ir.hajmousa.habits.core.date

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DayClock(
    private val clock: Clock,
    dayStartHour: Int = 0,
    private val zoneProvider: () -> ZoneId = { clock.zone }
) {
    @Volatile var dayStartHour: Int = dayStartHour
        private set

    init {
        require(dayStartHour in 0..6)
    }

    fun zoneId(): ZoneId = zoneProvider()

    fun setDayStartHour(hour: Int) {
        require(hour in 0..6)
        dayStartHour = hour
    }

    fun today(): LocalDate = Instant.ofEpochMilli(clock.millis() - dayStartHour * 60L * 60 * 1000)
        .atZone(zoneId())
        .toLocalDate()

    companion object {
        fun system(zone: ZoneId? = null, dayStartHour: Int = 0) =
            DayClock(Clock.systemUTC(), dayStartHour) { zone ?: ZoneId.systemDefault() }
    }
}
