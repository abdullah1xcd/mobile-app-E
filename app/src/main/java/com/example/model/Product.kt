package com.example.model

data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val category: String,
    val price: Double,
    val originalPrice: Double? = null,
    val rating: Double,
    val reviewCount: Int,
    val description: String,
    val drawableRes: Int? = null,
    val emojiIcon: String = "📦",
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
    CONFIRMED("Order Confirmed", 0),
    PREPARING("Preparing", 1),
    SHIPPED("Shipped", 2),
    OUT_FOR_DELIVERY("Out for delivery", 3),
    DELIVERED("Delivered", 4)
}

data class Order(
    val orderId: String,
    val date: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val total: Double,
    val status: OrderStatus = OrderStatus.OUT_FOR_DELIVERY,
    val estimatedDelivery: String = "Tomorrow • 2:00–4:00 PM",
    val address: String = "Villa 221, South 90th St, New Cairo",
    val courierName: String = "Ahmed",
    val courierPhone: String = "+20 100 123 4567",
    val paymentMethod: String = "Credit Card (•••• 4821)",
    val etaMinutes: Int = 15
)

data class Address(
    val id: String,
    val title: String,
    val street: String,
    val city: String = "Cairo",
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
