package ir.hajmousa.habits.domain.task

object TaskTagFilter {
    fun matches(itemTagIds: Set<Long>, selectedTagIds: Set<Long>): Boolean = selectedTagIds.all { it in itemTagIds }
}
