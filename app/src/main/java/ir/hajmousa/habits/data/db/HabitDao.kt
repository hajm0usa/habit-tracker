package ir.hajmousa.habits.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Transaction
    @Query("SELECT * FROM habits ORDER BY sortOrder, id")
    fun observeAllWithTags(): Flow<List<HabitWithTags>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun find(id: Long): HabitEntity?

    @Query("SELECT * FROM habits")
    suspend fun allHabits(): List<HabitEntity>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM habits")
    suspend fun maxSortOrder(): Int

    @Query("SELECT * FROM habit_schedule_versions WHERE habitId = :habitId ORDER BY effectiveFromEpochDay")
    suspend fun versions(habitId: Long): List<HabitScheduleVersionEntity>

    @Query("SELECT * FROM habit_schedule_versions")
    fun observeVersions(): Flow<List<HabitScheduleVersionEntity>>

    @Query("SELECT * FROM habit_logs")
    fun observeLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND epochDay = :epochDay LIMIT 1")
    suspend fun findLog(habitId: Long, epochDay: Long): HabitLogEntity?

    @Query("SELECT * FROM habit_reminders")
    suspend fun allReminders(): List<HabitReminderEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM habit_reminders WHERE habitId = :habitId AND minuteOfDay = :minuteOfDay)")
    suspend fun hasReminder(habitId: Long, minuteOfDay: Int): Boolean

    @Query("SELECT * FROM habit_reminders")
    fun observeReminders(): Flow<List<HabitReminderEntity>>

    @Query("DELETE FROM habit_reminders WHERE habitId = :habitId")
    suspend fun deleteReminders(habitId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReminders(reminders: List<HabitReminderEntity>)

    @Insert
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Insert
    suspend fun insertVersion(version: HabitScheduleVersionEntity)

    @Query("DELETE FROM habit_schedule_versions WHERE habitId = :habitId AND effectiveFromEpochDay = :epochDay")
    suspend fun deleteVersionOn(habitId: Long, epochDay: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putLog(log: HabitLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putTags(crossRefs: List<HabitTagCrossRef>)

    @Query("DELETE FROM habit_tag_cross_ref WHERE habitId = :habitId")
    suspend fun clearTags(habitId: Long)

    @Query("UPDATE habits SET archived = :archived WHERE id = :habitId")
    suspend fun setArchived(habitId: Long, archived: Boolean)

    @Query("UPDATE habits SET sortOrder = :sortOrder WHERE id = :habitId")
    suspend fun setSortOrder(habitId: Long, sortOrder: Int)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabit(habitId: Long)

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId")
    suspend fun logCount(habitId: Long): Int
}
