package ir.hajmousa.habits.core.date

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DayClock(private val clock: Clock, val dayStartHour: Int = 0) {
    init {
        require(dayStartHour in 0..6)
    }

    fun today(): LocalDate = LocalDate.ofInstant(
        Instant.ofEpochMilli(clock.millis() - dayStartHour * 60L * 60 * 1000),
        clock.zone
    )

    companion object {
        fun system(zone: ZoneId = ZoneId.systemDefault(), dayStartHour: Int = 0) =
            DayClock(Clock.system(zone), dayStartHour)
    }
}
