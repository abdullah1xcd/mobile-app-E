package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.data.local.SessionPreferences
import com.example.domain.usecase.GetCurrentUserUseCase
import com.example.domain.usecase.LogoutUseCase
import com.example.model.Address
import com.example.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val currentUser: User? = null,
    val addresses: List<Address> = SampleData.defaultAddresses,
    val selectedAddressId: String = "addr-1",
    val pushNotificationsEnabled: Boolean = true,
    val fastCheckoutEnabled: Boolean = true
) {
    val isLoggedIn: Boolean
        get() = currentUser != null

    val isAdmin: Boolean
        get() = currentUser?.isAdmin == true
}

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sessionPrefs: SessionPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase.currentUserFlow.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
        viewModelScope.launch {
            sessionPrefs.pushNotificationsEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(pushNotificationsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            sessionPrefs.fastCheckoutEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(fastCheckoutEnabled = enabled) }
            }
        }
    }

    fun setPushNotificationsEnabled(enabled: Boolean) {
        sessionPrefs.setPushNotificationsEnabled(enabled)
    }

    fun setFastCheckoutEnabled(enabled: Boolean) {
        sessionPrefs.setFastCheckoutEnabled(enabled)
    }

    fun selectAddress(id: String) {
        _uiState.update { it.copy(selectedAddressId = id) }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            _events.emit("Logged out successfully")
            onComplete()
        }
    }
}
