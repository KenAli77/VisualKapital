package com.example.visualmoney.di

import androidx.room.Room
import com.example.visualmoney.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidDatabaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "visual_money.db"
        ).build()
    }
    
    single { get<AppDatabase>().cachedQuoteDao() }
    single { get<AppDatabase>().portfolioBuyDao() }
}
