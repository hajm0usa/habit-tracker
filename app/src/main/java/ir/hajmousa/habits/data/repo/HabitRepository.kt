package ir.hajmousa.habits.data.repo

import androidx.room.withTransaction
import ir.hajmousa.habits.core.date.DayClock
import ir.hajmousa.habits.data.db.AppDatabase
import ir.hajmousa.habits.data.db.HabitEntity
import ir.hajmousa.habits.data.db.HabitLogEntity
import ir.hajmousa.habits.data.db.HabitScheduleVersionEntity
import ir.hajmousa.habits.data.db.HabitTagCrossRef
import ir.hajmousa.habits.data.db.HabitWithTags
import ir.hajmousa.habits.domain.habit.CompletionRules
import ir.hajmousa.habits.domain.habit.ScheduleResolver
import ir.hajmousa.habits.domain.model.HabitLogRecord
import ir.hajmousa.habits.domain.model.HabitRecord
import ir.hajmousa.habits.domain.model.HabitSchedule
import ir.hajmousa.habits.domain.model.HabitType
import ir.hajmousa.habits.domain.model.ScheduleType
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val database: AppDatabase, private val dayClock: DayClock) {
    fun observeAll(): Flow<List<HabitWithTags>> = database.habitDao().observeAllWithTags()
    fun observeVersions(): Flow<List<HabitScheduleVersionEntity>> = database.habitDao().observeVersions()
    fun observeLogs(): Flow<List<HabitLogEntity>> = database.habitDao().observeLogs()

    suspend fun save(habit: HabitEntity, version: HabitScheduleVersionEntity, tagIds: Set<Long>): Long {
        require(habit.name.isNotBlank())
        require(version.targetValue > 0 && version.step > 0)
        when (version.scheduleType) {
            ScheduleType.DAILY.name -> Unit
            ScheduleType.WEEKDAYS.name -> require(version.weekdaysMask in 1..127)
            ScheduleType.EVERY_N.name -> require((version.intervalN ?: 0) > 0 && version.anchorEpochDay != null)
            else -> throw IllegalArgumentException("Unknown schedule type")
        }
        if (habit.type == HabitType.BOOLEAN.name) require(version.targetValue == 1 && version.step == 1)
        val today = dayClock.today().toEpochDay()
        return database.withTransaction {
            val id = if (habit.id == 0L) database.habitDao().insert(
                habit.copy(name = habit.name.trim(), sortOrder = database.habitDao().maxSortOrder() + 1)
            ) else {
                database.habitDao().update(habit.copy(name = habit.name.trim()))
                habit.id
            }
            database.habitDao().clearTags(id)
            if (tagIds.isNotEmpty()) database.habitDao().putTags(tagIds.map { HabitTagCrossRef(id, it) })
            database.habitDao().deleteVersionOn(id, today)
            database.habitDao().insertVersion(version.copy(id = 0, habitId = id, effectiveFromEpochDay = today))
            id
        }
    }

    suspend fun setValue(habitId: Long, epochDay: Long, value: Int) = database.withTransaction {
        requireEditableScheduledDay(habitId, epochDay)
        val habit = database.habitDao().find(habitId) ?: return@withTransaction
        require(value >= 0 && (habit.type != HabitType.BOOLEAN.name || value in 0..1))
        val previous = database.habitDao().findLog(habitId, epochDay)
        database.habitDao().putLog(HabitLogEntity(previous?.id ?: 0, habitId, epochDay, value, false))
    }

    suspend fun increment(habitId: Long, epochDay: Long, direction: Int) = database.withTransaction {
        require(direction == -1 || direction == 1)
        requireEditableScheduledDay(habitId, epochDay)
        val versions = database.habitDao().versions(habitId).map { it.toModel() }
        val version = ScheduleResolver.versionOn(versions, epochDay) ?: return@withTransaction
        val habit = database.habitDao().find(habitId) ?: return@withTransaction
        val current = database.habitDao().findLog(habitId, epochDay)?.value ?: 0
        val next = if (habit.type == HabitType.BOOLEAN.name) {
            if (direction > 0) 1 else 0
        } else {
            CompletionRules.adjustValue(current, version.step, direction)
        }
        val previous = database.habitDao().findLog(habitId, epochDay)
        database.habitDao().putLog(HabitLogEntity(previous?.id ?: 0, habitId, epochDay, next, false))
    }

    suspend fun setSkipped(habitId: Long, epochDay: Long, skipped: Boolean) = database.withTransaction {
        requireEditableScheduledDay(habitId, epochDay)
        val previous = database.habitDao().findLog(habitId, epochDay)
        database.habitDao().putLog(
            HabitLogEntity(previous?.id ?: 0, habitId, epochDay, previous?.value ?: 0, skipped)
        )
    }

    suspend fun setArchived(habitId: Long, archived: Boolean) = database.habitDao().setArchived(habitId, archived)

    suspend fun logCount(habitId: Long): Int = database.habitDao().logCount(habitId)

    suspend fun delete(habitId: Long): Int = database.withTransaction {
        val count = database.habitDao().logCount(habitId)
        database.habitDao().deleteHabit(habitId)
        count
    }

    suspend fun reorder(habitIds: List<Long>) = database.withTransaction {
        habitIds.forEachIndexed { index, id -> database.habitDao().setSortOrder(id, index) }
    }

    private suspend fun requireEditableScheduledDay(habitId: Long, epochDay: Long) {
        val today = dayClock.today().toEpochDay()
        require(epochDay in (today - 7)..today)
        val habit = database.habitDao().find(habitId) ?: error("Habit not found")
        require(!habit.archived)
        val versions = database.habitDao().versions(habitId).map { it.toModel() }
        require(ScheduleResolver.isScheduled(epochDay, versions))
    }
}

fun HabitEntity.toRecord() = HabitRecord(
    id = id,
    name = name,
    note = note,
    unit = unit,
    type = runCatching { HabitType.valueOf(type) }.getOrDefault(HabitType.BOOLEAN),
    sortOrder = sortOrder,
    createdEpochDay = createdEpochDay,
    archived = archived
)

fun HabitWithTags.toRecord() = habit.toRecord()

fun HabitScheduleVersionEntity.toModel() = HabitSchedule(
    effectiveFromEpochDay = effectiveFromEpochDay,
    type = runCatching { ScheduleType.valueOf(scheduleType) }.getOrDefault(ScheduleType.DAILY),
    weekdaysMask = weekdaysMask,
    intervalN = intervalN,
    anchorEpochDay = anchorEpochDay,
    targetValue = targetValue,
    step = step
)

fun HabitLogEntity.toModel() = HabitLogRecord(epochDay, value, skipped)
