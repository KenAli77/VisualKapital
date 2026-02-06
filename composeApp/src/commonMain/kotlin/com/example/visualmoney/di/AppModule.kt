package com.example.visualmoney.di

import androidx.lifecycle.SavedStateHandle
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.visualmoney.assetDetails.AssetDetailsViewModel
import com.example.visualmoney.data.local.AppDatabase
import com.example.visualmoney.data.local.CachedQuoteDao
import com.example.visualmoney.data.local.InvestmentReminderDao
import com.example.visualmoney.data.local.getDatabaseBuilder
import com.example.visualmoney.data.remote.FmpDataSource
import com.example.visualmoney.data.repository.FinancialRepository
import com.example.visualmoney.data.repository.FinancialRepositoryImpl
import com.example.visualmoney.home.HomeViewModel
import com.example.visualmoney.network.provideHttpClientEngine
import com.example.visualmoney.newAsset.NewAssetViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<HttpClient> {
        val engine = provideHttpClientEngine()
        HttpClient(engine) {
            install(Logging) {
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(Auth) {
                // Bearer tokens logic can go here
            }
        }
    }
    single { FmpDataSource(get()) }
    single<AppDatabase> {
        getDatabaseBuilder().apply {
            setDriver(BundledSQLiteDriver())
            setQueryCoroutineContext(Dispatchers.IO)
        }.build()
    }
    single<FinancialRepository> {
        FinancialRepositoryImpl(
            get(),
            get<AppDatabase>().cachedQuoteDao(),
            get<AppDatabase>().portfolioAssetDao()
        )
    }
    viewModelOf(::HomeViewModel)
    viewModelOf(::NewAssetViewModel)
    viewModelOf(::AssetDetailsViewModel)
//    viewModel {
//        NewAssetViewModel(get())
//    }
}


