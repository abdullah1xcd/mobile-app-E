package com.example.data.remote.dto

data class CreateOrderItemRequest(
    val productId: String,
    val quantity: Int,
    val selectedSize: Int? = null,
    val selectedColor: String? = null
)

data class CreateOrderRequest(
    val items: List<CreateOrderItemRequest>,
    val addressId: String,
    val paymentMethod: String,
    val couponCode: String? = null
)

data class OrderItemDto(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val selectedSize: Int? = null,
    val selectedColor: String? = null
)

data class OrderResponseDto(
    val orderId: String,
    val status: String,
    val paymentStatus: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val total: Double,
    val address: String,
    val paymentMethod: String,
    val estimatedDelivery: String,
    val courierName: String,
    val courierPhone: String,
    val etaMinutes: Int,
    val items: List<OrderItemDto>,
    val createdAt: String
)
