package ir.hajmousa.habits

import android.app.Application
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ir.hajmousa.habits.work.TaskCleanupWorker
import ir.hajmousa.habits.notifications.NotificationHelper
import ir.hajmousa.habits.feature.widget.WidgetRefreshWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class HabitsApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper(this).createChannels()
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
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "today-widget-refresh",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15, TimeUnit.MINUTES).build()
        )
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                container.dayClock.setDayStartHour(container.settings.dayStartHour.first())
                container.reminders.scheduleAll()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                Log.e("KarHa", "Reminder scheduling failed", error)
            }
        }
    }
}
