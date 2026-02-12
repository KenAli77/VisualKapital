package com.visualmoney.app.core

/**
 * A sealed class representing the result of an async operation.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    data class Loading<T>(val cachedData: T? = null) : Resource<T>()
}
