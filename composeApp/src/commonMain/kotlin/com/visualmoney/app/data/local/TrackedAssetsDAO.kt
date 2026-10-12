package com.visualmoney.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedAssetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(asset: TrackedAssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(assets: List<TrackedAssetEntity>)

    @Query("SELECT * FROM tracked_assets WHERE symbol = :symbol LIMIT 1")
    suspend fun get(symbol: String): TrackedAssetEntity?

    @Query("SELECT * FROM tracked_assets WHERE symbol = :symbol LIMIT 1")
    fun observe(symbol: String): Flow<TrackedAssetEntity?>

    @Query("SELECT * FROM tracked_assets ORDER BY updatedAtEpochMs DESC")
    fun observeAll(): Flow<List<TrackedAssetEntity>>

    @Query("DELETE FROM tracked_assets WHERE symbol = :symbol")
    suspend fun delete(symbol: String)
}