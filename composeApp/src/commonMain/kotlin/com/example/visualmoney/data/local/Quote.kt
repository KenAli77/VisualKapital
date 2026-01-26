package com.example.visualmoney.data.local

import kotlinx.serialization.Serializable

@Serializable
data class CachedQuote(
    val symbol: String,
    val price: Double,
    val changePct: Double? = null,
    val volume: Long? = null,
    val currency: String? = null,
    val updatedAtEpochMs: Long
)