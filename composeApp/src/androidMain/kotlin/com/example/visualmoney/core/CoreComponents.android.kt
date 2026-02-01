package com.example.visualmoney.core

import java.util.Locale

actual fun getCountries(): List<Country> {
    val isoCountryCodes: Array<String> = Locale.getISOCountries()
    val countries: ArrayList<Country> = arrayListOf()

    for (countryCode in isoCountryCodes) {
        val locale = Locale("", countryCode)
        val countryName: String = locale.displayCountry
        val flagOffset = 0x1F1E6
        val asciiOffset = 0x41
        val firstChar = Character.codePointAt(countryCode, 0) - asciiOffset + flagOffset
        val secondChar = Character.codePointAt(countryCode, 1) - asciiOffset + flagOffset
        val flag = String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
        val displayText = "$flag $countryName"
        countries.add(Country(countryCode, displayText))
    }
    return countries
}