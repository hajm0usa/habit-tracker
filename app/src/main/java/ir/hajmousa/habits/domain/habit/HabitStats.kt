package ir.hajmousa.habits.domain.habit

import ir.hajmousa.habits.domain.model.HabitLogRecord

enum class HabitDayStatus { DONE, MISSED, SKIPPED, UNSCHEDULED, PENDING }

data class HabitSuccessRate(val completed: Int, val eligible: Int) {
    val percent: Int? get() = if (eligible == 0) null else completed * 100 / eligible
}

object HabitStats {
    fun dayStatus(day: Long, today: Long, versions: List<ir.hajmousa.habits.domain.model.HabitSchedule>, logs: Map<Long, HabitLogRecord>): HabitDayStatus {
        val version = ScheduleResolver.versionOn(versions, day) ?: return HabitDayStatus.UNSCHEDULED
        if (!ScheduleResolver.isScheduled(day, versions)) return HabitDayStatus.UNSCHEDULED
        val log = logs[day]
        if (log?.skipped == true) return HabitDayStatus.SKIPPED
        if (CompletionRules.isCompleted(log, version.targetValue)) return HabitDayStatus.DONE
        return if (day >= today) HabitDayStatus.PENDING else HabitDayStatus.MISSED
    }

    fun successRate(
        fromEpochDay: Long,
        throughEpochDay: Long,
        todayEpochDay: Long,
        versions: List<ir.hajmousa.habits.domain.model.HabitSchedule>,
        logs: Map<Long, HabitLogRecord>
    ): HabitSuccessRate {
        require(fromEpochDay <= throughEpochDay)
        var completed = 0
        var eligible = 0
        for (day in fromEpochDay..minOf(throughEpochDay, todayEpochDay)) {
            val version = ScheduleResolver.versionOn(versions, day) ?: continue
            if (!ScheduleResolver.isScheduled(day, versions)) continue
            val log = logs[day]
            if (log?.skipped == true) continue
            if (day == todayEpochDay && !CompletionRules.isCompleted(log, version.targetValue)) continue
            eligible++
            if (CompletionRules.isCompleted(log, version.targetValue)) completed++
        }
        return HabitSuccessRate(completed, eligible)
    }

    fun numericTrend(todayEpochDay: Long, logs: Map<Long, HabitLogRecord>): List<Int> =
        (todayEpochDay - 29..todayEpochDay).map { day -> logs[day]?.takeUnless { it.skipped }?.value ?: 0 }
}
