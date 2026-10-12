package com.visualmoney.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioAssetDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(asset: PortfolioAsset)

    @Query("SELECT * FROM portfolio_assets")
    fun observeAllAssets(): Flow<List<PortfolioAsset>>

    @Query("SELECT * FROM portfolio_assets WHERE symbol = :symbol")
    fun observeAsset(symbol: String): Flow<PortfolioAsset?>

    @Query("DELETE FROM portfolio_assets WHERE symbol = :symbol")
    suspend fun deleteAsset(symbol: String)

    @Query("DELETE FROM portfolio_assets")
    suspend fun deleteAllAssets()
}



