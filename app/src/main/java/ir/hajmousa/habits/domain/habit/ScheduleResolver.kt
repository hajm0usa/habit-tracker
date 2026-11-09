package ir.hajmousa.habits.domain.habit

import ir.hajmousa.habits.domain.model.HabitSchedule
import ir.hajmousa.habits.domain.model.ScheduleType
import java.time.LocalDate

object ScheduleResolver {
    fun versionOn(versions: List<HabitSchedule>, epochDay: Long): HabitSchedule? =
        versions.filter { it.effectiveFromEpochDay <= epochDay }.maxByOrNull { it.effectiveFromEpochDay }

    fun isScheduled(epochDay: Long, versions: List<HabitSchedule>): Boolean {
        val version = versionOn(versions, epochDay) ?: return false
        return when (version.type) {
            ScheduleType.DAILY -> true
            ScheduleType.WEEKDAYS -> {
                val saturdayBasedDay = (LocalDate.ofEpochDay(epochDay).dayOfWeek.value + 1) % 7
                version.weekdaysMask and (1 shl saturdayBasedDay) != 0
            }
            ScheduleType.EVERY_N -> {
                val anchor = version.anchorEpochDay ?: return false
                val interval = version.intervalN ?: return false
                require(interval > 0)
                epochDay >= anchor && (epochDay - anchor) % interval == 0L
            }
        }
    }
}
