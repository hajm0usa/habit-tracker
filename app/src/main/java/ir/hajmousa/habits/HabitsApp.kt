package ir.hajmousa.habits

import android.app.Application
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ir.hajmousa.habits.work.TaskCleanupWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class HabitsApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                container.tasks.cleanupExpired()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                Log.e("KarHa", "Task cleanup failed", error)
            }
        }
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "task-cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<TaskCleanupWorker>(24, TimeUnit.HOURS).build()
        )
    }
}
