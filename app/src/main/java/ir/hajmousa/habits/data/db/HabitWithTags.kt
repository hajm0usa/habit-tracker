package ir.hajmousa.habits.data.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class HabitWithTags(
    @Embedded val habit: HabitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(value = HabitTagCrossRef::class, parentColumn = "habitId", entityColumn = "tagId")
    )
    val tags: List<TagEntity>
)
