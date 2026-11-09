package ir.hajmousa.habits.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TagEntity::class, TaskEntity::class, TaskTagCrossRef::class,
        HabitEntity::class, HabitScheduleVersionEntity::class, HabitLogEntity::class,
        HabitReminderEntity::class, HabitTagCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun tagDao(): TagDao
    abstract fun habitDao(): HabitDao
}
