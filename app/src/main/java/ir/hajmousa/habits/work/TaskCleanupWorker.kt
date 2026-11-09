package ir.hajmousa.habits.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.hajmousa.habits.HabitsApp
import kotlinx.coroutines.CancellationException

class TaskCleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        (applicationContext as HabitsApp).container.tasks.cleanupExpired()
        Result.success()
    } catch (error: Exception) {
        if (error is CancellationException) throw error
        Result.retry()
    }
}
