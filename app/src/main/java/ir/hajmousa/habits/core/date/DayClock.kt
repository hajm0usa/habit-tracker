package ir.hajmousa.habits.core.date

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DayClock(
    private val clock: Clock,
    val dayStartHour: Int = 0,
    private val zoneProvider: () -> ZoneId = { clock.zone }
) {
    init {
        require(dayStartHour in 0..6)
    }

    fun zoneId(): ZoneId = zoneProvider()

    fun today(): LocalDate = LocalDate.ofInstant(
        Instant.ofEpochMilli(clock.millis() - dayStartHour * 60L * 60 * 1000),
        zoneId()
    )

    companion object {
        fun system(zone: ZoneId? = null, dayStartHour: Int = 0) =
            DayClock(Clock.systemUTC(), dayStartHour) { zone ?: ZoneId.systemDefault() }
    }
}
