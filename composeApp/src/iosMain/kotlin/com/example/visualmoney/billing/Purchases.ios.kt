package com.example.visualmoney.billing

actual fun getPurchases(): Purchases {
    return PurchasesImpl()
}
