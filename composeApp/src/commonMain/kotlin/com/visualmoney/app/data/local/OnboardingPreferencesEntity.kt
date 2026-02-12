package com.visualmoney.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "onboarding_preferences")
data class OnboardingPreferencesEntity(
    @PrimaryKey
    val id: Int = 1, // Always 1, single row table
    val isOnboardingCompleted: Boolean = false,
    val userName: String = ""
)
