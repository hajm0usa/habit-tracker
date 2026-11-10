package ir.hajmousa.habits.data.backup

import androidx.room.withTransaction
import ir.hajmousa.habits.data.db.*
import ir.hajmousa.habits.data.settings.SettingsStore
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import ir.hajmousa.habits.core.date.JalaliConverter

class BackupService(private val database: AppDatabase, private val settings: SettingsStore, private val clock: Clock = Clock.systemUTC()) {
    suspend fun export(): String {
        val settingSnapshot = BackupSettings(settings.dayStartHour.first(), settings.isWeeklyBackupEnabled(), settings.getWeeklyBackupMinute())
        val backup = database.withTransaction {
            val habits = database.habitDao().allHabits()
            BackupModel(
                exportedAtEpochMillis = clock.millis(),
                settings = settingSnapshot,
                tags = database.tagDao().all().map { BackupTag(it.id, it.name, it.colorArgb, it.iconKey) },
                habits = habits.map { BackupHabit(it.id, it.name, it.note, it.unit, it.type, it.sortOrder, it.createdEpochDay, it.archived) },
                scheduleVersions = database.habitDao().allVersions().map { BackupSchedule(it.id, it.habitId, it.effectiveFromEpochDay, it.scheduleType, it.weekdaysMask, it.intervalN, it.anchorEpochDay, it.targetValue, it.step) },
                habitLogs = database.habitDao().allLogs().map { BackupLog(it.id, it.habitId, it.epochDay, it.value, it.skipped) },
                habitReminders = database.habitDao().allReminders().map { BackupReminder(it.id, it.habitId, it.minuteOfDay) },
                tasks = database.taskDao().allTasks().map { BackupTask(it.id, it.title, it.description, it.dueEpochDay, it.dueMinuteOfDay, it.reminderEnabled, it.priority, it.isDone, it.doneAtEpochMillis, it.doneEpochDay) },
                habitTags = database.habitDao().allTagRefs().map { BackupHabitTag(it.habitId, it.tagId) },
                taskTags = database.taskDao().allTagRefs().map { BackupTaskTag(it.taskId, it.tagId) }
            )
        }
        return BackupCodec.encode(backup)
    }

    fun inspect(content: String): Pair<BackupModel, BackupSummary> {
        val backup = BackupCodec.decode(content)
        return backup to BackupSummary(backup.habits.size, backup.tasks.size, backup.tags.size, backup.exportedAtEpochMillis)
    }

    suspend fun restore(backup: BackupModel) {
        BackupValidator.validate(backup)
        database.withTransaction {
            database.taskDao().deleteAll()
            database.habitDao().deleteAll()
            database.tagDao().deleteAll()
            database.tagDao().insertAll(backup.tags.map { TagEntity(it.id, it.name, it.colorArgb, it.iconKey) })
            database.habitDao().insertAll(backup.habits.map { HabitEntity(it.id, it.name, it.note, it.unit, it.type, it.sortOrder, it.createdEpochDay, it.archived) })
            database.taskDao().insertAll(backup.tasks.map { TaskEntity(it.id, it.title, it.description, it.dueEpochDay, it.dueMinuteOfDay, it.reminderEnabled, it.priority, it.isDone, it.doneAtEpochMillis, it.doneEpochDay) })
            database.habitDao().insertVersions(backup.scheduleVersions.map { HabitScheduleVersionEntity(it.id, it.habitId, it.effectiveFromEpochDay, it.scheduleType, it.weekdaysMask, it.intervalN, it.anchorEpochDay, it.targetValue, it.step) })
            database.habitDao().insertLogs(backup.habitLogs.map { HabitLogEntity(it.id, it.habitId, it.epochDay, it.value, it.skipped) })
            database.habitDao().restoreReminders(backup.habitReminders.map { HabitReminderEntity(it.id, it.habitId, it.minuteOfDay) })
            database.habitDao().insertTagRefs(backup.habitTags.map { HabitTagCrossRef(it.habitId, it.tagId) })
            database.taskDao().insertTagRefs(backup.taskTags.map { TaskTagCrossRef(it.taskId, it.tagId) })
        }
        settings.setDayStartHour(backup.settings.dayStartHour)
        settings.setWeeklyBackupEnabled(backup.settings.weeklyBackupEnabled)
        settings.setWeeklyBackupMinute(backup.settings.weeklyBackupMinute)
    }

}

internal object BackupCodec {
    private val json = kotlinx.serialization.json.Json { encodeDefaults = true }
    fun encode(data: BackupModel): String = json.encodeToString(data)
    fun decode(content: String): BackupModel {
        require(content.length <= 20_000_000) { "Backup is too large" }
        val root = try { json.parseToJsonElement(content) as? JsonObject }
        catch (error: kotlinx.serialization.SerializationException) { throw IllegalArgumentException("Invalid backup JSON", error) }
            ?: throw IllegalArgumentException("Invalid backup JSON")
        val version = root["schemaVersion"]?.jsonPrimitive?.intOrNull ?: throw IllegalArgumentException("Missing backup version")
        require(version == 1) { "Unsupported backup version" }
        val backup = try { json.decodeFromJsonElement<BackupModel>(root) }
        catch (error: kotlinx.serialization.SerializationException) { throw IllegalArgumentException("Invalid backup JSON", error) }
        BackupValidator.validate(backup)
        return backup
    }
}

internal object BackupValidator {
    private fun validDay(epochDay: Long): Boolean = try {
        JalaliConverter.toJalali(LocalDate.ofEpochDay(epochDay))
        true
    } catch (_: Exception) { false }

    fun validate(data: BackupModel) {
        require(data.schemaVersion == 1) { "Unsupported backup version" }
        require(data.exportedAtEpochMillis >= 0 && data.settings.dayStartHour in 0..6 && data.settings.weeklyBackupMinute in 0..1439)
        require(validDay(Instant.ofEpochMilli(data.exportedAtEpochMillis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()))
        fun unique(ids: List<Long>) = require(ids.all { it > 0 } && ids.toSet().size == ids.size)
        unique(data.tags.map { it.id }); unique(data.habits.map { it.id }); unique(data.tasks.map { it.id })
        unique(data.scheduleVersions.map { it.id }); unique(data.habitLogs.map { it.id }); unique(data.habitReminders.map { it.id })
        require(data.tags.all { it.name.isNotBlank() && it.iconKey.isNotBlank() })
        require(data.habits.all { it.name.isNotBlank() && it.sortOrder >= 0 && it.type in setOf("BOOLEAN", "NUMERIC") && validDay(it.createdEpochDay) })
        val habitIds = data.habits.map { it.id }.toSet()
        val tagIds = data.tags.map { it.id }.toSet()
        val taskIds = data.tasks.map { it.id }.toSet()
        require(data.scheduleVersions.all {
            it.habitId in habitIds && validDay(it.effectiveFromEpochDay) && it.targetValue > 0 && it.step > 0 && when (it.scheduleType) {
                "DAILY" -> it.weekdaysMask == 0 && it.intervalN == null && it.anchorEpochDay == null
                "WEEKDAYS" -> it.weekdaysMask in 1..127 && it.intervalN == null && it.anchorEpochDay == null
                "EVERY_N" -> it.weekdaysMask == 0 && (it.intervalN ?: 0) > 0 && (it.anchorEpochDay?.let(::validDay) == true)
                else -> false
            }
        })
        require(data.habits.all { habit -> data.scheduleVersions.any { it.habitId == habit.id } })
        require(data.habits.all { habit -> data.scheduleVersions.filter { it.habitId == habit.id }.all { habit.type != "BOOLEAN" || it.targetValue == 1 && it.step == 1 } })
        require(data.scheduleVersions.map { it.habitId to it.effectiveFromEpochDay }.toSet().size == data.scheduleVersions.size)
        val habitTypes = data.habits.associate { it.id to it.type }
        require(data.habitLogs.all { it.habitId in habitIds && validDay(it.epochDay) && it.value >= 0 && (habitTypes[it.habitId] != "BOOLEAN" || it.value <= 1) })
        require(data.habitLogs.map { it.habitId to it.epochDay }.toSet().size == data.habitLogs.size)
        require(data.habitReminders.all { it.habitId in habitIds && it.minuteOfDay in 0..1439 })
        require(data.habitReminders.map { it.habitId to it.minuteOfDay }.toSet().size == data.habitReminders.size)
        require(data.tasks.all { task ->
            task.title.isNotBlank() && task.priority in setOf("LOW", "MEDIUM", "HIGH") &&
                (task.dueEpochDay == null || validDay(task.dueEpochDay)) &&
                (task.doneEpochDay == null || validDay(task.doneEpochDay)) &&
                (task.doneAtEpochMillis == null || task.doneAtEpochMillis >= 0) &&
                (task.dueMinuteOfDay == null || task.dueMinuteOfDay in 0..1439) &&
                (!task.reminderEnabled || task.dueEpochDay != null && task.dueMinuteOfDay != null) &&
                (if (task.isDone) task.doneAtEpochMillis != null && task.doneEpochDay != null
                else task.doneAtEpochMillis == null && task.doneEpochDay == null)
        })
        require(data.habitTags.all { it.habitId in habitIds && it.tagId in tagIds })
        require(data.taskTags.all { it.taskId in taskIds && it.tagId in tagIds })
        require(data.habitTags.map { it.habitId to it.tagId }.toSet().size == data.habitTags.size)
        require(data.taskTags.map { it.taskId to it.tagId }.toSet().size == data.taskTags.size)
    }
}
