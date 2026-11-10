package ir.hajmousa.habits.domain.task

data class ProgressHabit(val scheduled: Boolean, val archived: Boolean, val target: Int, val value: Int?, val skipped: Boolean)
data class ProgressTask(val dueEpochDay: Long?, val done: Boolean)
data class TodayProgress(val done: Int, val total: Int)

object TodayProgressCalculator {
    fun calculate(todayEpochDay: Long, habits: List<ProgressHabit>, tasks: List<ProgressTask>): TodayProgress {
        val activeHabits = habits.filter { it.scheduled && !it.archived && !it.skipped }
        val todayTasks = tasks.filter { it.dueEpochDay == todayEpochDay }
        return TodayProgress(
            done = activeHabits.count { (it.value ?: 0) >= it.target } + todayTasks.count { it.done },
            total = activeHabits.size + todayTasks.size
        )
    }
}
