package ir.hajmousa.habits.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.hajmousa.habits.HabitsApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class TaskCleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        val container = (applicationContext as HabitsApp).container
        container.dayClock.setDayStartHour(container.settings.dayStartHour.first())
        container.tasks.cleanupExpired()
        Result.success()
    } catch (error: Exception) {
        if (error is CancellationException) throw error
        Result.retry()
    }
}
