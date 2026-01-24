package com.example.visualmoney.util

import com.example.visualmoney.BuildKonfig

object LogoUtil {
    private val apiKey = BuildKonfig.LOGO_DEV_KEY

    fun getLogoUrl(symbol: String): String {
        // Basic implementation for stocks (using ticker)
        // For domains, we'd need a way to map symbol -> domain or use search API.
        // Assuming we use the 'token' param pattern if needed, or just domain.
        // Logo.dev Ticker API format: https://img.logo.dev/ticker/{ticker}?token={token}
        
        return "https://img.logo.dev/ticker/${symbol}?token=${apiKey}"
    }

    fun getCryptoLogoUrl(symbol: String): String {
         // Logo.dev Crypto API format roughly matches tickers often, or specific path
         // Checking documentation pattern: https://img.logo.dev/ticker/{symbol}?token={token} usually works for major assets
         return "https://img.logo.dev/ticker/${symbol}?token=${apiKey}"
    }
}
