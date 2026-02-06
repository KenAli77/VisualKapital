package com.example.visualmoney.billing

import androidx.compose.runtime.Composable
import cocoapods.RevenueCat.RCCustomerInfo
import cocoapods.RevenueCat.RCEntitlementInfo
import cocoapods.RevenueCat.RCEntitlementInfos
import cocoapods.RevenueCat.RCOffering
import cocoapods.RevenueCat.RCOfferings
import cocoapods.RevenueCat.RCPackage
import cocoapods.RevenueCat.RCPurchases
import cocoapods.RevenueCat.RCPurchases.Companion.sharedPurchases
import cocoapods.RevenueCat.configureWithAPIKey
import com.example.visualmoney.BuildKonfig
import com.example.visualmoney.core.Resource
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.timeIntervalSince1970

@OptIn(ExperimentalForeignApi::class)
internal class PurchasesImpl : Purchases {

    override fun configure(userId: String) {
        val apiKey = BuildKonfig.RC_API_KEY_IOS
        RCPurchases.configureWithAPIKey(apiKey = apiKey, appUserID = userId)
    }

    override fun getCustomerInfo(onResult: (Resource<CustomerInfo>) -> Unit) {
        sharedPurchases().getCustomerInfoWithCompletion { rcCustomerInfo, nsError ->
            if (rcCustomerInfo != null) {
                onResult(Resource.Success(rcCustomerInfo.asCustomerInfo()))
            } else {
                onResult(Resource.Error(nsError?.localizedFailureReason ?: "Unknown error"))
            }
        }
    }

    override fun getManagementUrl(onResult: (Resource<String>) -> Unit) {
        sharedPurchases().getCustomerInfoWithCompletion { rcCustomerInfo, nsError ->
            val url = rcCustomerInfo?.managementURL()?.absoluteString
            url?.let {
                onResult(Resource.Success(it))
            } ?: onResult(Resource.Error("Error getting management url: ${nsError?.localizedFailureReason}"))
        }
    }

    override fun getOfferings(onResult: (Resource<List<PackageInfo>>) -> Unit) {
        RCPurchases.sharedPurchases().getOfferingsWithCompletion { rcOfferings, nsError ->
            if (rcOfferings != null) {
                val packages = rcOfferings.toPackages()
                onResult(Resource.Success(packages))
            } else {
                onResult(Resource.Error(nsError?.localizedFailureReason ?: "Unknown error"))
            }
        }
    }

    @Composable
    override fun purchasePackage(
        packageId: String,
        offeringId: String,
        onResult: (Resource<CustomerInfo>) -> Unit
    ) {
        RCPurchases.sharedPurchases().getOfferingsWithCompletion { rcOfferings, _ ->
            val rcPackage = rcOfferings?.all()?.get(offeringId)
                ?.let { it as? RCOffering }
                ?.availablePackages()
                ?.mapNotNull { it as? RCPackage }
                ?.firstOrNull { it.identifier() == packageId }

            if (rcPackage != null) {
                RCPurchases.sharedPurchases()
                    .purchasePackage(rcPackage) { _, rcCustomerInfo, error, userCancelled ->
                        when {
                            userCancelled -> {
                                onResult(Resource.Error("User cancelled"))
                            }
                            rcCustomerInfo != null -> {
                                onResult(Resource.Success(rcCustomerInfo.asCustomerInfo()))
                            }
                            else -> {
                                onResult(Resource.Error(error?.localizedFailureReason ?: "Unknown error"))
                            }
                        }
                    }
            } else {
                onResult(Resource.Error("Package not found"))
            }
        }
    }
}

// Extension functions for mapping RevenueCat iOS types to domain models

@OptIn(ExperimentalForeignApi::class)
private fun RCOfferings.toPackages(): List<PackageInfo> {
    return all().values.mapNotNull { it as? RCOffering }.flatMap { offering ->
        offering.availablePackages().mapNotNull { pkg ->
            pkg as? RCPackage
        }.map { rcPackage ->
            PackageInfo(
                identifier = rcPackage.identifier(),
                offeringId = offering.identifier(),
                priceString = rcPackage.storeProduct().localizedPriceString(),
                description = rcPackage.storeProduct().localizedDescription()
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun RCEntitlementInfos.asEntitlementInfos(): EntitlementInfos {
    val entitlementInfos: Map<String, EntitlementInfo> = this.all().filter { entry ->
        entry.key is String && entry.value is RCEntitlementInfo
    }.map { entry ->
        val key = entry.key as String
        val value = entry.value as RCEntitlementInfo
        key to value.asEntitlementInfo()
    }.toMap()

    return EntitlementInfos(all = entitlementInfos)
}

@OptIn(ExperimentalForeignApi::class)
private fun RCEntitlementInfo.asEntitlementInfo(): EntitlementInfo {
    return EntitlementInfo(
        identifier = this.identifier(),
        isActive = this.isActive(),
        willRenew = this.willRenew(),
        latestPurchaseDate = this.latestPurchaseDate()?.timeIntervalSince1970?.toLong() ?: 0L,
        originalPurchaseDate = this.originalPurchaseDate()?.timeIntervalSince1970?.toLong() ?: 0L,
        expirationDate = this.expirationDate()?.timeIntervalSince1970?.toLong(),
        productIdentifier = this.productIdentifier(),
        productPlanIdentifier = this.productPlanIdentifier(),
        isSandbox = this.isSandbox(),
        unsubscribeDetectedAt = this.unsubscribeDetectedAt()?.timeIntervalSince1970?.toLong(),
        billingIssueDetectedAt = this.billingIssueDetectedAt()?.timeIntervalSince1970?.toLong()
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun RCCustomerInfo.asCustomerInfo(): CustomerInfo {
    return CustomerInfo(
        originalAppUserId = originalAppUserId(),
        entitlements = entitlements().asEntitlementInfos()
    )
}
