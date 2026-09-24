package com.example.data.repository

import com.example.data.remote.api.LuminaApiService
import com.example.data.remote.dto.CouponValidateRequest
import com.example.model.CouponValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CouponRepository(
    private val apiService: LuminaApiService
) {
    suspend fun validateCoupon(code: String, subtotal: Double): CouponValidationResult = withContext(Dispatchers.IO) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            return@withContext CouponValidationResult(
                isValid = false,
                code = "",
                discountAmount = 0.0,
                message = "Please enter a coupon code"
            )
        }

        // Try remote validation
        try {
            val response = apiService.validateCoupon(CouponValidateRequest(code = cleanCode, cartSubtotal = subtotal))
            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                return@withContext CouponValidationResult(
                    isValid = res.isValid,
                    code = res.code,
                    discountAmount = res.discountAmount,
                    discountPercent = res.discountPercent,
                    message = res.message
                )
            }
        } catch (_: Exception) {
            // fallback to local rules
        }

        // Business rules for Coupons
        when (cleanCode) {
            "SAVE20" -> {
                val discount = (subtotal * 0.20).coerceAtMost(200.0)
                CouponValidationResult(
                    isValid = true,
                    code = cleanCode,
                    discountAmount = discount,
                    discountPercent = 20,
                    message = "Saved 20% (up to 200 EGP)"
                )
            }
            "NOON40", "LUMINA40" -> {
                val discount = 400.0.coerceAtMost(subtotal)
                CouponValidationResult(
                    isValid = true,
                    code = cleanCode,
                    discountAmount = discount,
                    discountPercent = 40,
                    message = "VIP Voucher: 400 EGP off"
                )
            }
            "WELCOME" -> {
                val discount = 150.0.coerceAtMost(subtotal)
                CouponValidationResult(
                    isValid = true,
                    code = cleanCode,
                    discountAmount = discount,
                    message = "Welcome voucher: 150 EGP off"
                )
            }
            else -> {
                CouponValidationResult(
                    isValid = false,
                    code = cleanCode,
                    discountAmount = 0.0,
                    message = "Invalid or expired coupon code"
                )
            }
        }
    }
}
