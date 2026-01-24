package com.example.visualmoney

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform