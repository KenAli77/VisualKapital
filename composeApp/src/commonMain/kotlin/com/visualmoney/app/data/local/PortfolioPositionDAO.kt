package com.visualmoney.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


data class PortfolioPositionRow(
    val symbol: String,
    val type: String,            // enum stored as String via converter (Room returns as String in raw queries)
    val totalQuantity: Double,
    val costBasis: Double        // sum(qty*price + fee)
) {
    val avgCost: Double get() = if (totalQuantity == 0.0) 0.0 else costBasis / totalQuantity
}
@Dao
interface PortfolioBuyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(buy: PortfolioBuyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(buys: List<PortfolioBuyEntity>)

    @Query("SELECT * FROM portfolio_buys WHERE symbol = :symbol ORDER BY executedAtEpochMs DESC")
    fun observeBuysForSymbol(symbol: String): Flow<List<PortfolioBuyEntity>>

    @Query("SELECT * FROM portfolio_buys ORDER BY executedAtEpochMs DESC")
    fun observeAllBuys(): Flow<List<PortfolioBuyEntity>>

    @Query("DELETE FROM portfolio_buys WHERE id = :id")
    suspend fun deleteBuy(id: String)

    @Query("DELETE FROM portfolio_buys WHERE symbol = :symbol")
    suspend fun deleteAllForSymbol(symbol: String)

    // Derived positions for portfolio list
    @Query(
        """
        SELECT 
            symbol,
            type,
            SUM(quantity) AS totalQuantity,
            SUM(quantity * pricePerUnit + fee) AS costBasis
        FROM portfolio_buys
        GROUP BY symbol, type
        ORDER BY costBasis DESC
        """
    )
    fun observePositions(): Flow<List<PortfolioPositionRow>>
}