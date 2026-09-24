package com.example.domain.usecase

import com.example.domain.model.PaymentResult
import com.example.domain.model.ProductReview
import com.example.domain.model.ShipmentTracking
import com.example.domain.model.ShippingRate
import com.example.domain.repository.ICouponRepository
import com.example.domain.repository.IPaymentRepository
import com.example.domain.repository.IReviewRepository
import com.example.domain.repository.IShippingRepository
import com.example.model.CouponValidationResult

class ValidateCouponUseCase(private val couponRepository: ICouponRepository) {
    suspend operator fun invoke(code: String, subtotal: Double): CouponValidationResult {
        if (code.isBlank()) {
            return CouponValidationResult(
                isValid = false,
                code = "",
                discountAmount = 0.0,
                message = "Please enter a coupon code"
            )
        }
        return couponRepository.validateCoupon(code.trim().uppercase(), subtotal)
    }
}

class ProcessPaymentUseCase(private val paymentRepository: IPaymentRepository) {
    suspend operator fun invoke(
        orderId: String,
        amount: Double,
        paymentMethod: String,
        cardLast4: String? = null
    ): Result<PaymentResult> {
        return paymentRepository.processPayment(orderId, amount, paymentMethod, cardLast4)
    }
}

class GetShippingRatesUseCase(private val shippingRepository: IShippingRepository) {
    suspend operator fun invoke(city: String, subtotal: Double): List<ShippingRate> {
        return shippingRepository.getAvailableShippingRates(city, subtotal)
    }
}

class GetShipmentTrackingUseCase(private val shippingRepository: IShippingRepository) {
    suspend operator fun invoke(orderId: String): Result<ShipmentTracking> {
        return shippingRepository.getShipmentTracking(orderId)
    }
}

class GetProductReviewsUseCase(private val reviewRepository: IReviewRepository) {
    suspend operator fun invoke(productId: String): List<ProductReview> {
        return reviewRepository.getReviewsForProduct(productId)
    }
}

class SubmitReviewUseCase(private val reviewRepository: IReviewRepository) {
    suspend operator fun invoke(productId: String, rating: Int, comment: String): Result<ProductReview> {
        if (comment.isBlank()) {
            return Result.failure(IllegalArgumentException("Review comment cannot be empty"))
        }
        return reviewRepository.submitReview(productId, rating, comment.trim())
    }
}
