package com.example.data.repository

import com.example.data.datasource.DataSourceConfig
import com.example.data.local.SessionPreferences
import com.example.data.mock.MockAuthDataSource
import com.example.data.remote.api.LuminaApiService
import com.example.data.remote.dto.LoginRequest
import com.example.data.remote.dto.RegisterRequest
import com.example.domain.repository.IAuthRepository
import com.example.model.User
import com.example.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepository(
    private val sessionPrefs: SessionPreferences,
    private val apiService: LuminaApiService
) : IAuthRepository {
    override val currentUserFlow: StateFlow<User?> = sessionPrefs.currentUserFlow

    override fun getCurrentUser(): User? = sessionPrefs.getCurrentUser()

    override fun isAuthenticated(): Boolean = sessionPrefs.getCurrentUser() != null

    override suspend fun login(email: String, pass: String): Result<User> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()

        // If not forced to MOCK, try real backend first
        if (!DataSourceConfig.isMockMode) {
            try {
                val response = apiService.login(LoginRequest(email = cleanEmail, password = pass))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val role = try { UserRole.valueOf(body.user.role) } catch (_: Exception) { UserRole.CUSTOMER }
                    val user = User(
                        id = body.user.id,
                        name = body.user.name,
                        email = body.user.email,
                        role = role,
                        phone = body.user.phone,
                        avatarUrl = body.user.avatarUrl
                    )
                    sessionPrefs.saveAuthSession(body.accessToken, body.refreshToken, user)
                    return@withContext Result.success(user)
                }
            } catch (e: Exception) {
                if (!DataSourceConfig.fallbackToMockOnError) {
                    return@withContext Result.failure(e)
                }
            }
        }

        // Mock / Offline authentication with Role-based demo accounts
        if (cleanEmail.contains("admin") || cleanEmail == "admin@lumina.com") {
            val admin = MockAuthDataSource.demoAdmin.copy(email = cleanEmail)
            sessionPrefs.saveAuthSession("enc-jwt-admin-token-xyz", "enc-refresh-admin-token", admin)
            Result.success(admin)
        } else {
            val customer = User(
                id = "usr-cust-${cleanEmail.hashCode()}",
                name = if (cleanEmail.contains("abdo") || cleanEmail.contains("haroon")) "Abdullah" else cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = cleanEmail,
                role = UserRole.CUSTOMER,
                phone = "+20 100 123 4567"
            )
            sessionPrefs.saveAuthSession("enc-jwt-customer-token-abc", "enc-refresh-customer-token", customer)
            Result.success(customer)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        pass: String,
        role: UserRole,
        phone: String?
    ): Result<User> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()

        if (!DataSourceConfig.isMockMode) {
            try {
                val response = apiService.register(
                    RegisterRequest(
                        name = name.trim(),
                        email = cleanEmail,
                        password = pass,
                        role = role.name,
                        phone = phone
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val userRole = try { UserRole.valueOf(body.user.role) } catch (_: Exception) { UserRole.CUSTOMER }
                    val user = User(
                        id = body.user.id,
                        name = body.user.name,
                        email = body.user.email,
                        role = userRole,
                        phone = body.user.phone,
                        avatarUrl = body.user.avatarUrl
                    )
                    sessionPrefs.saveAuthSession(body.accessToken, body.refreshToken, user)
                    return@withContext Result.success(user)
                }
            } catch (e: Exception) {
                if (!DataSourceConfig.fallbackToMockOnError) {
                    return@withContext Result.failure(e)
                }
            }
        }

        val newUser = User(
            id = "usr-" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            email = cleanEmail,
            role = role,
            phone = phone ?: "+20 100 123 4567"
        )
        sessionPrefs.saveAuthSession("enc-jwt-new-token", "enc-refresh-new-token", newUser)
        Result.success(newUser)
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!DataSourceConfig.isMockMode) {
                apiService.logout()
            }
        } catch (_: Exception) {
            // Safe to ignore network failure during local logout
        }
        sessionPrefs.clearSession()
        Result.success(Unit)
    }

    override suspend fun refreshToken(): Result<String> = withContext(Dispatchers.IO) {
        val currentRefresh = sessionPrefs.getRefreshToken()
        if (currentRefresh.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("No refresh token stored"))
        }
        // In real backend, call POST /api/v1/auth/refresh
        val newAccessToken = "enc-jwt-refreshed-" + System.currentTimeMillis()
        val user = sessionPrefs.getCurrentUser()
        if (user != null) {
            sessionPrefs.saveAuthSession(newAccessToken, currentRefresh, user)
        }
        Result.success(newAccessToken)
    }
}
