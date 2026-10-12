package com.visualmoney.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform