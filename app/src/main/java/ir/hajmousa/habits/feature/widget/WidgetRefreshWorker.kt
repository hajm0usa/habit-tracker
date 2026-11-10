package ir.hajmousa.habits.feature.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        WidgetUpdater.update(applicationContext)
        Result.success()
    } catch (error: Exception) {
        if (error is kotlinx.coroutines.CancellationException) throw error
        Result.retry()
    }
}
