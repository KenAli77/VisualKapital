package com.visualmoney.app.util

import com.visualmoney.app.BuildKonfig

object LogoUtil {
    private val apiKey = BuildKonfig.LOGO_DEV_KEY

    fun getLogoUrl(symbol: String): String {

        return "https://img.logo.dev/ticker/${symbol}?token=${apiKey}&format=png&theme=dark"
    }

    fun getCryptoLogoUrl(symbol: String): String {
        return "https://img.logo.dev/crypto/${symbol}?token=${apiKey}&format=png&theme=dark"
    }
}
