package com.example.visualmoney.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.visualmoney.AssetCategory
import com.example.visualmoney.calendar.now
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "portfolio_assets",
)

data class PortfolioAsset(
    @PrimaryKey val symbol: String,
    val name: String,
    val purchasePrice: Double,
    val currentPrice: Double,
    val qty: Int = 0,
    val purchasedAt: LocalDate = LocalDate.now(),
    val type: AssetCategory,
    val note:String = "",
    val exchangeName: String,

    val notes: String? = null,
)
