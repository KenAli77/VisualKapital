package com.example.visualmoney.data.local

import androidx.room.Entity
import androidx.room.Index
import com.example.visualmoney.domain.model.EventType
import com.example.visualmoney.domain.model.FinancialEvent
import kotlinx.datetime.LocalDate

/**
 * Cached financial event from FMP API.
 * Composite key: (symbol, eventType, date).
 */
@Entity(
    tableName = "financial_events",
    primaryKeys = ["symbol", "eventType", "date"],
    indices = [
        Index(value = ["symbol"]),
        Index(value = ["date"]),
        Index(value = ["updatedAtEpochMs"])
    ]
)
data class FinancialEventEntity(
    val symbol: String,
    val eventType: EventType,
    val date: String, // ISO date string "YYYY-MM-DD"
    
    // Earnings fields
    val epsEstimated: Double? = null,
    val epsActual: Double? = null,
    val revenueEstimated: Double? = null,
    val revenueActual: Double? = null,
    val time: String? = null, // "bmo" or "amc"
    
    // Dividend fields
    val paymentDate: String? = null,
    val recordDate: String? = null,
    val declarationDate: String? = null,
    val dividendAmount: Double? = null,
    val dividendYield: Double? = null,
    
    // Stock split fields
    val splitNumerator: Double? = null,
    val splitDenominator: Double? = null,
    
    // Cache metadata
    val updatedAtEpochMs: Long
)

// -------- Mapping functions --------

fun FinancialEventEntity.toDomain(): FinancialEvent? {
    val localDate = try {
        LocalDate.parse(date)
    } catch (e: Exception) {
        return null
    }
    
    return when (eventType) {
        EventType.EARNINGS -> FinancialEvent.Earnings(
            symbol = symbol,
            date = localDate,
            epsEstimated = epsEstimated,
            epsActual = epsActual,
            revenueEstimated = revenueEstimated,
            revenueActual = revenueActual,
            time = time
        )
        EventType.DIVIDEND -> FinancialEvent.Dividend(
            symbol = symbol,
            date = localDate,
            paymentDate = paymentDate?.let { LocalDate.parse(it) },
            recordDate = recordDate?.let { LocalDate.parse(it) },
            declarationDate = declarationDate?.let { LocalDate.parse(it) },
            amount = dividendAmount ?: 0.0,
            yield = dividendYield
        )
        EventType.STOCK_SPLIT -> FinancialEvent.StockSplit(
            symbol = symbol,
            date = localDate,
            numerator = splitNumerator ?: 1.0,
            denominator = splitDenominator ?: 1.0
        )
    }
}

fun FinancialEvent.toEntity(updatedAt: Long): FinancialEventEntity {
    return when (this) {
        is FinancialEvent.Earnings -> FinancialEventEntity(
            symbol = symbol,
            eventType = eventType,
            date = date.toString(),
            epsEstimated = epsEstimated,
            epsActual = epsActual,
            revenueEstimated = revenueEstimated,
            revenueActual = revenueActual,
            time = time,
            updatedAtEpochMs = updatedAt
        )
        is FinancialEvent.Dividend -> FinancialEventEntity(
            symbol = symbol,
            eventType = eventType,
            date = date.toString(),
            paymentDate = paymentDate?.toString(),
            recordDate = recordDate?.toString(),
            declarationDate = declarationDate?.toString(),
            dividendAmount = amount,
            dividendYield = yield,
            updatedAtEpochMs = updatedAt
        )
        is FinancialEvent.StockSplit -> FinancialEventEntity(
            symbol = symbol,
            eventType = eventType,
            date = date.toString(),
            splitNumerator = numerator,
            splitDenominator = denominator,
            updatedAtEpochMs = updatedAt
        )
    }
}
