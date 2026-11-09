package ir.hajmousa.habits

import android.content.Context
import androidx.room.Room
import ir.hajmousa.habits.core.date.DayClock
import ir.hajmousa.habits.data.db.AppDatabase
import ir.hajmousa.habits.data.repo.HabitRepository
import ir.hajmousa.habits.data.repo.TagRepository
import ir.hajmousa.habits.data.repo.TaskRepository
import java.time.Clock

class AppContainer(context: Context) {
    val dayClock = DayClock.system()
    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "kar-ha.db"
    ).build()
    val tasks = TaskRepository(database, dayClock, Clock.systemDefaultZone())
    val habits = HabitRepository(database, dayClock)
    val tags = TagRepository(database)
}
