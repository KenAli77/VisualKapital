package com.example.visualmoney

import android.app.Application
import com.example.visualmoney.data.local.CachedQuoteDao
import com.example.visualmoney.data.local.DatabaseSeeder
import com.example.visualmoney.data.local.PortfolioBuyDao
import com.example.visualmoney.di.androidDatabaseModule
import com.example.visualmoney.di.doInitKoin
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext

class VisualMoneyApplication : Application() {
    
    private val portfolioBuyDao: PortfolioBuyDao by inject()
    private val cachedQuoteDao: CachedQuoteDao by inject()
    
    override fun onCreate() {
        super.onCreate()
        
        doInitKoin {
            androidContext(this@VisualMoneyApplication)
            modules(androidDatabaseModule)
        }
        
        // Seed database with mock data for testing
        println("VisualMoneyApplication: Starting database seeding...")
        DatabaseSeeder.seedIfEmpty(portfolioBuyDao, cachedQuoteDao)
    }
}
