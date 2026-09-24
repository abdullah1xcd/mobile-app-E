package com.example.model

/**
 * User Roles for strict Role-Based Access Control (RBAC)
 */
enum class UserRole {
    CUSTOMER,
    ADMIN,
    STAFF
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.CUSTOMER,
    val phone: String? = null,
    val avatarUrl: String? = null
) {
    val isAdmin: Boolean
        get() = role == UserRole.ADMIN
}

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val category: String,
    val price: Double,
    val originalPrice: Double? = null,
    val compareAtPrice: Double? = originalPrice,
    val sku: String = "LUM-$id",
    val rating: Double,
    val reviewCount: Int,
    val description: String,
    val drawableRes: Int? = null,
    val emojiIcon: String = "📦",
    val imageUrl: String? = null,
    val images: List<String> = emptyList(),
    val seller: String = "Lumina Official Store",
    val stockQuantity: Int = 50,
    val sizes: List<Int> = emptyList(),
    val colors: List<String> = emptyList(),
    val inStock: Boolean = true,
    val isPopular: Boolean = false,
    val isFlashDeal: Boolean = false,
    val isRecommended: Boolean = false,
    val tags: List<String> = emptyList()
) {
    val discountPercent: Int?
        get() = if (originalPrice != null && originalPrice > price) {
            (((originalPrice - price) / originalPrice) * 100).toInt()
        } else null
}

data class CartItem(
    val product: Product,
    val selectedSize: Int? = null,
    val selectedColor: String? = null,
    val quantity: Int = 1
) {
    val totalItemPrice: Double
        get() = product.price * quantity
}

enum class OrderStatus(val label: String, val stepIndex: Int) {
    PENDING("Order Placed", 0),
    CONFIRMED("Order Confirmed", 1),
    PREPARING("Preparing", 2),
    PACKED("Packed", 3),
    SHIPPED("Shipped", 4),
    OUT_FOR_DELIVERY("Out for delivery", 5),
    DELIVERED("Delivered", 6),
    CANCELLED("Cancelled", -1),
    PAYMENT_FAILED("Payment Failed", -1),
    RETURN_REQUESTED("Return Requested", -1),
    RETURNED("Returned", -1),
    REFUNDED("Refunded", -1)
}

enum class PaymentStatus {
    PENDING,
    AUTHORIZED,
    PAID,
    FAILED,
    REFUNDED,
    PARTIALLY_REFUNDED
}

enum class PaymentMethodType(val label: String) {
    CASH_ON_DELIVERY("Cash on Delivery"),
    CREDIT_CARD("Credit / Debit Card"),
    WALLET("Vodafone Cash / Mobile Wallet"),
    FAWRY("Fawry Pay")
}

data class Order(
    val orderId: String,
    val date: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val total: Double,
    val status: OrderStatus = OrderStatus.CONFIRMED,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val estimatedDelivery: String = "Tomorrow • 2:00–4:00 PM",
    val address: String = "Villa 221, South 90th St, New Cairo",
    val courierName: String = "Ahmed",
    val courierPhone: String = "+20 100 123 4567",
    val paymentMethod: String = "Cash on Delivery",
    val etaMinutes: Int = 15
)

data class Address(
    val id: String,
    val title: String,
    val fullName: String = "Abdullah",
    val phone: String = "+20 100 123 4567",
    val governorate: String = "Cairo",
    val city: String = "Cairo",
    val area: String = "New Cairo",
    val street: String,
    val building: String = "221",
    val floor: String = "3",
    val apartment: String = "12",
    val landmark: String = "Near Dusit Thani",
    val deliveryInstructions: String = "Please ring doorbell",
    val isDefault: Boolean = false
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val group: String, // "Today", "Yesterday", "Earlier"
    val iconEmoji: String,
    val isUnread: Boolean = false,
    val orderId: String? = null
)

data class CouponValidationResult(
    val isValid: Boolean,
    val code: String,
    val discountAmount: Double,
    val discountPercent: Int = 0,
    val message: String
)
