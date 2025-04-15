package com.example.pupilmesh.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManga(manga: MangaEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mangas: List<MangaEntity>)
    
    @Query("SELECT * FROM manga ORDER BY page ASC, title ASC")
    fun getAllManga(): Flow<List<MangaEntity>>
    
    @Query("SELECT * FROM manga WHERE page = :page ORDER BY title ASC")
    fun getMangaByPage(page: Int): Flow<List<MangaEntity>>
    
    @Query("SELECT * FROM manga WHERE id = :id LIMIT 1")
    suspend fun getMangaById(id: String): MangaEntity?
    
    @Query("SELECT MAX(page) FROM manga")
    suspend fun getMaxPage(): Int?
    
    @Query("DELETE FROM manga")
    suspend fun deleteAll()
    
    @Query("DELETE FROM manga WHERE insertedAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM manga")
    suspend fun getCount(): Int
} 