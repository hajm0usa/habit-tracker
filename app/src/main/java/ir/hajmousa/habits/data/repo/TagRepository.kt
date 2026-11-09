package ir.hajmousa.habits.data.repo

import ir.hajmousa.habits.data.db.AppDatabase
import ir.hajmousa.habits.data.db.TagEntity
import kotlinx.coroutines.flow.Flow

class TagRepository(private val database: AppDatabase) {
    fun observeAll(): Flow<List<TagEntity>> = database.tagDao().observeAll()

    suspend fun save(tag: TagEntity) {
        require(tag.name.isNotBlank())
        val normalized = tag.copy(name = tag.name.trim())
        if (normalized.id == 0L) database.tagDao().insert(normalized) else database.tagDao().update(normalized)
    }

    suspend fun delete(id: Long) = database.tagDao().delete(id)
}
