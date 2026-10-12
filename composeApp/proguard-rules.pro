# RevenueCat ProGuard rules
-keep class com.revenuecat.purchases.** { *; }
-dontwarn com.revenuecat.purchases.**

# If you're using RevenueCat Paywalls
-keep class com.revenuecat.purchases.ui.revenuecatui.** { *; }
-dontwarn com.revenuecat.purchases.ui.revenuecatui.**

# The specific missing class mentioned in the error
-dontwarn com.revenuecat.purchases.paywalls.components.common.ComponentOverride$Condition$MultipleIntroOffers
