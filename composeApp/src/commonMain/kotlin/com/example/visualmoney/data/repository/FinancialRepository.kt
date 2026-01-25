package com.example.visualmoney.data.repository

import com.example.visualmoney.data.remote.FmpDataSource
import com.example.visualmoney.domain.model.AssetProfile
import com.example.visualmoney.domain.model.AssetQuote
import com.example.visualmoney.domain.model.ChartPoint

interface FinancialRepository {
    suspend fun getQuote(symbol: String): AssetQuote
    suspend fun getQuotes(symbols: List<String>): List<AssetQuote>
    suspend fun getProfile(symbol: String): AssetProfile
    suspend fun getTopGainers(): List<AssetQuote>
    suspend fun getTopLosers(): List<AssetQuote>
    suspend fun getCommodities(): List<AssetQuote>
    suspend fun getChart(symbol: String): List<ChartPoint>
}

class FinancialRepositoryImpl(
    private val remoteSource: FmpDataSource
) : FinancialRepository {
    override suspend fun getQuote(symbol: String): AssetQuote = remoteSource.getQuote(symbol)
    
    override suspend fun getQuotes(symbols: List<String>): List<AssetQuote> = remoteSource.getQuotes(symbols)

    override suspend fun getProfile(symbol: String): AssetProfile = remoteSource.getProfile(symbol)

    override suspend fun getTopGainers(): List<AssetQuote> = remoteSource.getTopGainers()

    override suspend fun getTopLosers(): List<AssetQuote> = remoteSource.getTopLosers()
    
    override suspend fun getCommodities(): List<AssetQuote> = remoteSource.getCommodities()
    override suspend fun getChart(symbol: String): List<ChartPoint> = remoteSource.getChart(symbol)
}
