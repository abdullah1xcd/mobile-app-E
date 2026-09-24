package com.example.data.remote.dto

data class CouponValidateRequest(
    val code: String,
    val cartSubtotal: Double
)

data class CouponValidateResponseDto(
    val isValid: Boolean,
    val code: String,
    val discountAmount: Double,
    val discountPercent: Int,
    val message: String
)
