package ir.hajmousa.habits.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorArgb: Int,
    val iconKey: String
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dueEpochDay: Long? = null,
    val dueMinuteOfDay: Int? = null,
    val reminderEnabled: Boolean = false,
    val priority: String = "MEDIUM",
    val isDone: Boolean = false,
    val doneAtEpochMillis: Long? = null,
    val doneEpochDay: Long? = null
)

@Entity(
    tableName = "task_tag_cross_ref",
    primaryKeys = ["taskId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = TaskEntity::class, parentColumns = ["id"], childColumns = ["taskId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["taskId"]), Index(value = ["tagId"])]
)
data class TaskTagCrossRef(val taskId: Long, val tagId: Long)
