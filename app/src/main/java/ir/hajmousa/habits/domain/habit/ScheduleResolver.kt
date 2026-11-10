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

    fun nextScheduledDay(fromEpochDay: Long, versions: List<HabitSchedule>): Long? {
        var day = fromEpochDay
        while (true) {
            val version = versionOn(versions, day)
            if (version == null) {
                day = versions.asSequence().map { it.effectiveFromEpochDay }.filter { it > day }.minOrNull() ?: return null
                continue
            }
            when (version.type) {
                ScheduleType.DAILY -> return day
                ScheduleType.WEEKDAYS -> {
                    val nextVersionDay = versions.asSequence().map { it.effectiveFromEpochDay }.filter { it > day }.minOrNull()
                    if (version.weekdaysMask == 0) {
                        day = nextVersionDay ?: return null
                        continue
                    }
                    var scheduledDay = day
                    while (true) {
                        if (versionOn(versions, scheduledDay) != version) break
                        val weekday = (LocalDate.ofEpochDay(scheduledDay).dayOfWeek.value + 1) % 7
                        if (version.weekdaysMask and (1 shl weekday) != 0) return scheduledDay
                        scheduledDay++
                    }
                    day = nextVersionDay ?: return null
                }
                ScheduleType.EVERY_N -> {
                    val anchor = version.anchorEpochDay ?: return null
                    val interval = version.intervalN ?: return null
                    require(interval > 0)
                    val candidate = if (day <= anchor) anchor else {
                        val remainder = (day - anchor) % interval
                        if (remainder == 0L) day else day + interval - remainder
                    }
                    val nextVersionDay = versions.asSequence().map { it.effectiveFromEpochDay }.filter { it > day }.minOrNull()
                    if (nextVersionDay != null && nextVersionDay <= candidate) day = nextVersionDay else return candidate
                }
            }
        }
    }
}
