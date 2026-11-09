package ir.hajmousa.habits.data.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TaskWithTags(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(value = TaskTagCrossRef::class, parentColumn = "taskId", entityColumn = "tagId")
    )
    val tags: List<TagEntity>
)
