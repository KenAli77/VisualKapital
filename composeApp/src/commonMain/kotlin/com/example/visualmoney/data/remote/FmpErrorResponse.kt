package com.example.visualmoney.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Error response from Financial Modeling Prep API
 */
@Serializable
data class FmpErrorResponse(
    @SerialName("Error Message")
    val errorMessage: String
)
