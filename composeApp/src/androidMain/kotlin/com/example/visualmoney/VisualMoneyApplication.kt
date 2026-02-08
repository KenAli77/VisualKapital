package com.example.visualmoney

import android.app.Application
import android.content.Context
import com.example.visualmoney.di.doInitKoin
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import org.koin.android.ext.koin.androidContext

class VisualMoneyApplication : Application() {

    companion object {
        @JvmStatic
        lateinit var instance: VisualMoneyApplication
            private set

        val context: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(PurchasesConfiguration(BuildKonfig.RC_API_KEY_ANDROID))
        
        doInitKoin {
            androidContext(this@VisualMoneyApplication)
        }
    }
}
