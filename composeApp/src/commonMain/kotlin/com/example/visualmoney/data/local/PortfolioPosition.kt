package com.example.visualmoney.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.visualmoney.calendar.now
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

enum class AssetType { SECURITY, CRYPTO, COMMODITY,OTHER }
enum class ManualInvestmentType { FIXED_INCOME, REAL_ESTATE, CASH_ACCOUNT, PHYSICAL_ASSET, OTHER }

@Entity(
    tableName = "tracked_assets",
    indices = [
        Index(value = ["symbol"], unique = true),
        Index(value = ["type"])
    ]
)
data class TrackedAssetEntity(
    @PrimaryKey val symbol: String,          // e.g. "AAPL", "BTCUSD", "XAUUSD"
    val type: AssetType,
    val name: String? = null,
    val exchange: String? = null,
    val currency: String? = null,
    // diversification metadata (optional, can be filled after profile fetch)
    val sector: String? = null,
    val country: String? = null,
    val updatedAtEpochMs: Long = Clock.System.now().toEpochMilliseconds()
)

@Entity(
    tableName = "portfolio_buys",
    indices = [
        Index(value = ["symbol"]),
        Index(value = ["executedAtEpochMs"])
    ]
)
data class PortfolioBuyEntity(
    @PrimaryKey val id: String,              // UUID string
    val symbol: String,                      // FK-ish to tracked_assets.symbol (not enforced; you may buy custom symbols)
    val type: AssetType,
    val quantity: Double,                    // e.g. 1.5 BTC, 10 shares
    val pricePerUnit: Double,                // entry price per unit
    val currency: String,                    // currency of the buy
    val fee: Double = 0.0,                   // total fee for this buy
    val executedAtEpochMs: Long              // when user bought
)

@Entity(
    tableName = "manual_investments",
    indices = [Index(value = ["type"])]
)
data class ManualInvestmentEntity(
    @PrimaryKey val id: String,              // UUID string
    val name: String,                        // "Rental apartment", "Bond 2028", "Savings account"
    val type: ManualInvestmentType,
    val currentValue: Double,                // user-updated valuation or principal
    val currency: String,
    val notes: String? = null,

    // optional tags for diversification
    val sector: String? = null,
    val country: String? = null,

    val createdAtEpochMs: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAtEpochMs: Long = Clock.System.now().toEpochMilliseconds()
)

@Entity(
    tableName = "investment_reminders",
)
data class InvestmentReminderEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val description: String,
    val note:String,
    val dueDate: LocalDate,
    val isDone: Boolean = false,
    val createdAtEpochMs: LocalDate = LocalDate.now()
)