package ir.hajmousa.habits.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupModel(
    val schemaVersion: Int = 1,
    val exportedAtEpochMillis: Long,
    val settings: BackupSettings,
    val tags: List<BackupTag>,
    val habits: List<BackupHabit>,
    val scheduleVersions: List<BackupSchedule>,
    val habitLogs: List<BackupLog>,
    val habitReminders: List<BackupReminder>,
    val tasks: List<BackupTask>,
    val habitTags: List<BackupHabitTag>,
    val taskTags: List<BackupTaskTag>
)

@Serializable data class BackupSettings(val dayStartHour: Int, val weeklyBackupEnabled: Boolean, val weeklyBackupMinute: Int)
@Serializable data class BackupTag(val id: Long, val name: String, val colorArgb: Int, val iconKey: String)
@Serializable data class BackupHabit(val id: Long, val name: String, val note: String?, val unit: String?, val type: String, val sortOrder: Int, val createdEpochDay: Long, val archived: Boolean)
@Serializable data class BackupSchedule(val id: Long, val habitId: Long, val effectiveFromEpochDay: Long, val scheduleType: String, val weekdaysMask: Int, val intervalN: Int?, val anchorEpochDay: Long?, val targetValue: Int, val step: Int)
@Serializable data class BackupLog(val id: Long, val habitId: Long, val epochDay: Long, val value: Int, val skipped: Boolean)
@Serializable data class BackupReminder(val id: Long, val habitId: Long, val minuteOfDay: Int)
@Serializable data class BackupTask(val id: Long, val title: String, val description: String?, val dueEpochDay: Long?, val dueMinuteOfDay: Int?, val reminderEnabled: Boolean, val priority: String, val isDone: Boolean, val doneAtEpochMillis: Long?, val doneEpochDay: Long?)
@Serializable data class BackupHabitTag(val habitId: Long, val tagId: Long)
@Serializable data class BackupTaskTag(val taskId: Long, val tagId: Long)

data class BackupSummary(val habitCount: Int, val taskCount: Int, val tagCount: Int, val exportedAtEpochMillis: Long)
