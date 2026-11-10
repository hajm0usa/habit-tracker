package ir.hajmousa.habits.domain.task

import org.junit.Assert.assertEquals
import org.junit.Test

class TodayProgressTest {
    @Test fun progressIncludesScheduledHabitsAndTodayTasksButNotSkippedOrArchived() {
        assertEquals(
            TodayProgress(2, 3),
            TodayProgressCalculator.calculate(10, listOf(
                ProgressHabit(true, false, 1, 1, false),
                ProgressHabit(true, false, 5, 2, false),
                ProgressHabit(true, false, 1, 1, true),
                ProgressHabit(true, true, 1, 1, false),
                ProgressHabit(false, false, 1, 1, false)
            ), listOf(ProgressTask(10, true), ProgressTask(11, false)))
        )
    }
}
