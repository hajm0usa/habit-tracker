package ir.hajmousa.habits.domain.task

import ir.hajmousa.habits.domain.model.TaskPriority
import ir.hajmousa.habits.domain.model.TaskRecord
import ir.hajmousa.habits.domain.model.TaskSection

object TaskGrouping {
    fun group(tasks: List<TaskRecord>, todayEpochDay: Long): Map<TaskSection, List<TaskRecord>> {
        val groups = TaskSection.entries.associateWith { mutableListOf<TaskRecord>() }
        val doneCutoff = TaskCleanup.cutoffEpochDay(todayEpochDay)
        tasks.forEach { task ->
            val section = when {
                task.isDone -> if ((task.doneEpochDay ?: Long.MIN_VALUE) >= doneCutoff) TaskSection.DONE else null
                task.dueEpochDay == null -> TaskSection.NO_DATE
                task.dueEpochDay < todayEpochDay -> TaskSection.OVERDUE
                task.dueEpochDay == todayEpochDay -> TaskSection.TODAY
                else -> TaskSection.UPCOMING
            }
            section?.let { groups.getValue(it) += task }
        }
        val priorityOrder = compareByDescending<TaskRecord> { it.priority.ordinal }
        groups.getValue(TaskSection.OVERDUE).sortWith(compareBy<TaskRecord> { it.dueEpochDay }.then(priorityOrder))
        groups.getValue(TaskSection.TODAY).sortWith(priorityOrder)
        groups.getValue(TaskSection.UPCOMING).sortWith(compareBy<TaskRecord> { it.dueEpochDay }.then(priorityOrder))
        groups.getValue(TaskSection.NO_DATE).sortWith(priorityOrder)
        groups.getValue(TaskSection.DONE).sortWith(compareByDescending<TaskRecord> { it.doneEpochDay }.then(priorityOrder))
        return groups
    }
}
