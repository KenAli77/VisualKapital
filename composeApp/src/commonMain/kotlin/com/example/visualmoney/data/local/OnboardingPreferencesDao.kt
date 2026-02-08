package com.example.visualmoney.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OnboardingPreferencesDao {
    
    @Query("SELECT * FROM onboarding_preferences WHERE id = 1")
    fun getOnboardingPreferences(): Flow<OnboardingPreferencesEntity?>
    
    @Query("SELECT * FROM onboarding_preferences WHERE id = 1")
    suspend fun getOnboardingPreferencesOnce(): OnboardingPreferencesEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOnboardingPreferences(preferences: OnboardingPreferencesEntity)
    
    @Query("UPDATE onboarding_preferences SET isOnboardingCompleted = 1, userName = :userName WHERE id = 1")
    suspend fun completeOnboarding(userName: String)
}
