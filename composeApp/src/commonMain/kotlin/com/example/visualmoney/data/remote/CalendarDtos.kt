package com.example.visualmoney.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for FMP Calendar API responses.
 */

@Serializable
data class EarningsCalendarDto(
    val date: String? = null,
    val symbol: String? = null,
    val eps: Double? = null,
    val epsEstimated: Double? = null,
    val time: String? = null, // "bmo" or "amc"
    val revenue: Double? = null,
    val revenueEstimated: Double? = null,
    val fiscalDateEnding: String? = null,
    val updatedFromDate: String? = null
)

@Serializable
data class DividendCalendarDto(
    val date: String? = null, // ex-dividend date
    val symbol: String? = null,
    val dividend: Double? = null,
    val recordDate: String? = null,
    val paymentDate: String? = null,
    val declarationDate: String? = null,
    @SerialName("adjDividend")
    val adjustedDividend: Double? = null,
    val label: String? = null
)

@Serializable
data class StockSplitCalendarDto(
    val date: String? = null,
    val symbol: String? = null,
    val numerator: Double? = null,
    val denominator: Double? = null,
    val label: String? = null
)
