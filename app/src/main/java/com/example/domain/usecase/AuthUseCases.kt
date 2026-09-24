package com.example.domain.usecase

import com.example.domain.repository.IAuthRepository
import com.example.model.User
import com.example.model.UserRole
import kotlinx.coroutines.flow.StateFlow

class LoginUseCase(private val authRepository: IAuthRepository) {
    suspend operator fun invoke(email: String, pass: String): Result<User> {
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
        if (pass.length < 4) {
            return Result.failure(IllegalArgumentException("Password must be at least 4 characters"))
        }
        return authRepository.login(email.trim(), pass)
    }
}

class RegisterUseCase(private val authRepository: IAuthRepository) {
    suspend operator fun invoke(
        name: String,
        email: String,
        pass: String,
        role: UserRole = UserRole.CUSTOMER,
        phone: String? = null
    ): Result<User> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be empty"))
        }
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
        if (pass.length < 4) {
            return Result.failure(IllegalArgumentException("Password must be at least 4 characters"))
        }
        return authRepository.register(name.trim(), email.trim(), pass, role, phone?.trim())
    }
}

class LogoutUseCase(private val authRepository: IAuthRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}

class GetCurrentUserUseCase(private val authRepository: IAuthRepository) {
    val currentUserFlow: StateFlow<User?> = authRepository.currentUserFlow
    operator fun invoke(): User? = authRepository.getCurrentUser()
    fun isAuthenticated(): Boolean = authRepository.isAuthenticated()
}
