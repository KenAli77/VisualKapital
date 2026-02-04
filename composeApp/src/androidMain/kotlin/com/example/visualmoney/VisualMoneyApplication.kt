package com.example.visualmoney

import android.app.Application
import android.content.Context
import com.example.visualmoney.di.doInitKoin
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
        doInitKoin {
            androidContext(this@VisualMoneyApplication)
        }
    }
}
