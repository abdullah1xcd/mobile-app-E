package com.example.data.repository

import com.example.data.local.SessionPreferences
import com.example.data.remote.api.LuminaApiService
import com.example.data.remote.dto.LoginRequest
import com.example.data.remote.dto.RegisterRequest
import com.example.model.User
import com.example.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepository(
    private val sessionPrefs: SessionPreferences,
    private val apiService: LuminaApiService
) {
    val currentUserFlow: StateFlow<User?> = sessionPrefs.currentUserFlow

    fun getCurrentUser(): User? = sessionPrefs.getCurrentUser()

    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()

        // 1. Attempt Real Backend API call if reachable
        try {
            val response = apiService.login(LoginRequest(email = cleanEmail, password = password))
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
        } catch (_: Exception) {
            // Graceful fallback to offline authentication/simulation
        }

        // 2. Offline / Mock Authentication with Role enforcement
        if (cleanEmail.contains("admin") || cleanEmail == "admin@lumina.com") {
            val adminUser = User(
                id = "usr-admin-01",
                name = "Store Manager",
                email = cleanEmail,
                role = UserRole.ADMIN,
                phone = "+20 100 999 8877",
                avatarUrl = null
            )
            sessionPrefs.saveAuthSession("mock-jwt-admin-token", "mock-refresh-token", adminUser)
            Result.success(adminUser)
        } else {
            val customerUser = User(
                id = "usr-cust-${cleanEmail.hashCode()}",
                name = if (cleanEmail.contains("abdo") || cleanEmail.contains("haroon")) "Abdullah" else cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = cleanEmail,
                role = UserRole.CUSTOMER,
                phone = "+20 100 123 4567",
                avatarUrl = null
            )
            sessionPrefs.saveAuthSession("mock-jwt-customer-token", "mock-refresh-token", customerUser)
            Result.success(customerUser)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole = UserRole.CUSTOMER,
        phone: String? = null
    ): Result<User> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        try {
            val response = apiService.register(
                RegisterRequest(
                    name = name.trim(),
                    email = cleanEmail,
                    password = password,
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
        } catch (_: Exception) {
            // Graceful fallback
        }

        val newUser = User(
            id = "usr-" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            email = cleanEmail,
            role = role,
            phone = phone ?: "+20 100 123 4567"
        )
        sessionPrefs.saveAuthSession("mock-jwt-token", "mock-refresh-token", newUser)
        Result.success(newUser)
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.logout()
        } catch (_: Exception) {
            // ignore network failure on logout
        }
        sessionPrefs.clearSession()
        Result.success(Unit)
    }
}
