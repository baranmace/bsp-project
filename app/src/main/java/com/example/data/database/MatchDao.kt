package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM esport_matches ORDER BY dateMillis ASC")
    fun getAllMatches(): Flow<List<EsportMatch>>

    @Query("SELECT * FROM esport_matches WHERE id = :id")
    suspend fun getMatchById(id: Int): EsportMatch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: EsportMatch): Long

    @Update
    suspend fun updateMatch(match: EsportMatch)

    @Delete
    suspend fun deleteMatch(match: EsportMatch)
}
