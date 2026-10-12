package com.visualmoney.app.premium

import androidx.lifecycle.ViewModel
import com.revenuecat.purchases.kmp.Purchases
import androidx.lifecycle.viewModelScope
import com.visualmoney.app.domain.model.AssetQuote
import com.visualmoney.app.domain.model.StockNews
import com.visualmoney.app.data.repository.FinancialRepository
import kotlinx.coroutines.awaitAll
import com.visualmoney.app.assetDetails.ChartRange
import com.visualmoney.app.assetDetails.apiPeriod
import com.visualmoney.app.domain.model.Dividend
import com.visualmoney.app.domain.model.SplitEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import com.visualmoney.app.calendar.now


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.revenuecat.purchases.kmp.models.Package
import kotlin.math.sqrt


// ============ Data Models ============

data class PackageInfo(
    val identifier: String,
    val offeringId: String,
    val priceString: String,
    val title: String,
    val description: String,
    val type: PackageType
)

enum class PackageType {
    MONTHLY, ANNUAL, LIFETIME, UNKNOWN
}

enum class RiskLevel(val label: String) {
    LOW("Low"), MEDIUM("Medium"), HIGH("High")
}

// Advanced Portfolio Metrics
data class PortfolioMetrics(
    // Performance
    val totalValue: Double = 0.0,
    val totalCost: Double = 0.0,
    val totalReturn: Double = 0.0,
    val totalReturnPct: Double = 0.0,
    val topGainer: AssetQuote? = null,
    val topLoser: AssetQuote? = null,
    
    // Risk Metrics
    val volatility: Double = 0.0,      // Standard deviation of daily returns
    val beta: Double = 1.0,             // Market sensitivity
    val sharpeRatio: Double = 0.0,      // Risk-adjusted return
    val valueAtRisk: Double = 0.0,      // 95% VaR (potential daily loss)
    
    // Overall Risk Assessment
    val riskScore: Int = 50,            // 0-100 (higher = safer)
    val riskLevel: RiskLevel = RiskLevel.MEDIUM,
    
    // Dividend Metrics
    val estimatedAnnualIncome: Double = 0.0,
    val currentYield: Double = 0.0,
    val rebalancingSuggestion: String = "Your portfolio looks balanced. Keep monitoring your asset allocation."
)

data class ExposureItem(
    val name: String,
    val value: Double,
    val percentage: Double,
    val color: Long = 0xFF6366F1  // Default purple
)

data class ConcentrationAlert(
    val symbol: String,
    val name: String,
    val logoUrl: String?,
    val percentage: Double,
    val threshold: Double = 10.0
)

data class HighRiskAsset(
    val symbol: String,
    val name: String,
    val logoUrl: String?,
    val percentage: Double,
    val sector: String
)

data class AssetHolding(
    val symbol: String,
    val name: String,
    val logoUrl: String?,
    val quantity: Double,
    val currentPrice: Double,
    val currentValue: Double,
    val percentage: Double,
    val dailyChange: Double,    
    val dailyChangePct: Double
)

// ============ Premium State ============

data class PremiumState(
    // Purchase State
    val packages: List<PackageInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val purchaseSuccess: Boolean = false,
    val shouldPurchase: Boolean = false,
    val isPremium: Boolean = false,
    
    // Premium Features Loading
    val isPremiumLoading: Boolean = false,
    
    // Portfolio Metrics
    val metrics: PortfolioMetrics = PortfolioMetrics(),
    
    // Exposure Data
    val sectorExposure: List<ExposureItem> = emptyList(),
    val countryExposure: List<ExposureItem> = emptyList(),
    val assetClassExposure: List<ExposureItem> = emptyList(),
    
    // Dividend Data
    val dividends: Map<String, List<Dividend>> = emptyMap(),
    val splits: Map<String, List<SplitEvent>> = emptyMap(),
    val dividendCalendar: List<String> = emptyList(),
    
    // Holdings
    val holdings: List<AssetHolding> = emptyList(),
    
    // Alerts & Risk
    val concentrationAlerts: List<ConcentrationAlert> = emptyList(),
    val highRiskAssets: List<HighRiskAsset> = emptyList(),
    
    // News (for reference)
    val news: List<StockNews> = emptyList()
)

// Sector colors for charts
val SECTOR_COLORS = mapOf(
    "Technology" to 0xFF6366F1,
    "Healthcare" to 0xFF22C55E,
    "Financial Services" to 0xFF3B82F6,
    "Consumer Cyclical" to 0xFFF59E0B,
    "Consumer Defensive" to 0xFF84CC16,
    "Industrials" to 0xFF8B5CF6,
    "Energy" to 0xFFEF4444,
    "Real Estate" to 0xFF14B8A6,
    "Communication Services" to 0xFFEC4899,
    "Basic Materials" to 0xFF78716C,
    "Utilities" to 0xFF0EA5E9,
    "Cryptocurrency" to 0xFFF97316,
    "Precious Metals" to 0xFFEAB308, // Gold color
    "Unknown" to 0xFF9CA3AF,
    
    // Asset Classes
    "Stocks" to 0xFF3B82F6, // Blue
    "ETF" to 0xFF06B6D4,    // Cyan
    "Crypto" to 0xFFF97316, // Orange
    "Commodities" to 0xFFEAB308, // Yellow
    
    // Countries (Common ones)
    "US" to 0xFF2563EB,     // Darker Blue
    "Global" to 0xFF10B981, // Emerald
    "China" to 0xFFEF4444,  // Red
    "Europe" to 0xFF6366F1  // Indigo
)

fun getChartColor(name: String): Long {
    return SECTOR_COLORS[name] ?: run {
        // Deterministic color generation for unknown labels
        val hash = name.hashCode()
        val r = (hash and 0xFF0000) shr 16
        val g = (hash and 0x00FF00) shr 8
        val b = (hash and 0x0000FF)
        
        // Ensure color isn't too dark or too light (simple normalization)
        // Or just mask with alpha and use raw hash bytes
        (0xFF000000 or (hash.toLong() and 0xFFFFFF))
    }
}

// ============ ViewModel ============

class PremiumViewModel(
    private val purchases: Purchases,
    private val repository: FinancialRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(PremiumState())
    val state: StateFlow<PremiumState> = _state.asStateFlow()
    
    private var revenueCatPackages: List<Package> = emptyList()
    
    init {
       checkEntitlement()
       loadOfferings()
    }

    private fun checkEntitlement() {
         purchases.getCustomerInfo(
             onSuccess = { customerInfo ->
                 val isPremium = customerInfo.entitlements.active.isNotEmpty()
                 _state.update { it.copy(isPremium = isPremium) }
                 if (isPremium) {
                     loadPremiumFeatures()
                 }
             },
             onError = { _ -> }
         )
    }
    
    fun loadOfferings() {
        _state.update { it.copy(isLoading = true, error = null) }
        
        purchases.getOfferings(
            onError = { error ->
                _state.update { 
                    it.copy(isLoading = false, error = error.message)
                }
            },
            onSuccess = { offerings ->
                val availablePackages = offerings.current?.availablePackages ?: emptyList()
                revenueCatPackages = availablePackages
                val mappedPackages = availablePackages.map { pkg ->
                    val type = when {
                        pkg.identifier.contains("monthly", ignoreCase = true) -> PackageType.MONTHLY
                        pkg.identifier.contains("annual", ignoreCase = true) || pkg.identifier.contains("yearly", ignoreCase = true) -> PackageType.ANNUAL
                        pkg.identifier.contains("lifetime", ignoreCase = true) -> PackageType.LIFETIME
                        else -> PackageType.UNKNOWN
                    }
                    
                    PackageInfo(
                        identifier = pkg.identifier,
                        offeringId = offerings.current?.identifier ?: "default",
                        priceString = pkg.storeProduct.price.formatted,
                        title = pkg.storeProduct.title,
                        description = pkg.storeProduct.title ?: "",
                        type = type
                    )
                }.sortedBy { it.type.ordinal }

                _state.update { 
                    it.copy(packages = mappedPackages, isLoading = false, error = null)
                }
            }
        )
    }
    
    private var premiumJob: kotlinx.coroutines.Job? = null

    fun loadPremiumFeatures() {
        premiumJob?.cancel()
        premiumJob = viewModelScope.launch {
            repository.getPortfolioAssets().collect { assets ->
                 if (assets.isEmpty()) {
                     _state.update { it.copy(isPremiumLoading = false) }
                     return@collect
                 }
                 
                 _state.update { it.copy(isPremiumLoading = true) }
                 
                 try {
                    val symbols = assets.map { it.symbol }
                    
                    // 2. Fetch Profiles & Quotes & News & Charts & Events (in parallel)
                    val profilesHelper = async { repository.getProfiles(symbols) }
                    val quotesHelper = async { repository.getQuotes(symbols) }
                    val newsHelper = async { repository.getStockNews(symbols) }
                    
                    // Fetch 3 months of history for each asset for volatility calculation
                    val chartHelpers = symbols.map { symbol ->
                        async { 
                            symbol to repository.getChart(
                                symbol,
                                ChartRange.THREE_MONTHS.apiPeriod.start,
                                ChartRange.THREE_MONTHS.apiPeriod.end
                            )
                        }
                    }
                    
                    // Parallelize Dividend & Split fetching for ALL assets
                    val dividendHelpers = symbols.map { symbol ->
                        async { symbol to repository.getDividends(symbol) }
                    }
                    val splitHelpers = symbols.map { symbol ->
                        async { symbol to repository.getSplits(symbol) }
                    }
                    
                    val profiles = profilesHelper.await()
                    val quotes = quotesHelper.await()
                    val news = newsHelper.await()
                    val charts = chartHelpers.awaitAll().toMap()
                    val allDividends = dividendHelpers.awaitAll().toMap()
                    val allSplits = splitHelpers.awaitAll().toMap()
                    
                    val quoteMap = quotes.associateBy { it.symbol }
                    val profileMap = profiles.associateBy { it.symbol }
                    
                    // 3. Calculate Portfolio Value & Holdings
                    var totalValue = 0.0
                    var totalCost = 0.0
                    val holdingsList = mutableListOf<AssetHolding>()
                    val dividendCalendar = mutableListOf<String>()
                    
                    var totalProjectedIncome = 0.0

                    assets.forEach { asset ->
                        val quote = quoteMap[asset.symbol]
                        val profile = profileMap[asset.symbol]
                        val price = quote?.price ?: asset.purchasePrice
                        val value = asset.qty * price
                        val cost = asset.qty * asset.purchasePrice
                        
                        val assetDividends = allDividends[asset.symbol] ?: emptyList()
                        
                        if (assetDividends.isNotEmpty()) {
                            // Add to calendar (first 3 upcoming/recent)
                            assetDividends.take(3).forEach { div ->
                                if (div.paymentDate.isNotBlank()) {
                                    dividendCalendar.add("Payment: ${div.paymentDate} - ${asset.symbol} ($${div.dividend})")
                                }
                            }
                            
                            // Calculate Annual Income: Sum dividends from the last 12 months
                            val now = LocalDate.now()
                            val oneYearAgo = LocalDate(now.year - 1, now.month, now.dayOfMonth)
                            
                            val lastYearDividends = assetDividends.filter { div ->
                                try {
                                    val divDate = LocalDate.parse(div.date)
                                    divDate >= oneYearAgo
                                } catch (e: Exception) {
                                    false
                                }
                            }
                            
                            val annualDivPerShare = if (lastYearDividends.isNotEmpty()) {
                                lastYearDividends.sumOf { it.dividend }
                            } else {
                                // Fallback: If no dividends in last year but lastDiv exists, assume quarterly
                                (profile?.lastDiv ?: 0.0) * 4.0
                            }
                            
                            if (annualDivPerShare > 0) {
                                totalProjectedIncome += annualDivPerShare * asset.qty
                            }
                        } else {
                            // Fallback if historical dividends failed but profile has lastDiv
                            val lastDiv = profile?.lastDiv ?: 0.0
                            if (lastDiv > 0) {
                                totalProjectedIncome += (lastDiv * 4.0) * asset.qty // Assume annual estimate
                            }
                        }

                        totalValue += value
                        totalCost += cost
                        
                        holdingsList.add(
                            AssetHolding(
                                symbol = asset.symbol,
                                name = asset.name,
                                logoUrl = profile?.image,
                                quantity = asset.qty.toDouble(),
                                currentPrice = price,
                                currentValue = value,
                                percentage = 0.0, // Will update after total is known
                                dailyChange = quote?.change ?: 0.0,
                                dailyChangePct = quote?.changesPercentage ?: 0.0
                            )
                        )
                    }
                    
                    // Sort calendar by date
                    dividendCalendar.sortDescending()

                    // Update holdings with percentages
                    val holdings = holdingsList.map { 
                        it.copy(percentage = if (totalValue > 0) (it.currentValue / totalValue) * 100 else 0.0) 
                    }.sortedByDescending { it.currentValue }
                    
                    val totalReturn = totalValue - totalCost
                    val totalReturnPct = if (totalCost > 0) (totalReturn / totalCost) * 100 else 0.0
                    
                    val sortedByGain = quotes.sortedByDescending { it.changesPercentage }
                    val topGainer = sortedByGain.firstOrNull()
                    val topLoser = sortedByGain.lastOrNull()
                    
                    // 4. Calculate Exposure
                    val sectorMap = mutableMapOf<String, Double>()
                    val countryMap = mutableMapOf<String, Double>()
                    val assetClassMap = mutableMapOf<String, Double>()
                    
                    assets.forEach { asset ->
                        val price = quoteMap[asset.symbol]?.price ?: asset.purchasePrice
                        val value = asset.qty * price
                        val profile = profileMap[asset.symbol]
                        
                        var sector = profile?.sector
                        var country = profile?.country
                        
                        // Fallback logic for Commodities, Crypto, and ETFs if profile is missing or incomplete
                        if (sector.isNullOrBlank() || sector.contains("Unknown")) {
                            when {
                                // Crypto
                                profile?.exchange == "CRYPTO" || 
                                asset.symbol.contains("-USD") || 
                                asset.symbol.contains("BTC") || 
                                asset.symbol.contains("ETH") -> {
                                    sector = "Cryptocurrency"
                                    country = if (country.isNullOrBlank()) "Global" else country
                                }
                                
                                // ETFs
                                profile?.isEtf == true || 
                                asset.name.contains("ETF", ignoreCase = true) -> {
                                    sector = "Exchange Traded Fund"
                                    country = if (country.isNullOrBlank()) "Global" else country
                                }
                                
                                // Indices
                                asset.symbol.startsWith("^") -> {
                                    sector = "Index"
                                    country = if (country.isNullOrBlank()) "Global" else country
                                }
                                
                                // Forex
                                asset.symbol.contains("=X") -> {
                                    sector = "Currency"
                                    country = "Global"
                                }
                                
                                // Commodities
                                asset.symbol.equals("GC=F", ignoreCase = true) || 
                                asset.symbol.contains("Gold", ignoreCase = true) || 
                                asset.name.contains("Gold", ignoreCase = true) ||
                                asset.symbol.equals("SI=F", ignoreCase = true) || 
                                asset.name.contains("Silver", ignoreCase = true) ||
                                asset.symbol.equals("PL=F", ignoreCase = true) || // Platinum
                                asset.symbol.equals("PA=F", ignoreCase = true) // Palladium
                                -> {
                                    sector = "Precious Metals"
                                    country = "Global"
                                }
                                
                                asset.symbol.equals("CL=F", ignoreCase = true) || 
                                asset.name.contains("Crude Oil", ignoreCase = true) ||
                                asset.symbol.equals("NG=F", ignoreCase = true) || // Natural Gas
                                asset.name.contains("Natural Gas", ignoreCase = true) ||
                                asset.symbol.equals("BZ=F", ignoreCase = true) // Brent Crude
                                -> {
                                    sector = "Energy"
                                    country = "Global"
                                }
                                
                                 asset.symbol.equals("HG=F", ignoreCase = true) || // Copper
                                 asset.name.contains("Copper", ignoreCase = true)
                                -> {
                                    sector = "Basic Materials"
                                    country = "Global"
                                }
                                
                                asset.symbol.equals("ZC=F", ignoreCase = true) || // Corn
                                asset.symbol.equals("ZW=F", ignoreCase = true) || // Wheat
                                asset.symbol.equals("ZS=F", ignoreCase = true)    // Soybeans
                                -> {
                                    sector = "Agricultural"
                                    country = "Global"
                                }

                                else -> {
                                    sector = "Unknown"
                                    country = if (country.isNullOrBlank()) "Unknown" else country
                                }
                            }
                        } else {
                            // Ensure country is not null even if sector is known
                             if (country.isNullOrBlank()) {
                                 country = "Unknown"
                             }
                        }

                        val assetClass = when {
                            asset.name.contains("ETF", ignoreCase = true) || profile?.isEtf == true -> "ETF"
                            sector == "Cryptocurrency" || profile?.exchange == "CRYPTO" -> "Crypto"
                            sector == "Precious Metals" || sector == "Energy" || sector == "Agricultural" -> "Commodities"
                            else -> "Stocks"
                        }

                        
                        sectorMap[sector] = (sectorMap[sector] ?: 0.0) + value
                        if (country?.isNotBlank() == true)
                        countryMap[country] = (countryMap[country] ?: 0.0) + value
                        assetClassMap[assetClass] = (assetClassMap[assetClass] ?: 0.0) + value
                        
                    }
                    
                    fun mapToExposureItems(map: Map<String, Double>): List<ExposureItem> {
                        return map.entries.map { (name, value) ->
                            ExposureItem(
                                name = name,
                                value = value,
                                percentage = if (totalValue > 0) (value / totalValue) * 100 else 0.0,
                                color = getChartColor(name)
                            )
                        }.sortedByDescending { it.percentage }
                    }
                    
                    val sectorExposure = mapToExposureItems(sectorMap)
                    val countryExposure = mapToExposureItems(countryMap)
                    val assetClassExposure = mapToExposureItems(assetClassMap)
                    
                    // 5. Concentration Alerts (positions > 10%)
                    val concentrationAlerts = holdings
                        .filter { it.percentage > 10.0 }
                        .map { 
                            ConcentrationAlert(
                                symbol = it.symbol,
                                name = it.name,
                                logoUrl = it.logoUrl,
                                percentage = it.percentage
                            )
                        }
                    
                    // 6. High Risk Assets (based on sector)
                    val highRiskSectors = setOf("Technology", "Cryptocurrency", "Consumer Cyclical", "Financial Services")
                    val highRiskAssets = assets.mapNotNull { asset ->
                        val profile = profileMap[asset.symbol]
                        val sector = profile?.sector ?: "Unknown"
                        if (sector in highRiskSectors) {
                            val price = quoteMap[asset.symbol]?.price ?: asset.purchasePrice
                            val value = asset.qty * price
                            HighRiskAsset(
                                symbol = asset.symbol,
                                name = asset.name,
                                logoUrl = profile?.image,
                                percentage = if (totalValue > 0) (value / totalValue) * 100 else 0.0,
                                sector = sector
                            )
                        } else null
                    }.sortedByDescending { it.percentage }
                    
                    // 7. Calculate Advanced Risk Metrics using Historical Data
                    
                    // Weighted Beta
                    var weightedBeta = 0.0
                    holdings.forEach { holding ->
                        val profile = profileMap[holding.symbol]
                        val beta = profile?.beta ?: 1.0
                        weightedBeta += beta * (holding.percentage / 100.0)
                    }
                    
                    // Portfolio Volatility (simplified: weighted avg of asset volatilities + correlation assumption)
                    // A true portfolio volatility requires a covariance matrix, but for now we'll do weighted average * diversification factor
                    // Calculate individual asset volatilities from charts
                    var weightedVolatility = 0.0
                    
                    charts.forEach { (symbol, points) ->
                        val holding = holdings.find { it.symbol == symbol } ?: return@forEach
                        if (points.size > 1) {
                             // Calculate daily returns
                             val dailyReturns = points.zipWithNext { a, b -> 
                                 (a.price - b.price) / b.price 
                             }
                             val avgReturn = dailyReturns.average()
                             val variance = dailyReturns.map { (it - avgReturn) * (it - avgReturn) }.average()
                             val stdDev = sqrt(variance) // Daily volatility
                             
                             // Annualized Volatility = Daily * sqrt(252)
                             val annualizedVol = stdDev * 15.87 // sqrt(252) approx 15.87
                             
                             weightedVolatility += annualizedVol * (holding.percentage / 100.0)
                        }
                    }
                    
                    // If no charts (e.g. all crypto or errors), fallback to quotes daily change
                    if (weightedVolatility == 0.0) {
                         val dailyChanges = quotes.map { kotlin.math.abs(it.changesPercentage) / 100.0 }
                         if (dailyChanges.isNotEmpty()) {
                             weightedVolatility = dailyChanges.average() * 15.87 // annualized rough estimate
                         }
                    }
                    
                    // Apply a diversification factor (naive: more assets = less risk)
                    // 1 asset = 1.0, 10 assets = ~0.6
                    val diversificationFactor = 1.0 / sqrt(assets.size.coerceAtLeast(1).toDouble()).coerceAtLeast(1.0) * 0.5 + 0.5
                    val portfolioVolatility = weightedVolatility * diversificationFactor
                    
                    // Sharpe Ratio
                    val riskFreeRate = 0.045 // 4.5%
                    val sharpeRatio = if (portfolioVolatility > 0.001) 
                        (totalReturnPct/100.0 - riskFreeRate) / portfolioVolatility 
                    else 0.0
                    
                    // Value at Risk (VaR)
                    val valueAtRisk = totalValue * portfolioVolatility * 1.65 / 15.87 // Daily 95% VaR (~1.65 sigma)
                    
                    // 8. Overall Risk Score
                    val highRiskPct = highRiskAssets.sumOf { it.percentage }
                    val concentrationPenalty = concentrationAlerts.size * 5.0
                    val volatilityPenalty = (portfolioVolatility * 100.0).coerceAtMost(40.0)
                    
                    val riskScore = (100.0 - (highRiskPct * 0.3) - concentrationPenalty - volatilityPenalty).coerceIn(1.0, 99.0).toInt()
                    val riskLevel = when {
                        riskScore >= 70 -> RiskLevel.LOW
                        riskScore >= 40 -> RiskLevel.MEDIUM
                        else -> RiskLevel.HIGH
                    }
                    
                    // Already calculated totalProjectedIncome in the loop above
                    val currentYield = if (totalValue > 0) (totalProjectedIncome / totalValue) * 100 else 0.0

                    // Generate Dynamic Rebalancing Suggestion
                    var suggestion = "Your portfolio looks balanced. Keep monitoring your asset allocation."
                    val topSector = sectorExposure.maxByOrNull { it.percentage }
                    
                    if (riskLevel == RiskLevel.HIGH) {
                         suggestion = "Your portfolio risk is High. Consider diversifying into defensive sectors like Utilities or Consumer Defensive to lower volatility."
                    } else if ((topSector?.percentage ?: 0.0) > 40.0) {
                        suggestion = "Your portfolio is heavily weighted towards ${topSector?.name} (${topSector?.percentage?.toInt()}%). Consider trimming this position to reduce sector-specific risk."
                    } else if (sharpeRatio < 0.5 && totalReturnPct < 0) {
                         suggestion = "Your risk-adjusted returns are low. Review your underperforming assets and consider reallocating to higher quality growth or dividend stocks."
                    } else if (assetClassExposure.find { it.name == "Crypto" }?.percentage?.let { it > 20 } == true) {
                        suggestion = "You have significant exposure to Crypto (>20%). Ensure you are comfortable with the high volatility associated with this asset class."
                    }

                    val metrics = PortfolioMetrics(
                        totalValue = totalValue,
                        totalCost = totalCost,
                        totalReturn = totalReturn,
                        totalReturnPct = totalReturnPct,
                        topGainer = topGainer,
                        topLoser = topLoser,
                        volatility = portfolioVolatility * 100, // display as %
                        beta = weightedBeta,
                        sharpeRatio = sharpeRatio,
                        valueAtRisk = valueAtRisk,
                        riskScore = riskScore,
                        riskLevel = riskLevel,
                        estimatedAnnualIncome = totalProjectedIncome,
                        currentYield = currentYield,
                        rebalancingSuggestion = suggestion
                    )
                    
                    _state.update { 
                        it.copy(
                            isPremiumLoading = false,
                            metrics = metrics,
                            sectorExposure = sectorExposure,
                            countryExposure = countryExposure,
                            assetClassExposure = assetClassExposure,
                            holdings = holdings,
                            concentrationAlerts = concentrationAlerts,
                            highRiskAssets = highRiskAssets,
                            news = news,
                            dividends = allDividends,
                            splits = allSplits,
                            dividendCalendar = dividendCalendar
                        )
                    }
                    
                } catch (e: Exception) {
                     _state.update { it.copy(isPremiumLoading = false, error = "Failed to load premium data: ${e.message}") }
                }
            }
        }
    }
    
    fun restorePurchases() {
        _state.update { it.copy(isLoading = true, error = null) }
        purchases.restorePurchases(
            onSuccess = { customerInfo ->
                val isPremium = customerInfo.entitlements.active.isNotEmpty()
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        isPremium = isPremium,
                        purchaseSuccess = isPremium
                    )
                }
                if (isPremium) {
                    loadPremiumFeatures()
                }
            },
            onError = { error ->
                _state.update { 
                    it.copy(isLoading = false, error = "Failed to restore: ${error.message}")
                }
            }
        )
    }
    
    fun startPurchaseFlow() {
        _state.update { it.copy(shouldPurchase = true) }
    }
    
    fun purchaseComplete() {
        _state.update { 
            it.copy(
                shouldPurchase = false, 
                purchaseSuccess = true,
                isPremium = true
            )
        }
        loadPremiumFeatures()
    }
    
    fun purchaseFailed(error: String) {
        _state.update { 
            it.copy(shouldPurchase = false, error = error)
        }
    }
    
    fun triggerPurchase(packageInfo: PackageInfo) {
        _state.update { it.copy(isLoading = true, error = null) }
        
        val packageToPurchase = revenueCatPackages.find { it.identifier == packageInfo.identifier }
        
        if (packageToPurchase != null) {
            purchases.purchase(
                 packageToPurchase = packageToPurchase,
                 onError = { error, _ ->
                     purchaseFailed(error.message)
                     _state.update { it.copy(isLoading = false) }
                 },
                 onSuccess = { _, _ ->
                     purchaseComplete()
                     _state.update { it.copy(isLoading = false) }
                 }
            )
        } else {
             purchaseFailed("Package not found")
             _state.update { it.copy(isLoading = false) }
        }
    }

    // Need to update triggerPurchase to actually use the underlying RevenueCat package object.
    // However, PackageInfo is our wrapper. We need to map back or store usage. 
    // Ideally we should have passed the RC Package object but for now let's assume we can fetch it again or simple mock it if complex.
    // Actually, looking at previous code, loadOfferings mapped it. 
    // Let's implement a safer triggerPurchase.
    
    fun resetPurchaseSuccess() {
        _state.update { it.copy(purchaseSuccess = false) }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
