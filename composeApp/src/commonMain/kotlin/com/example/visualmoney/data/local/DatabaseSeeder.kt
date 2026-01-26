package com.example.visualmoney.data.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * Seeds the database with mock portfolio positions and cached quotes for development/testing.
 * This should only be used in debug builds.
 */
object DatabaseSeeder {
    
    fun seedIfEmpty(
        portfolioBuyDao: PortfolioBuyDao,
        cachedQuoteDao: CachedQuoteDao
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            println("DatabaseSeeder: Checking if seeding is needed...")
            
            // Check if database already has data - if so, skip seeding
            val existingPositions = portfolioBuyDao.observePositions().first()
            if (existingPositions.isNotEmpty()) {
                println("DatabaseSeeder: Database already has ${existingPositions.size} positions, skipping seeding")
                return@launch
            }
            
            println("DatabaseSeeder: Database is empty, seeding mock data...")
            
            // Seed portfolio positions
            seedPortfolioPositions(portfolioBuyDao)
            
            // Seed cached quotes
            seedCachedQuotes(cachedQuoteDao)
            
            println("DatabaseSeeder: Seeding complete!")
        }
    }
    
    private suspend fun seedPortfolioPositions(dao: PortfolioBuyDao) {
        val now = Clock.System.now().toEpochMilliseconds()
        
        val mockBuys = listOf(
            // Apple - 10 shares bought at $150
            PortfolioBuyEntity(
                id = "mock-aapl-1",
                symbol = "AAPL",
                type = AssetType.EQUITY,
                quantity = 10.0,
                pricePerUnit = 150.0,
                currency = "USD",
                fee = 5.0,
                executedAtEpochMs = now - 30 * 24 * 60 * 60 * 1000L // 30 days ago
            ),
            // Tesla - 5 shares at $250
            PortfolioBuyEntity(
                id = "mock-tsla-1",
                symbol = "TSLA",
                type = AssetType.EQUITY,
                quantity = 5.0,
                pricePerUnit = 250.0,
                currency = "USD",
                fee = 5.0,
                executedAtEpochMs = now - 20 * 24 * 60 * 60 * 1000L
            ),
            // Amazon - 3 shares at $180
            PortfolioBuyEntity(
                id = "mock-amzn-1",
                symbol = "AMZN",
                type = AssetType.EQUITY,
                quantity = 3.0,
                pricePerUnit = 180.0,
                currency = "USD",
                fee = 5.0,
                executedAtEpochMs = now - 15 * 24 * 60 * 60 * 1000L
            ),
            // Google - 2 shares at $140
            PortfolioBuyEntity(
                id = "mock-googl-1",
                symbol = "GOOGL",
                type = AssetType.EQUITY,
                quantity = 2.0,
                pricePerUnit = 140.0,
                currency = "USD",
                fee = 5.0,
                executedAtEpochMs = now - 10 * 24 * 60 * 60 * 1000L
            ),
            // Microsoft - 8 shares at $350
            PortfolioBuyEntity(
                id = "mock-msft-1",
                symbol = "MSFT",
                type = AssetType.EQUITY,
                quantity = 8.0,
                pricePerUnit = 350.0,
                currency = "USD",
                fee = 5.0,
                executedAtEpochMs = now - 60 * 24 * 60 * 60 * 1000L
            ),
            // Nvidia - 4 shares at $600
            PortfolioBuyEntity(
                id = "mock-nvda-1",
                symbol = "NVDA",
                type = AssetType.EQUITY,
                quantity = 4.0,
                pricePerUnit = 600.0,
                currency = "USD",
                fee = 5.0,
                executedAtEpochMs = now - 45 * 24 * 60 * 60 * 1000L
            ),
            // Bitcoin - 0.5 BTC at $40,000
            PortfolioBuyEntity(
                id = "mock-btc-1",
                symbol = "BTCUSD",
                type = AssetType.CRYPTO,
                quantity = 0.5,
                pricePerUnit = 40000.0,
                currency = "USD",
                fee = 20.0,
                executedAtEpochMs = now - 90 * 24 * 60 * 60 * 1000L
            ),
            // Ethereum - 2 ETH at $2,500
            PortfolioBuyEntity(
                id = "mock-eth-1",
                symbol = "ETHUSD",
                type = AssetType.CRYPTO,
                quantity = 2.0,
                pricePerUnit = 2500.0,
                currency = "USD",
                fee = 10.0,
                executedAtEpochMs = now - 60 * 24 * 60 * 60 * 1000L
            )
        )
        
        println("DatabaseSeeder: Inserting ${mockBuys.size} portfolio buys")
        dao.upsertAll(mockBuys)
    }
    
    private suspend fun seedCachedQuotes(dao: CachedQuoteDao) {
        // Make quotes 6 hours old so they appear STALE and trigger API refresh
        val sixHoursAgo = Clock.System.now().toEpochMilliseconds() - (6 * 60 * 60 * 1000L)
        println("DatabaseSeeder: Creating STALE quotes with timestamp 6 hours ago to test API refresh")
        
        val mockQuotes = listOf(
            // Apple - up 2.5%
            CachedQuoteEntity(
                symbol = "AAPL",
                price = 175.50,
                changePct = 2.5,
                change = 4.28,
                dayLow = 172.00,
                dayHigh = 177.00,
                yearLow = 140.00,
                yearHigh = 200.00,
                volume = 50000000,
                marketCap = 2800000000000.0,
                updatedAtEpochMs = sixHoursAgo
            ),
            // Tesla - up 5.2%
            CachedQuoteEntity(
                symbol = "TSLA",
                price = 285.00,
                changePct = 5.2,
                change = 14.10,
                dayLow = 275.00,
                dayHigh = 290.00,
                yearLow = 150.00,
                yearHigh = 310.00,
                volume = 80000000,
                marketCap = 900000000000.0,
                updatedAtEpochMs = sixHoursAgo
            ),
            // Amazon - down 1.8%
            CachedQuoteEntity(
                symbol = "AMZN",
                price = 195.00,
                changePct = -1.8,
                change = -3.57,
                dayLow = 193.00,
                dayHigh = 200.00,
                yearLow = 150.00,
                yearHigh = 220.00,
                volume = 35000000,
                marketCap = 2000000000000.0,
                updatedAtEpochMs = sixHoursAgo
            ),
            // Google - up 0.8%
            CachedQuoteEntity(
                symbol = "GOOGL",
                price = 155.00,
                changePct = 0.8,
                change = 1.23,
                dayLow = 153.00,
                dayHigh = 156.00,
                yearLow = 120.00,
                yearHigh = 165.00,
                volume = 25000000,
                marketCap = 1900000000000.0,
                updatedAtEpochMs = sixHoursAgo
            ),
            // Microsoft - up 1.5%
            CachedQuoteEntity(
                symbol = "MSFT",
                price = 420.0,
                changePct = 1.5,
                change = 6.21,
                dayLow = 415.0,
                dayHigh = 425.0,
                yearLow = 300.0,
                yearHigh = 450.0,
                volume = 20000000,
                marketCap = 3100000000000.0,
                updatedAtEpochMs = sixHoursAgo
            ),
            // Nvidia - up 7.3%
            CachedQuoteEntity(
                symbol = "NVDA",
                price = 850.0,
                changePct = 7.3,
                change = 57.80,
                dayLow = 820.0,
                dayHigh = 860.0,
                yearLow = 400.0,
                yearHigh = 900.0,
                volume = 40000000,
                marketCap = 2100000000000.0,
                updatedAtEpochMs = sixHoursAgo
            ),
            // Bitcoin - up 8.5%
            CachedQuoteEntity(
                symbol = "BTCUSD",
                price = 105000.0,
                changePct = 8.5,
                change = 8235.29,
                dayLow = 100000.0,
                dayHigh = 107000.0,
                yearLow = 40000.0,
                yearHigh = 110000.0,
                volume = 50000000000,
                marketCap = 2000000000000.0,
                updatedAtEpochMs = sixHoursAgo
            ),
            // Ethereum - down 2.1%
            CachedQuoteEntity(
                symbol = "ETHUSD",
                price = 3200.0,
                changePct = -2.1,
                change = -68.57,
                dayLow = 3150.0,
                dayHigh = 3350.0,
                yearLow = 2000.0,
                yearHigh = 4000.0,
                volume = 15000000000,
                marketCap = 380000000000.0,
                updatedAtEpochMs = sixHoursAgo
            )
        )
        
        println("DatabaseSeeder: Inserting ${mockQuotes.size} cached quotes")
        dao.upsertAll(mockQuotes)
    }
}
