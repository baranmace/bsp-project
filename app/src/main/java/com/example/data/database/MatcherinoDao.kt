package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MatcherinoDao {
    @Query("SELECT * FROM matcherinos ORDER BY id DESC")
    fun getAllMatcherinos(): Flow<List<Matcherino>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatcherino(matcherino: Matcherino): Long

    @Update
    suspend fun updateMatcherino(matcherino: Matcherino)

    @Delete
    suspend fun deleteMatcherino(matcherino: Matcherino)
}
