package com.visualmoney.app

import android.app.Application
import android.content.Context
import com.visualmoney.app.di.doInitKoin
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
        
        if (BuildKonfig.DEBUG) {
            Purchases.logLevel = LogLevel.DEBUG
        } else {
            Purchases.logLevel = LogLevel.INFO
        }
        Purchases.configure(PurchasesConfiguration(BuildKonfig.RC_API_KEY_ANDROID))
        
        doInitKoin {
            androidContext(this@VisualMoneyApplication)
        }
    }
}
