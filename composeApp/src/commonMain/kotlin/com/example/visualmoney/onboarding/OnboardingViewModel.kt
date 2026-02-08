package com.example.visualmoney.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoney.data.local.OnboardingPreferencesDao
import com.example.visualmoney.data.local.OnboardingPreferencesEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingState(
    val currentPage: Int = 0,
    val userName: String = "",
    val isLoading: Boolean = false
)

class OnboardingViewModel(
    private val onboardingPreferencesDao: OnboardingPreferencesDao
) : ViewModel() {
    
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()
    
    fun updateUserName(name: String) {
        _state.update { it.copy(userName = name) }
    }
    
    fun nextPage() {
        _state.update { it.copy(currentPage = it.currentPage + 1) }
    }
    
    fun previousPage() {
        if (_state.value.currentPage > 0) {
            _state.update { it.copy(currentPage = it.currentPage - 1) }
        }
    }
    
    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Save onboarding completion with user name
                onboardingPreferencesDao.saveOnboardingPreferences(
                    OnboardingPreferencesEntity(
                        id = 1,
                        isOnboardingCompleted = true,
                        userName = _state.value.userName.ifBlank { "User" }
                    )
                )
                onComplete()
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
