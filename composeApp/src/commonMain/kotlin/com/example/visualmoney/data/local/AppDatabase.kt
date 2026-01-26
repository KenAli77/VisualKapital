package com.example.visualmoney.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(
    entities = [
        CachedQuoteEntity::class,
        TrackedAssetEntity::class,
        PortfolioBuyEntity::class,
        ManualInvestmentEntity::class,
        InvestmentReminderEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackedAssetDao(): TrackedAssetDao
    abstract fun portfolioBuyDao(): PortfolioBuyDao
    abstract fun manualInvestmentDao(): ManualInvestmentDao
    abstract fun investmentReminderDao(): InvestmentReminderDao
    abstract fun cachedQuoteDao(): CachedQuoteDao

}