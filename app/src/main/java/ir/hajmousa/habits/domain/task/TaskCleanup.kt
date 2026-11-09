package ir.hajmousa.habits.domain.task

object TaskCleanup {
    fun cutoffEpochDay(todayEpochDay: Long): Long = todayEpochDay - 7

    fun isExpired(doneEpochDay: Long, todayEpochDay: Long): Boolean = doneEpochDay < cutoffEpochDay(todayEpochDay)
}
