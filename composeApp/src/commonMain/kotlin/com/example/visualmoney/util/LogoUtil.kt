package com.example.visualmoney.util

import com.example.visualmoney.BuildKonfig

object LogoUtil {
    private val apiKey = BuildKonfig.LOGO_DEV_KEY

    fun getLogoUrl(symbol: String): String {

        return "https://img.logo.dev/ticker/${symbol}?token=${apiKey}"
    }

    fun getCryptoLogoUrl(symbol: String): String {
        return "https://img.logo.dev/ticker/${symbol}?token=${apiKey}"
    }
}
