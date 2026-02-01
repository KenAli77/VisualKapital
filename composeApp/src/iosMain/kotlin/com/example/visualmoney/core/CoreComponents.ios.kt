package com.example.visualmoney.core

import platform.Foundation.ISOCountryCodes
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.currentLocale

actual fun getCountries(): List<Country> {
    val locale = NSLocale.currentLocale
    return NSLocale.ISOCountryCodes.mapNotNull { code ->
        if (code is String) {
            val name = locale.displayNameForKey(NSLocaleCountryCode, code) ?: return@mapNotNull null

            Country(
                countryCode = code,
                displayText = code.toFlagEmoji() + " " + name,
            )

        } else null

    }.sortedBy { it.displayText }
}


private fun codePointToString(codePoint: Int): String {
    return if (codePoint <= 0xFFFF) {
        codePoint.toChar().toString()
    } else {
        val cp = codePoint - 0x10000
        val highSurrogate = 0xD800 + (cp shr 10)
        val lowSurrogate = 0xDC00 + (cp and 0x3FF)
        "${highSurrogate.toChar()}${lowSurrogate.toChar()}"
    }
}

fun String.toFlagEmoji(): String {
    return ""
//    val cc = this.trim().uppercase()
//    if (cc.length != 2) return ""
//
//    val base = 0x1F1E6
//    val a = 'A'.code
//
//    val first = base + (cc[0].code - a)
//    val second = base + (cc[1].code - a)
//
//    return codePointToString(first) + codePointToString(second)
}