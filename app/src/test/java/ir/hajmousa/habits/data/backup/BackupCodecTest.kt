package ir.hajmousa.habits.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupCodecTest {
    private val valid = BackupModel(
        exportedAtEpochMillis = 100,
        settings = BackupSettings(0, true, 1020),
        tags = listOf(BackupTag(1, "tag", 0xFF000000.toInt(), "star")),
        habits = listOf(BackupHabit(2, "habit", null, null, "BOOLEAN", 0, 10, false)),
        scheduleVersions = listOf(BackupSchedule(3, 2, 10, "DAILY", 0, null, null, 1, 1)),
        habitLogs = listOf(BackupLog(4, 2, 10, 1, false)),
        habitReminders = listOf(BackupReminder(5, 2, 540)),
        tasks = listOf(BackupTask(6, "task", null, null, null, false, "MEDIUM", false, null, null)),
        habitTags = listOf(BackupHabitTag(2, 1)),
        taskTags = listOf(BackupTaskTag(6, 1))
    )

    @Test fun roundTripsValidBackup() {
        assertEquals(valid, BackupCodec.decode(BackupCodec.encode(valid)))
    }

    @Test fun rejectsInvalidReferencesAndUnknownSchema() {
        assertThrows(IllegalArgumentException::class.java) { BackupValidator.validate(valid.copy(taskTags = listOf(BackupTaskTag(999, 1)))) }
        assertThrows(IllegalArgumentException::class.java) { BackupCodec.decode(BackupCodec.encode(valid.copy(schemaVersion = 2))) }
        assertThrows(IllegalArgumentException::class.java) { BackupCodec.decode("not JSON") }
        assertThrows(IllegalArgumentException::class.java) { BackupValidator.validate(valid.copy(habits = listOf(valid.habits.single().copy(createdEpochDay = Long.MAX_VALUE)))) }
    }
}
