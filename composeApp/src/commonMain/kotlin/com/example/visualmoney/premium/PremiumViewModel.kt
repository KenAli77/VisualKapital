package com.example.visualmoney.premium

import androidx.lifecycle.ViewModel
import com.revenuecat.purchases.kmp.Purchases
import androidx.lifecycle.viewModelScope
import com.example.visualmoney.domain.model.AssetQuote
import com.example.visualmoney.domain.model.StockNews
import com.example.visualmoney.data.repository.FinancialRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    val riskLevel: RiskLevel = RiskLevel.MEDIUM
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
    "Unknown" to 0xFF9CA3AF
)

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
    
    fun loadPremiumFeatures() {
        if (_state.value.isPremiumLoading) return
        _state.update { it.copy(isPremiumLoading = true) }
        viewModelScope.launch {
            try {
                // 1. Get Portfolio Assets
                val assets = repository.getPortfolioAssets().first()
                if (assets.isEmpty()) {
                    _state.update { it.copy(isPremiumLoading = false) }
                    return@launch
                }
                
                val symbols = assets.map { it.symbol }
                
                // 2. Fetch Profiles & Quotes & News
                val profilesHelper = async { repository.getProfiles(symbols) }
                val quotesHelper = async { repository.getQuotes(symbols) }
                val newsHelper = async { repository.getStockNews(symbols) }
                
                val profiles = profilesHelper.await()
                val quotes = quotesHelper.await()
                val news = newsHelper.await()
                
                val quoteMap = quotes.associateBy { it.symbol }
                val profileMap = profiles.associateBy { it.symbol }
                
                // 3. Calculate Portfolio Value & Holdings
                var totalValue = 0.0
                var totalCost = 0.0
                val holdingsList = mutableListOf<AssetHolding>()
                
                assets.forEach { asset ->
                    val quote = quoteMap[asset.symbol]
                    val profile = profileMap[asset.symbol]
                    val price = quote?.price ?: asset.purchasePrice
                    val value = asset.qty * price
                    val cost = asset.qty * asset.purchasePrice
                    
                    totalValue += value
                    totalCost += cost
                    
                    holdingsList.add(
                        AssetHolding(
                            symbol = asset.symbol,
                            name = asset.name,
                            logoUrl = profile?.image,
                            quantity = asset.qty.toDouble(),
                            currentValue = value,
                            percentage = 0.0, // Will update after total is known
                            dailyChange = quote?.change ?: 0.0,
                            dailyChangePct = quote?.changesPercentage ?: 0.0
                        )
                    )
                }
                
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
                    
                    val sector = profile?.sector ?: "Unknown"
                    val country = profile?.country ?: "Unknown"
                    val assetClass = when {
                        asset.name.contains("ETF", ignoreCase = true) -> "ETF"
                        asset.symbol.contains("BTC") || asset.symbol.contains("ETH") -> "Crypto"
                        else -> "Stocks"
                    }
                    
                    sectorMap[sector] = (sectorMap[sector] ?: 0.0) + value
                    countryMap[country] = (countryMap[country] ?: 0.0) + value
                    assetClassMap[assetClass] = (assetClassMap[assetClass] ?: 0.0) + value
                }
                
                fun mapToExposureItems(map: Map<String, Double>): List<ExposureItem> {
                    return map.entries.map { (name, value) ->
                        ExposureItem(
                            name = name,
                            value = value,
                            percentage = if (totalValue > 0) (value / totalValue) * 100 else 0.0,
                            color = SECTOR_COLORS[name] ?: 0xFF9CA3AF
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
                
                // 7. Calculate Risk Metrics
                // Volatility: Using daily changes from quotes as proxy
                val dailyChanges = quotes.map { it.changesPercentage }
                val avgChange = dailyChanges.average()
                val volatility = if (dailyChanges.size > 1) {
                    sqrt(dailyChanges.map { (it - avgChange) * (it - avgChange) }.average())
                } else 0.0
                
                // Beta: Simplified estimate based on volatility (market volatility ~1%)
                val marketVolatility = 1.0
                val beta = if (marketVolatility > 0) volatility / marketVolatility else 1.0
                
                // Sharpe Ratio: (Return - Risk Free Rate) / Volatility
                val riskFreeRate = 5.0 // Approximate annual risk-free rate
                val annualizedReturn = totalReturnPct
                val sharpeRatio = if (volatility > 0) (annualizedReturn - riskFreeRate) / (volatility * sqrt(252.0)) else 0.0
                
                // Value at Risk (VaR) at 95% confidence: ~1.65 standard deviations
                val valueAtRisk = totalValue * volatility * 1.65 / 100
                
                // 8. Overall Risk Score (0-100, higher = safer)
                val highRiskPct = highRiskAssets.sumOf { it.percentage }
                val concentrationPenalty = concentrationAlerts.size * 10.0
                val volatilityPenalty = (volatility * 5).coerceAtMost(30.0)
                
                val riskScore = (100.0 - highRiskPct / 2 - concentrationPenalty - volatilityPenalty).coerceIn(0.0, 100.0).toInt()
                val riskLevel = when {
                    riskScore >= 70 -> RiskLevel.LOW
                    riskScore >= 40 -> RiskLevel.MEDIUM
                    else -> RiskLevel.HIGH
                }
                
                val metrics = PortfolioMetrics(
                    totalValue = totalValue,
                    totalCost = totalCost,
                    totalReturn = totalReturn,
                    totalReturnPct = totalReturnPct,
                    topGainer = topGainer,
                    topLoser = topLoser,
                    volatility = volatility,
                    beta = beta,
                    sharpeRatio = sharpeRatio,
                    valueAtRisk = valueAtRisk,
                    riskScore = riskScore,
                    riskLevel = riskLevel
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
                        news = news
                    )
                }
                
            } catch (e: Exception) {
                 _state.update { it.copy(isPremiumLoading = false, error = "Failed to load premium data") }
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
