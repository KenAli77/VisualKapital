package com.example.visualmoney.billing

import androidx.compose.runtime.Composable
import com.example.visualmoney.core.Resource

/**
 * Platform-specific factory function for creating Purchases instance.
 */
expect fun getPurchases(): Purchases

/**
 * Common interface for RevenueCat purchases functionality.
 */
interface Purchases {
    /**
     * Configure RevenueCat with the given user ID.
     */
    fun configure(userId: String)
    
    /**
     * Get current customer info including entitlements.
     */
    fun getCustomerInfo(onResult: (Resource<CustomerInfo>) -> Unit)
    
    /**
     * Get subscription management URL.
     */
    fun getManagementUrl(onResult: (Resource<String>) -> Unit)
    
    /**
     * Get available offerings/packages.
     */
    fun getOfferings(onResult: (Resource<List<PackageInfo>>) -> Unit)
    
    /**
     * Purchase a package.
     */
    @Composable
    fun purchasePackage(
        packageId: String,
        offeringId: String,
        onResult: (Resource<CustomerInfo>) -> Unit
    )
}

// Data models

data class PackageInfo(
    val identifier: String,
    val offeringId: String,
    val priceString: String,
    val description: String
)

data class EntitlementInfos(val all: Map<String, EntitlementInfo>)

data class EntitlementInfo(
    val identifier: String,
    val isActive: Boolean,
    val willRenew: Boolean,
    val latestPurchaseDate: Long,
    val originalPurchaseDate: Long,
    val expirationDate: Long?,
    val productIdentifier: String,
    val productPlanIdentifier: String?,
    val isSandbox: Boolean,
    val unsubscribeDetectedAt: Long?,
    val billingIssueDetectedAt: Long?,
)

data class CustomerInfo(
    val originalAppUserId: String,
    val entitlements: EntitlementInfos
)

/**
 * Check if the customer has an active entitlement.
 */
fun CustomerInfo.hasActiveEntitlement(entitlementId: String): Boolean {
    return entitlements.all[entitlementId]?.isActive == true
}

/**
 * Check if the customer has any active premium entitlement.
 */
fun CustomerInfo.isPremium(): Boolean {
    return entitlements.all.values.any { it.isActive }
}
