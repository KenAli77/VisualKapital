package com.example.visualmoney

import android.app.Application
import com.example.visualmoney.di.doInitKoin
import org.koin.android.ext.koin.androidContext

class VisualMoneyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        doInitKoin {
            androidContext(this@VisualMoneyApplication)
        }
    }
}
