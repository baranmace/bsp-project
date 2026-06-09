package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BrawlerDao {
    @Query("SELECT * FROM brawler_metas ORDER BY tier ASC, brawlerName ASC")
    fun getAllBrawlers(): Flow<List<BrawlerMeta>>

    @Query("SELECT * FROM brawler_metas WHERE brawlerId = :brawlerId")
    suspend fun getBrawlerById(brawlerId: String): BrawlerMeta?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrawler(brawler: BrawlerMeta)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrawlers(brawlers: List<BrawlerMeta>)

    @Update
    suspend fun updateBrawler(brawler: BrawlerMeta)

    @Query("SELECT COUNT(*) FROM brawler_metas")
    suspend fun getBrawlerCount(): Int
}
