package ir.hajmousa.habits.data.repo

import androidx.room.withTransaction
import ir.hajmousa.habits.core.date.DayClock
import ir.hajmousa.habits.data.db.AppDatabase
import ir.hajmousa.habits.data.db.TaskEntity
import ir.hajmousa.habits.data.db.TaskTagCrossRef
import ir.hajmousa.habits.data.db.TaskWithTags
import ir.hajmousa.habits.domain.model.TaskPriority
import ir.hajmousa.habits.domain.model.TaskRecord
import ir.hajmousa.habits.domain.task.TaskCleanup
import java.time.Clock
import kotlinx.coroutines.flow.Flow

data class DeletedTask(val task: TaskEntity, val tagIds: List<Long>)

class TaskRepository(
    private val database: AppDatabase,
    private val dayClock: DayClock,
    private val clock: Clock
) {
    fun observeAll(): Flow<List<TaskWithTags>> = database.taskDao().observeWithTags()

    suspend fun save(task: TaskEntity, tagIds: Set<Long>): Long {
        require(task.title.isNotBlank())
        require(task.dueMinuteOfDay == null || task.dueMinuteOfDay in 0..1439)
        require(!task.reminderEnabled || (task.dueEpochDay != null && task.dueMinuteOfDay != null))
        return database.withTransaction {
            val id = database.taskDao().put(task.copy(title = task.title.trim()))
            val taskId = if (task.id == 0L) id else task.id
            database.taskDao().clearTags(taskId)
            if (tagIds.isNotEmpty()) database.taskDao().putTags(tagIds.map { TaskTagCrossRef(taskId, it) })
            taskId
        }
    }

    suspend fun setDone(id: Long, done: Boolean) {
        val task = database.taskDao().find(id) ?: return
        database.taskDao().update(
            task.copy(
                isDone = done,
                doneAtEpochMillis = if (done) clock.millis() else null,
                doneEpochDay = if (done) dayClock.today().toEpochDay() else null
            )
        )
    }

    suspend fun delete(id: Long): DeletedTask? = database.withTransaction {
        val task = database.taskDao().find(id) ?: return@withTransaction null
        val snapshot = DeletedTask(task, database.taskDao().tagIds(id))
        database.taskDao().delete(id)
        snapshot
    }

    suspend fun restore(deleted: DeletedTask) = database.withTransaction {
        val restoredId = database.taskDao().insertIfAbsent(deleted.task).let { insertedId ->
            if (insertedId == -1L) database.taskDao().put(deleted.task.copy(id = 0)) else insertedId
        }
        val existingTagIds = if (deleted.tagIds.isEmpty()) emptyList() else database.tagDao().existingIds(deleted.tagIds)
        if (existingTagIds.isNotEmpty()) {
            database.taskDao().putTags(existingTagIds.map { TaskTagCrossRef(restoredId, it) })
        }
    }

    suspend fun cleanupExpired(): Int {
        val today = dayClock.today().toEpochDay()
        return database.taskDao().deleteExpired(TaskCleanup.cutoffEpochDay(today))
    }
}

fun TaskWithTags.toRecord() = TaskRecord(
    id = task.id,
    title = task.title,
    dueEpochDay = task.dueEpochDay,
    priority = runCatching { TaskPriority.valueOf(task.priority) }.getOrDefault(TaskPriority.MEDIUM),
    isDone = task.isDone,
    doneEpochDay = task.doneEpochDay
)
