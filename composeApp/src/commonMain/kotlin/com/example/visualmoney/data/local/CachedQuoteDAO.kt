package com.example.visualmoney.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedQuoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(quote: CachedQuoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(quotes: List<CachedQuoteEntity>)

    @Query("SELECT * FROM cached_quotes WHERE symbol = :symbol LIMIT 1")
    suspend fun get(symbol: String): CachedQuoteEntity?

    @Query("SELECT * FROM cached_quotes WHERE symbol = :symbol LIMIT 1")
    fun observe(symbol: String): Flow<CachedQuoteEntity?>

    @Query("SELECT * FROM cached_quotes")
    fun observeAll(): Flow<List<CachedQuoteEntity>>

    @Query("DELETE FROM cached_quotes WHERE symbol = :symbol")
    suspend fun delete(symbol: String)

    @Query("DELETE FROM cached_quotes")
    suspend fun clearAll()

    // Find quotes that are "stale" (older than cutoff)
    @Query("SELECT * FROM cached_quotes WHERE updatedAtEpochMs < :staleBeforeEpochMs")
    suspend fun getStale(staleBeforeEpochMs: Long): List<CachedQuoteEntity>
}