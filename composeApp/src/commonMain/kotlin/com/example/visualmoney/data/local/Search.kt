package com.example.visualmoney.data.local

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val symbol: String= "",
    val name: String = "",
    val exchange: String = "",
    val currency: String= "",
    val country: String = "",
    val type: String = "",
    val region: String = "",
)