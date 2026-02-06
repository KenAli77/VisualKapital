package com.example.visualmoney.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters


@Database(
    entities = [
        TrackedAssetEntity::class,
        PortfolioBuyEntity::class,
        ManualInvestmentEntity::class,
        InvestmentReminderEntity::class,
        CachedQuoteEntity::class,
        PortfolioAsset::class
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackedAssetDao(): TrackedAssetDao
    abstract fun portfolioBuyDao(): PortfolioBuyDao

    abstract fun portfolioAssetDao(): PortfolioAssetDAO
    abstract fun manualInvestmentDao(): ManualInvestmentDao
    abstract fun investmentReminderDao(): InvestmentReminderDao
    abstract fun cachedQuoteDao(): CachedQuoteDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>