package unical.enterpriceapplication.onlycards.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import unical.enterpriceapplication.onlycards.model.entity.SearchHistory

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM `SearchHistory` ORDER BY id DESC")
    fun getAll(): Flow<List<SearchHistory>>
    @Query("SELECT * FROM `SearchHistory` WHERE search LIKE :search")
    fun containsByName(search: String):  Flow<List<SearchHistory>>
    @Query("SELECT EXISTS(SELECT * FROM `SearchHistory` WHERE search = :search)")
    fun existsByName(search: String): Boolean
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchHistory: SearchHistory)

    @Query("SELECT * FROM SearchHistory ORDER BY id ASC LIMIT 1")
    suspend fun getOldestEntry(): SearchHistory?

    @Delete
    suspend fun delete(searchHistory: SearchHistory)
    @Query("SELECT COUNT(*) FROM `SearchHistory`")
    suspend fun count(): Int

    @Query("DELETE FROM `SearchHistory`")
    suspend fun deleteAll()
}