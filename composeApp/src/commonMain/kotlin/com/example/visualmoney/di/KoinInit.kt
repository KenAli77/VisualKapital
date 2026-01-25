package com.example.visualmoney.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun doInitKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule)
    }
}
