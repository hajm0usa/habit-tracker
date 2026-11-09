package ir.hajmousa.habits.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorArgb: Int,
    val iconKey: String
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dueEpochDay: Long? = null,
    val dueMinuteOfDay: Int? = null,
    val reminderEnabled: Boolean = false,
    val priority: String = "MEDIUM",
    val isDone: Boolean = false,
    val doneAtEpochMillis: Long? = null,
    val doneEpochDay: Long? = null
)

@Entity(
    tableName = "task_tag_cross_ref",
    primaryKeys = ["taskId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = TaskEntity::class, parentColumns = ["id"], childColumns = ["taskId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["taskId"]), Index(value = ["tagId"])]
)
data class TaskTagCrossRef(val taskId: Long, val tagId: Long)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val note: String? = null,
    val unit: String? = null,
    val type: String,
    val sortOrder: Int,
    val createdEpochDay: Long,
    val archived: Boolean = false
)

@Entity(
    tableName = "habit_schedule_versions",
    foreignKeys = [ForeignKey(entity = HabitEntity::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["habitId"]), Index(value = ["habitId", "effectiveFromEpochDay"], unique = true)]
)
data class HabitScheduleVersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val effectiveFromEpochDay: Long,
    val scheduleType: String,
    val weekdaysMask: Int,
    val intervalN: Int? = null,
    val anchorEpochDay: Long? = null,
    val targetValue: Int,
    val step: Int
)

@Entity(
    tableName = "habit_logs",
    foreignKeys = [ForeignKey(entity = HabitEntity::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["habitId", "epochDay"], unique = true)]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val epochDay: Long,
    val value: Int,
    val skipped: Boolean = false
)

@Entity(
    tableName = "habit_reminders",
    foreignKeys = [ForeignKey(entity = HabitEntity::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["habitId", "minuteOfDay"], unique = true)]
)
data class HabitReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val minuteOfDay: Int
)

@Entity(
    tableName = "habit_tag_cross_ref",
    primaryKeys = ["habitId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = HabitEntity::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["habitId"]), Index(value = ["tagId"])]
)
data class HabitTagCrossRef(val habitId: Long, val tagId: Long)
