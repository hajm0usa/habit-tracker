package ir.hajmousa.habits.domain.task

import ir.hajmousa.habits.domain.model.TaskPriority
import ir.hajmousa.habits.domain.model.TaskRecord
import ir.hajmousa.habits.domain.model.TaskSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskRulesTest {
    @Test fun cleanupUsesStrictSevenDayBoundary() {
        assertFalse(TaskCleanup.isExpired(93, 100))
        assertTrue(TaskCleanup.isExpired(92, 100))
    }

    @Test fun groupsAndSortsTasksAndHidesOlderCompletion() {
        val tasks = listOf(
            TaskRecord(1, "overdue low", 98, TaskPriority.LOW, false, null),
            TaskRecord(2, "overdue high", 98, TaskPriority.HIGH, false, null),
            TaskRecord(3, "today", 100, TaskPriority.MEDIUM, false, null),
            TaskRecord(4, "future", 101, TaskPriority.HIGH, false, null),
            TaskRecord(5, "no date", null, TaskPriority.LOW, false, null),
            TaskRecord(6, "done recent", null, TaskPriority.MEDIUM, true, 93),
            TaskRecord(7, "done old", null, TaskPriority.HIGH, true, 92)
        )
        val grouped = TaskGrouping.group(tasks, 100)
        assertEquals(listOf(2L, 1L), grouped.getValue(TaskSection.OVERDUE).map { it.id })
        assertEquals(listOf(3L), grouped.getValue(TaskSection.TODAY).map { it.id })
        assertEquals(listOf(4L), grouped.getValue(TaskSection.UPCOMING).map { it.id })
        assertEquals(listOf(5L), grouped.getValue(TaskSection.NO_DATE).map { it.id })
        assertEquals(listOf(6L), grouped.getValue(TaskSection.DONE).map { it.id })
    }

    @Test fun tagFilterRequiresEverySelectedTag() {
        assertTrue(TaskTagFilter.matches(setOf(1L, 2L), setOf(1L, 2L)))
        assertFalse(TaskTagFilter.matches(setOf(1L), setOf(1L, 2L)))
        assertTrue(TaskTagFilter.matches(emptySet(), emptySet()))
    }
}
