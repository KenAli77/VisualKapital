package com.visualmoney.app.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.visualmoney.app.AppDatabase
import com.visualmoney.app.assetDetails.AssetDetailsViewModel
import com.visualmoney.app.calendar.CalendarScreenViewModel
import com.visualmoney.app.data.remote.FmpDataSource
import com.visualmoney.app.data.repository.FinancialRepository
import com.visualmoney.app.data.repository.FinancialRepositoryImpl
import com.visualmoney.app.data.repository.InvestmentReminderRepository
import com.visualmoney.app.data.repository.InvestmentReminderRepositoryImpl
import com.visualmoney.app.getDatabaseBuilder
import com.visualmoney.app.home.HomeViewModel
import com.visualmoney.app.network.provideHttpClientEngine
import com.visualmoney.app.newAsset.NewAssetViewModel
import com.visualmoney.app.onboarding.OnboardingViewModel
import com.visualmoney.app.premium.PremiumViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json
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
    single<InvestmentReminderRepository> {
        InvestmentReminderRepositoryImpl(
            get<AppDatabase>().investmentReminderDao(),
        )
    }
    single { get<AppDatabase>().onboardingPreferencesDao() }
    single { com.revenuecat.purchases.kmp.Purchases.sharedInstance }
    viewModelOf(::HomeViewModel)
    viewModelOf(::NewAssetViewModel)
    viewModelOf(::AssetDetailsViewModel)
    viewModelOf(::CalendarScreenViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::PremiumViewModel)
}


