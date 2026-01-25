package com.example.visualmoney.di

import com.example.visualmoney.network.provideHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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
    single { com.example.visualmoney.data.remote.FmpDataSource(get()) }
    single<com.example.visualmoney.data.repository.FinancialRepository> {
        com.example.visualmoney.data.repository.FinancialRepositoryImpl(get())
    }
    
    factory { com.example.visualmoney.home.HomeViewModel(get()) }
}
