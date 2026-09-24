package com.example.data.repository

import com.example.data.datasource.DataSourceConfig
import com.example.data.mock.MockCouponDataSource
import com.example.data.remote.api.LuminaApiService
import com.example.data.remote.dto.CouponValidateRequest
import com.example.domain.repository.ICouponRepository
import com.example.model.CouponValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CouponRepository(
    private val apiService: LuminaApiService
) : ICouponRepository {

    override suspend fun validateCoupon(code: String, cartSubtotal: Double): CouponValidationResult = withContext(Dispatchers.IO) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            return@withContext CouponValidationResult(
                isValid = false,
                code = "",
                discountAmount = 0.0,
                message = "Please enter a coupon code"
            )
        }

        // Real API validation if not in strict mock mode
        if (!DataSourceConfig.isMockMode) {
            try {
                val response = apiService.validateCoupon(
                    CouponValidateRequest(code = cleanCode, cartSubtotal = cartSubtotal)
                )
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
                // fallback to mock rules if configured
            }
        }

        // Authoritative Mock/Local rules
        MockCouponDataSource.validate(cleanCode, cartSubtotal)
    }
}
