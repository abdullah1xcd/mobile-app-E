package com.example.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "CUSTOMER",
    val phone: String? = null
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    val avatarUrl: String? = null
)

data class AuthResponseDto(
    val success: Boolean,
    val message: String? = null,
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)
