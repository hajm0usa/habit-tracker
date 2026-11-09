package ir.hajmousa.habits

import android.app.Application
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase()

class HabitsApp : Application()
