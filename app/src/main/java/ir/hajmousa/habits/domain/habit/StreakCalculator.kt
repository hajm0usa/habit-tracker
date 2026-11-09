package ir.hajmousa.habits.domain.habit

import ir.hajmousa.habits.domain.model.HabitLogRecord
import ir.hajmousa.habits.domain.model.HabitSchedule

object StreakCalculator {
    fun current(createdEpochDay: Long, todayEpochDay: Long, versions: List<HabitSchedule>, logs: Map<Long, HabitLogRecord>): Int {
        if (todayEpochDay < createdEpochDay) return 0
        var day = todayEpochDay
        val todayVersion = ScheduleResolver.versionOn(versions, day)
        val todayLog = logs[day]
        if (todayVersion != null && ScheduleResolver.isScheduled(day, versions) && todayLog?.skipped != true &&
            !CompletionRules.isCompleted(todayLog, todayVersion.targetValue)
        ) day--

        var streak = 0
        while (day >= createdEpochDay) {
            val version = ScheduleResolver.versionOn(versions, day)
            when {
                version == null || !ScheduleResolver.isScheduled(day, versions) -> day--
                logs[day]?.skipped == true -> day--
                CompletionRules.isCompleted(logs[day], version.targetValue) -> { streak++; day-- }
                else -> break
            }
        }
        return streak
    }

    fun best(createdEpochDay: Long, todayEpochDay: Long, versions: List<HabitSchedule>, logs: Map<Long, HabitLogRecord>): Int {
        if (todayEpochDay < createdEpochDay) return 0
        var current = 0
        var best = 0
        for (day in createdEpochDay..todayEpochDay) {
            val version = ScheduleResolver.versionOn(versions, day)
            if (version == null || !ScheduleResolver.isScheduled(day, versions) || logs[day]?.skipped == true) continue
            when {
                CompletionRules.isCompleted(logs[day], version.targetValue) -> current++
                day != todayEpochDay -> current = 0
            }
            best = maxOf(best, current)
        }
        return best
    }
}
