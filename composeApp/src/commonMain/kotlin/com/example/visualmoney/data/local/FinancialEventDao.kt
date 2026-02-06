package com.example.visualmoney.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.visualmoney.domain.model.EventType
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(events: List<FinancialEventEntity>)

    /**
     * Observe all events for given symbols within a date range.
     */
    @Query("""
        SELECT * FROM financial_events 
        WHERE symbol IN (:symbols) 
          AND date >= :fromDate 
          AND date <= :toDate
        ORDER BY date ASC
    """)
    fun observeEventsForSymbols(
        symbols: List<String>,
        fromDate: String,
        toDate: String
    ): Flow<List<FinancialEventEntity>>

    /**
     * Get stale events for given symbols (older than cutoff).
     */
    @Query("""
        SELECT DISTINCT symbol FROM financial_events 
        WHERE symbol IN (:symbols) 
          AND updatedAtEpochMs < :cutoffMs
    """)
    suspend fun getStaleSymbols(symbols: List<String>, cutoffMs: Long): List<String>

    /**
     * Check which symbols have any cached events.
     */
    @Query("SELECT DISTINCT symbol FROM financial_events WHERE symbol IN (:symbols)")
    suspend fun getCachedSymbols(symbols: List<String>): List<String>

    /**
     * Delete all events for a symbol (used before refresh).
     */
    @Query("DELETE FROM financial_events WHERE symbol = :symbol")
    suspend fun deleteForSymbol(symbol: String)

    /**
     * Delete old cached events.
     */
    @Query("DELETE FROM financial_events WHERE updatedAtEpochMs < :cutoffMs")
    suspend fun deleteStale(cutoffMs: Long)
}
