package ir.hajmousa.habits.domain.model

enum class HabitType { BOOLEAN, NUMERIC }

enum class ScheduleType { DAILY, WEEKDAYS, EVERY_N }

data class HabitRecord(
    val id: Long,
    val name: String,
    val note: String?,
    val unit: String?,
    val type: HabitType,
    val sortOrder: Int,
    val createdEpochDay: Long,
    val archived: Boolean
)

data class HabitSchedule(
    val effectiveFromEpochDay: Long,
    val type: ScheduleType,
    val weekdaysMask: Int,
    val intervalN: Int?,
    val anchorEpochDay: Long?,
    val targetValue: Int,
    val step: Int
)

data class HabitLogRecord(val epochDay: Long, val value: Int, val skipped: Boolean)
