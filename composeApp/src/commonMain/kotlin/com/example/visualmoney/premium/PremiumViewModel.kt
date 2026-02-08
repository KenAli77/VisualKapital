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
    
    data class PremiumState(
        val packages: List<PackageInfo> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val purchaseSuccess: Boolean = false,
        val shouldPurchase: Boolean = false,
        val isPremium: Boolean = false,
        
        // Premium Features Data
        val isPremiumLoading: Boolean = false,
        val portfolioAnalysis: PortfolioAnalysis? = null,
        val sectorExposure: Map<String, Double> = emptyMap(),
        val countryExposure: Map<String, Double> = emptyMap(),
        val news: List<StockNews> = emptyList()
    )
    
    data class PortfolioAnalysis(
        val totalValue: Double,
        val totalGainLoss: Double,
        val totalGainLossPct: Double,
        val topGainer: AssetQuote? = null,
        val topLoser: AssetQuote? = null
    )
    
    class PremiumViewModel(
        private val purchases: Purchases,
        private val repository: FinancialRepository
    ) : ViewModel() {
        
        private val _state = MutableStateFlow(PremiumState())
        val state: StateFlow<PremiumState> = _state.asStateFlow()
        
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
                    
                    // 3. Calculate Analysis
                    var totalValue = 0.0
                    var totalCost = 0.0
                    
                    val quoteMap = quotes.associateBy { it.symbol }
                    
                    assets.forEach { asset ->
                        val price = quoteMap[asset.symbol]?.price ?: asset.purchasePrice
                        totalValue += asset.qty * price
                        totalCost += asset.qty * asset.purchasePrice
                    }
                    
                    val totalGainLoss = totalValue - totalCost
                    val totalGainLossPct = if (totalCost > 0) (totalGainLoss / totalCost) * 100 else 0.0
                    
                    val sortedByGain = quotes.sortedByDescending { it.changesPercentage }
                    val topGainer = sortedByGain.firstOrNull()
                    val topLoser = sortedByGain.lastOrNull()
                    
                    // 4. Calculate Exposure
                    val profileMap = profiles.associateBy { it.symbol }
                    val sectorMap = mutableMapOf<String, Double>()
                    val countryMap = mutableMapOf<String, Double>()
                    
                    assets.forEach { asset ->
                        val price = quoteMap[asset.symbol]?.price ?: asset.purchasePrice
                        val value = asset.qty * price
                        val profile = profileMap[asset.symbol]
                        
                        val sector = profile?.sector ?: "Unknown"
                        val country = profile?.country ?: "Unknown"
                        
                        sectorMap[sector] = (sectorMap[sector] ?: 0.0) + value
                        countryMap[country] = (countryMap[country] ?: 0.0) + value
                    }
                    
                    // Convert to Percentage
                    val sectorExposure = sectorMap.mapValues { (it.value / totalValue) * 100 }
                    val countryExposure = countryMap.mapValues { (it.value / totalValue) * 100 }
                    
                    _state.update { 
                        it.copy(
                            isPremiumLoading = false,
                            portfolioAnalysis = PortfolioAnalysis(
                                totalValue = totalValue,
                                totalGainLoss = totalGainLoss,
                                totalGainLossPct = totalGainLossPct,
                                topGainer = topGainer,
                                topLoser = topLoser
                            ),
                            sectorExposure = sectorExposure,
                            countryExposure = countryExposure,
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
                    _state.update { it.copy(isLoading = false, isPremium = isPremium) }
                    if (isPremium) {
                        loadPremiumFeatures()
                        onPurchaseSuccess()
                    } else {
                        onPurchaseError("No active subscriptions found to restore")
                    }
                },
                onError = { error ->
                    onPurchaseError(error.message)
                }
            )
        }

        fun triggerPurchase(packageInfo: PackageInfo) {
            _state.update { it.copy(shouldPurchase = false, isLoading = true) }
    
            purchases.getOfferings(
                onError = { _ -> onPurchaseError("Could not find offering") },
                onSuccess = { offerings ->
                   val packageToBuy = offerings.current?.availablePackages?.firstOrNull { 
                       it.identifier == packageInfo.identifier 
                   }
                   
                   if (packageToBuy != null) {
                       purchases.purchase(
                           packageToPurchase = packageToBuy,
                           onError = { error, userCancelled ->
                               if (userCancelled) {
                                   _state.update { it.copy(isLoading = false) }
                               } else {
                                   onPurchaseError(error.message)
                               }
                           },
                           onSuccess = { _, customerInfo ->
                               val isPremium = customerInfo.entitlements.active.isNotEmpty()
                               _state.update { it.copy(purchaseSuccess = true, shouldPurchase = false, isLoading = false, isPremium = isPremium) }
                               if (isPremium) loadPremiumFeatures()
                           }
                       )
                   } else {
                       onPurchaseError("Package not found")
                   }
                }
            )
        }
        
        fun onPurchaseSuccess() {
            _state.update { it.copy(purchaseSuccess = true, shouldPurchase = false, isLoading = false) }
        }
        
        fun onPurchaseError(message: String) {
            _state.update { it.copy(error = message, shouldPurchase = false, isLoading = false) }
        }
        
        fun clearError() {
            _state.update { it.copy(error = null) }
        }
        
        fun resetPurchaseSuccess() {
            _state.update { it.copy(purchaseSuccess = false) }
        }
    }
