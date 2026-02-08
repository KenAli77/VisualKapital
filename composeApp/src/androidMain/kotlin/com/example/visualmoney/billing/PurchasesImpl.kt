package com.example.visualmoney.billing

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import com.example.visualmoney.BuildKonfig
import com.example.visualmoney.VisualMoneyApplication
import com.example.visualmoney.core.Resource
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.purchaseWith
import com.revenuecat.purchases.CustomerInfo as RevenueCatCustomerInfo
import com.revenuecat.purchases.EntitlementInfo as RevenueCatEntitlementInfo
import com.revenuecat.purchases.EntitlementInfos as RevenueCatEntitlementInfos
import com.revenuecat.purchases.Purchases as RevenueCatPurchases

class PurchasesImpl : Purchases {

    override fun configure(userId: String) {
        val apiKey = BuildKonfig.RC_API_KEY_ANDROID
        val context = VisualMoneyApplication.context

        RevenueCatPurchases.configure(
            PurchasesConfiguration.Builder(context, apiKey).apply {
                appUserID(userId)
            }.build()
        )
    }

    override fun getCustomerInfo(onResult: (Resource<CustomerInfo>) -> Unit) {
        RevenueCatPurchases.sharedInstance.getCustomerInfoWith { customerInfo ->
            onResult(Resource.Success(customerInfo.asCustomerInfo()))
        }
    }

    override fun getManagementUrl(onResult: (Resource<String>) -> Unit) {
        RevenueCatPurchases.sharedInstance.getCustomerInfoWith {
            val url = it.managementURL?.toString()
            url?.let {
                onResult(Resource.Success(it))
            } ?: onResult(Resource.Error("Error getting management url"))
        }
    }

    override fun getOfferings(onResult: (Resource<List<PackageInfo>>) -> Unit) {
        RevenueCatPurchases.sharedInstance.getOfferingsWith(
            onSuccess = { offerings ->
                val packages = offerings.all.values.flatMap { offering ->
                    offering.availablePackages.map { pkg ->
                        PackageInfo(
                            identifier = pkg.identifier,
                            offeringId = offering.identifier,
                            priceString = pkg.product.price.formatted,
                            description = pkg.product.description
                        )
                    }
                }
                onResult(Resource.Success(packages))
            },
            onError = { error ->
                onResult(Resource.Error(error.message))
            }
        )
    }

    @Composable
    override fun purchasePackage(
        packageId: String,
        offeringId: String,
        onResult: (Resource<CustomerInfo>) -> Unit
    ) {
        val activity = LocalActivity.current

        if (activity == null) {
            onResult(Resource.Error("Activity not found"))
            return
        }

        RevenueCatPurchases.sharedInstance.getOfferingsWith(
            onSuccess = { offerings ->
                val rcOffering = offerings.all[offeringId]
                val rcPackage = rcOffering?.availablePackages?.firstOrNull { pkg ->
                    pkg.identifier == packageId
                }

                if (rcPackage == null) {
                    onResult(Resource.Error("Package not found"))
                    return@getOfferingsWith
                }

                try {
                    val purchaseParams = PurchaseParams.Builder(activity = activity, rcPackage).build()

                    RevenueCatPurchases.sharedInstance.purchaseWith(
                        purchaseParams = purchaseParams,
                        onError = { error, userCancelled ->
                            if (userCancelled) {
                                onResult(Resource.Error("User cancelled"))
                            } else {
                                onResult(Resource.Error(error.message))
                            }
                        },
                        onSuccess = { _, customerInfo ->
                            onResult(Resource.Success(customerInfo.asCustomerInfo()))
                        }
                    )
                } catch (t: Throwable) {
                    onResult(Resource.Error("Purchase failed: ${t.localizedMessage}"))
                }
            },
            onError = { error ->
                onResult(Resource.Error(error.message))
            }
        )
    }

    // Extension functions for mapping RevenueCat types to our domain models

    private fun RevenueCatEntitlementInfos.asEntitlementInfos(): EntitlementInfos {
        return EntitlementInfos(
            all = this.all.mapValues { entry ->
                entry.value.asEntitlementInfo()
            }
        )
    }

    private fun RevenueCatEntitlementInfo.asEntitlementInfo(): EntitlementInfo {
        return EntitlementInfo(
            identifier = this.identifier,
            isActive = this.isActive,
            willRenew = this.willRenew,
            latestPurchaseDate = this.latestPurchaseDate.time,
            originalPurchaseDate = this.originalPurchaseDate.time,
            expirationDate = this.expirationDate?.time,
            productIdentifier = this.productIdentifier,
            productPlanIdentifier = this.productPlanIdentifier,
            isSandbox = this.isSandbox,
            unsubscribeDetectedAt = this.unsubscribeDetectedAt?.time,
            billingIssueDetectedAt = this.billingIssueDetectedAt?.time
        )
    }

    private fun RevenueCatCustomerInfo.asCustomerInfo(): CustomerInfo {
        return CustomerInfo(
            originalAppUserId = this.originalAppUserId,
            entitlements = entitlements.asEntitlementInfos()
        )
    }
}
