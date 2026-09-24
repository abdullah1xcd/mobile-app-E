package com.example.domain.model

import com.example.model.CartItem
import com.example.model.OrderStatus
import com.example.model.PaymentStatus

// Re-export common types from com.example.model for seamless architecture
typealias DomainProduct = com.example.model.Product
typealias DomainUser = com.example.model.User
typealias DomainOrder = com.example.model.Order
typealias DomainAddress = com.example.model.Address
typealias DomainCartItem = com.example.model.CartItem
typealias DomainCouponResult = com.example.model.CouponValidationResult

/**
 * Domain Payment models
 */
data class PaymentTransaction(
    val transactionId: String,
    val orderId: String,
    val amount: Double,
    val currency: String = "EGP",
    val paymentMethod: String,
    val status: PaymentStatus,
    val gatewayReference: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class PaymentResult(
    val isSuccess: Boolean,
    val transactionId: String?,
    val message: String,
    val paymentStatus: PaymentStatus
)

/**
 * Domain Shipping & Logistics models
 */
data class ShippingRate(
    val carrierId: String,
    val carrierName: String,
    val serviceLevel: String, // "Standard", "Express Same-Day"
    val rate: Double,
    val estimatedDeliveryDays: String
)

data class TrackingCheckpoint(
    val status: OrderStatus,
    val location: String,
    val timestamp: String,
    val description: String,
    val isCompleted: Boolean
)

data class ShipmentTracking(
    val trackingNumber: String,
    val orderId: String,
    val carrierName: String,
    val currentStatus: OrderStatus,
    val courierName: String,
    val courierPhone: String,
    val estimatedDeliveryTime: String,
    val checkpoints: List<TrackingCheckpoint>
)

/**
 * Domain Review models
 */
data class ProductReview(
    val id: String,
    val productId: String,
    val authorName: String,
    val rating: Int,
    val comment: String,
    val date: String,
    val isVerifiedPurchase: Boolean = true,
    val helpfulCount: Int = 0
)

/**
 * Domain Return & Refund models
 */
enum class ReturnReason(val label: String) {
    DEFECTIVE("Item is defective or broken"),
    WRONG_ITEM("Received incorrect item or size"),
    NOT_AS_DESCRIBED("Item does not match description"),
    CHANGED_MIND("No longer needed / Changed mind")
}

data class ReturnRequest(
    val returnId: String,
    val orderId: String,
    val reason: ReturnReason,
    val notes: String,
    val refundAmount: Double,
    val status: String = "PENDING_REVIEW", // PENDING_REVIEW, APPROVED, REJECTED, PICKUP_SCHEDULED, REFUNDED
    val createdAt: Long = System.currentTimeMillis()
)
