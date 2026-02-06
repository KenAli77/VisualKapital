package com.example.visualmoney.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Financial calendar events like earnings, dividends, and stock splits.
 */
sealed class FinancialEvent {
    abstract val symbol: String
    abstract val date: LocalDate
    abstract val eventType: EventType

    data class Earnings(
        override val symbol: String,
        override val date: LocalDate,
        val epsEstimated: Double?,
        val epsActual: Double?,
        val revenueEstimated: Double?,
        val revenueActual: Double?,
        val time: String? // "bmo" (before market open) or "amc" (after market close)
    ) : FinancialEvent() {
        override val eventType = EventType.EARNINGS
    }

    data class Dividend(
        override val symbol: String,
        override val date: LocalDate, // ex-dividend date
        val paymentDate: LocalDate?,
        val recordDate: LocalDate?,
        val declarationDate: LocalDate?,
        val amount: Double,
        val yield: Double?
    ) : FinancialEvent() {
        override val eventType = EventType.DIVIDEND
    }

    data class StockSplit(
        override val symbol: String,
        override val date: LocalDate,
        val numerator: Double,
        val denominator: Double
    ) : FinancialEvent() {
        override val eventType = EventType.STOCK_SPLIT
        
        val ratio: String get() = "${numerator.toInt()}:${denominator.toInt()}"
    }
}

enum class EventType {
    EARNINGS,
    DIVIDEND,
    STOCK_SPLIT
}
