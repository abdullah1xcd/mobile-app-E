package com.example.data.mock

import com.example.data.SampleData
import com.example.domain.model.PaymentResult
import com.example.domain.model.ProductReview
import com.example.domain.model.ReturnReason
import com.example.domain.model.ReturnRequest
import com.example.domain.model.ShipmentTracking
import com.example.domain.model.ShippingRate
import com.example.domain.model.TrackingCheckpoint
import com.example.model.CouponValidationResult
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.model.Product
import com.example.model.User
import com.example.model.UserRole

object MockAuthDataSource {
    val demoCustomer = User(
        id = "usr-demo",
        name = "Abdullah",
        email = "abdo@email.com",
        role = UserRole.CUSTOMER,
        phone = "+20 100 123 4567"
    )

    val demoAdmin = User(
        id = "usr-admin",
        name = "Abdullah Admin",
        email = "admin@lumina.com",
        role = UserRole.ADMIN,
        phone = "+20 100 892 3411"
    )
}

object MockPaymentDataSource {
    fun processPayment(orderId: String, amount: Double, method: String, cardLast4: String?): PaymentResult {
        return PaymentResult(
            isSuccess = true,
            transactionId = "TXN-${System.currentTimeMillis() % 1000000}",
            message = "Payment of EGP ${String.format("%.2f", amount)} approved via $method",
            paymentStatus = if (method.contains("Cash", ignoreCase = true)) PaymentStatus.PENDING else PaymentStatus.PAID
        )
    }
}

object MockShippingDataSource {
    val shippingRates = listOf(
        ShippingRate(
            carrierId = "bosta-std",
            carrierName = "Bosta Express",
            serviceLevel = "Standard Home Delivery",
            rate = 100.0,
            estimatedDeliveryDays = "Tomorrow • 2:00–5:00 PM"
        ),
        ShippingRate(
            carrierId = "aramex-exp",
            carrierName = "Aramex Priority",
            serviceLevel = "Same-Day Rush",
            rate = 180.0,
            estimatedDeliveryDays = "Today • Within 3 hours"
        )
    )

    fun getTracking(orderId: String): ShipmentTracking {
        return ShipmentTracking(
            trackingNumber = "LUM-EG-${orderId.takeLast(6).uppercase()}",
            orderId = orderId,
            carrierName = "Bosta Express",
            currentStatus = OrderStatus.SHIPPED,
            courierName = "Captain Ahmed",
            courierPhone = "+20 100 892 3411",
            estimatedDeliveryTime = "Today • 2:30 PM",
            checkpoints = listOf(
                TrackingCheckpoint(
                    status = OrderStatus.CONFIRMED,
                    location = "Cairo Fulfillment Hub",
                    timestamp = "10:15 AM",
                    description = "Order verified and sent to warehouse picker",
                    isCompleted = true
                ),
                TrackingCheckpoint(
                    status = OrderStatus.PREPARING,
                    location = "Warehouse 4, 10th of Ramadan",
                    timestamp = "11:30 AM",
                    description = "Quality inspect passed & packed into tamper-proof bag",
                    isCompleted = true
                ),
                TrackingCheckpoint(
                    status = OrderStatus.SHIPPED,
                    location = "New Cairo Delivery Station",
                    timestamp = "01:00 PM",
                    description = "Handed to courier Captain Ahmed",
                    isCompleted = true
                ),
                TrackingCheckpoint(
                    status = OrderStatus.OUT_FOR_DELIVERY,
                    location = "South 90th St, New Cairo",
                    timestamp = "02:00 PM",
                    description = "Courier on delivery route (~15 mins away)",
                    isCompleted = false
                ),
                TrackingCheckpoint(
                    status = OrderStatus.DELIVERED,
                    location = "Doorstep",
                    timestamp = "Pending",
                    description = "Recipient confirmation",
                    isCompleted = false
                )
            )
        )
    }
}

object MockReviewDataSource {
    private val reviewsMap = mutableMapOf<String, MutableList<ProductReview>>()

    init {
        SampleData.products.forEach { prod ->
            reviewsMap[prod.id] = mutableListOf(
                ProductReview(
                    id = "rev-${prod.id}-1",
                    productId = prod.id,
                    authorName = "Mohamed S.",
                    rating = 5,
                    comment = "Incredible build quality and fast next-day delivery in Cairo! Exactly as described.",
                    date = "2 days ago",
                    isVerifiedPurchase = true,
                    helpfulCount = 14
                ),
                ProductReview(
                    id = "rev-${prod.id}-2",
                    productId = prod.id,
                    authorName = "Nour E.",
                    rating = 5,
                    comment = "Super comfortable, authentic product. The packaging was pristine. Lumina is my new go-to app!",
                    date = "Last week",
                    isVerifiedPurchase = true,
                    helpfulCount = 8
                )
            )
        }
    }

    fun getReviews(productId: String): List<ProductReview> {
        return reviewsMap[productId] ?: emptyList()
    }

    fun addReview(productId: String, rating: Int, comment: String): ProductReview {
        val newReview = ProductReview(
            id = "rev-${System.currentTimeMillis()}",
            productId = productId,
            authorName = "Abdullah",
            rating = rating,
            comment = comment,
            date = "Just now",
            isVerifiedPurchase = true,
            helpfulCount = 0
        )
        val list = reviewsMap.getOrPut(productId) { mutableListOf() }
        list.add(0, newReview)
        return newReview
    }
}

object MockCouponDataSource {
    fun validate(code: String, subtotal: Double): CouponValidationResult {
        return when (code.trim().uppercase()) {
            "SAVE20" -> {
                val discount = (subtotal * 0.20).coerceAtMost(500.0)
                CouponValidationResult(
                    isValid = true,
                    code = "SAVE20",
                    discountAmount = discount,
                    discountPercent = 20,
                    message = "20% Discount applied! Saved EGP ${discount.toInt()}"
                )
            }
            "FIRST100" -> {
                val discount = 100.0.coerceAtMost(subtotal)
                CouponValidationResult(
                    isValid = true,
                    code = "FIRST100",
                    discountAmount = discount,
                    discountPercent = 0,
                    message = "EGP 100 Welcome voucher applied!"
                )
            }
            "FREESHIP" -> {
                CouponValidationResult(
                    isValid = true,
                    code = "FREESHIP",
                    discountAmount = 100.0,
                    discountPercent = 0,
                    message = "Free Shipping voucher applied!"
                )
            }
            else -> CouponValidationResult(
                isValid = false,
                code = code,
                discountAmount = 0.0,
                discountPercent = 0,
                message = "Invalid promo code. Try SAVE20 or FIRST100"
            )
        }
    }
}
