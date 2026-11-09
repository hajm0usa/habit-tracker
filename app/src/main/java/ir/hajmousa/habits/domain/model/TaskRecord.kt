package ir.hajmousa.habits.domain.model

data class TaskRecord(
    val id: Long,
    val title: String,
    val dueEpochDay: Long?,
    val priority: TaskPriority,
    val isDone: Boolean,
    val doneEpochDay: Long?
)

enum class TaskPriority { LOW, MEDIUM, HIGH }

enum class TaskSection { OVERDUE, TODAY, UPCOMING, NO_DATE, DONE }
