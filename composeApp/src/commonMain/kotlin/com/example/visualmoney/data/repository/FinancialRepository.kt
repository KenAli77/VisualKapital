package com.example.visualmoney.data.repository

import com.example.visualmoney.data.remote.FmpDataSource
import com.example.visualmoney.domain.model.Asset
import com.example.visualmoney.domain.model.AssetProfile
import com.example.visualmoney.domain.model.AssetQuote
import com.example.visualmoney.domain.model.Stock

interface FinancialRepository {
    suspend fun getQuote(symbol: String): AssetQuote
    suspend fun getQuotes(symbols: List<String>): List<AssetQuote>
    suspend fun getProfile(symbol: String): AssetProfile
    suspend fun getTopGainers(): List<AssetQuote>
    suspend fun getTopLosers(): List<AssetQuote>
    suspend fun getCommodities(): List<AssetQuote>
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
}
