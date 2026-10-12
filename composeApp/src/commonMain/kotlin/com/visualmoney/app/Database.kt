package com.visualmoney.app

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.SQLiteDriver
import com.visualmoney.app.data.local.CachedQuoteDao
import com.visualmoney.app.data.local.CachedQuoteEntity
import com.visualmoney.app.data.local.InvestmentReminderDao
import com.visualmoney.app.data.local.InvestmentReminderEntity
import com.visualmoney.app.data.local.ManualInvestmentDao
import com.visualmoney.app.data.local.ManualInvestmentEntity
import com.visualmoney.app.data.local.OnboardingPreferencesDao
import com.visualmoney.app.data.local.OnboardingPreferencesEntity
import com.visualmoney.app.data.local.PortfolioAsset
import com.visualmoney.app.data.local.PortfolioAssetDAO
import com.visualmoney.app.data.local.PortfolioBuyDao
import com.visualmoney.app.data.local.PortfolioBuyEntity
import com.visualmoney.app.data.local.RoomConverters
import com.visualmoney.app.data.local.TrackedAssetDao
import com.visualmoney.app.data.local.TrackedAssetEntity


@Database(
    entities = [
        TrackedAssetEntity::class,
        PortfolioBuyEntity::class,
        ManualInvestmentEntity::class,
        InvestmentReminderEntity::class,
        CachedQuoteEntity::class,
        PortfolioAsset::class,
        OnboardingPreferencesEntity::class
    ],
    version = 2,
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
    abstract fun onboardingPreferencesDao(): OnboardingPreferencesDao
}

@Suppress("KotlinNoActualForExpect","NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
expect fun getSQLDriver():SQLiteDriver
