package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetCurrentUserUseCase
import com.example.domain.usecase.LoginUseCase
import com.example.domain.usecase.LogoutUseCase
import com.example.domain.usecase.RegisterUseCase
import com.example.model.User
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedRole: UserRole = UserRole.CUSTOMER
) {
    val isLoggedIn: Boolean
        get() = currentUser != null

    val isAdmin: Boolean
        get() = currentUser?.role == UserRole.ADMIN
}

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase.currentUserFlow.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    fun setRole(role: UserRole) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = loginUseCase(email, pass)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, currentUser = user, errorMessage = null) }
                    _events.emit("Welcome back, ${user.name}!")
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Authentication failed") }
                }
            )
        }
    }

    fun register(
        name: String,
        email: String,
        pass: String,
        role: UserRole = _uiState.value.selectedRole,
        phone: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = registerUseCase(name, email, pass, role, phone)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, currentUser = user, errorMessage = null) }
                    _events.emit("Account created successfully as ${user.role.name}!")
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Registration failed") }
                }
            )
        }
    }

    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.update { it.copy(currentUser = null) }
            _events.emit("Logged out successfully")
            onComplete()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
