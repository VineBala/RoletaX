package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TitleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TitleDao {
    @Query("SELECT * FROM titles ORDER BY updatedAt DESC")
    fun getAllTitles(): Flow<List<TitleEntity>>

    @Query("SELECT * FROM titles WHERE id = :id LIMIT 1")
    fun getTitleById(id: Long): Flow<TitleEntity?>

    @Query("SELECT * FROM titles WHERE category = :category ORDER BY updatedAt DESC")
    fun getTitlesByCategory(category: String): Flow<List<TitleEntity>>

    @Query("SELECT * FROM titles WHERE status = :status ORDER BY updatedAt DESC")
    fun getTitlesByStatus(status: String): Flow<List<TitleEntity>>

    @Query("SELECT * FROM titles WHERE status = 'EM_ANDAMENTO' ORDER BY lastWatchedAt DESC, updatedAt DESC")
    fun getInProgressTitles(): Flow<List<TitleEntity>>

    @Query("SELECT * FROM titles ORDER BY createdAt DESC LIMIT 10")
    fun getRecentlyAddedTitles(): Flow<List<TitleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(title: TitleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(titles: List<TitleEntity>)

    @Update
    suspend fun update(title: TitleEntity)

    @Delete
    suspend fun delete(title: TitleEntity)

    @Query("DELETE FROM titles WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM titles")
    suspend fun deleteAll()
}
