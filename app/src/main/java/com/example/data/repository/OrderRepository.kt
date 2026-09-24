package com.example.data.repository

import com.example.data.local.OrderDao
import com.example.data.local.OrderEntity
import com.example.data.remote.api.LuminaApiService
import com.example.data.remote.dto.CreateOrderItemRequest
import com.example.data.remote.dto.CreateOrderRequest
import com.example.domain.repository.IOrderRepository
import com.example.model.Address
import com.example.model.CartItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.random.Random

class OrderRepository(
    private val orderDao: OrderDao,
    private val apiService: LuminaApiService
) : IOrderRepository {

    private val _ordersFlow = MutableStateFlow<List<Order>>(emptyList())
    override val ordersFlow: Flow<List<Order>> = _ordersFlow.asStateFlow()

    init {
        val initialOrder = Order(
            orderId = "10419",
            date = "Yesterday • 4:15 PM",
            items = emptyList(),
            subtotal = 3850.0,
            deliveryFee = 0.0,
            discount = 0.0,
            total = 3850.0,
            status = OrderStatus.OUT_FOR_DELIVERY,
            paymentStatus = PaymentStatus.PAID,
            estimatedDelivery = "Today • 3:30 PM",
            address = "Villa 221, South 90th St, New Cairo",
            courierName = "Ahmed",
            courierPhone = "+20 100 892 3411",
            paymentMethod = "Cash on Delivery",
            etaMinutes = 15
        )
        _ordersFlow.value = listOf(initialOrder)
    }

    override suspend fun createOrder(
        items: List<CartItem>,
        address: Address,
        paymentMethod: String,
        couponDiscount: Double
    ): Result<Order> = withContext(Dispatchers.IO) {
        if (items.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Cart is empty"))
        }

        val calculatedSubtotal = items.sumOf { it.totalItemPrice }
        val calculatedDeliveryFee = if (calculatedSubtotal >= 1000.0) 0.0 else 50.0
        val finalDiscount = couponDiscount.coerceAtMost(calculatedSubtotal)
        val calculatedTotal = (calculatedSubtotal + calculatedDeliveryFee - finalDiscount).coerceAtLeast(0.0)

        try {
            val req = CreateOrderRequest(
                items = items.map {
                    CreateOrderItemRequest(
                        productId = it.product.id,
                        quantity = it.quantity,
                        selectedSize = it.selectedSize,
                        selectedColor = it.selectedColor
                    )
                },
                addressId = address.id,
                paymentMethod = paymentMethod,
                couponCode = null
            )
            apiService.createOrder(req)
        } catch (_: Exception) {
            // Graceful fallback
        }

        val orderId = (10450 + Random.nextInt(10, 999)).toString()
        val isCard = paymentMethod.contains("Card", ignoreCase = true)
        val initialPaymentStatus = if (isCard) PaymentStatus.PAID else PaymentStatus.PENDING

        val newOrder = Order(
            orderId = orderId,
            date = "Today • Just now",
            items = items,
            subtotal = calculatedSubtotal,
            deliveryFee = calculatedDeliveryFee,
            discount = finalDiscount,
            total = calculatedTotal,
            status = OrderStatus.CONFIRMED,
            paymentStatus = initialPaymentStatus,
            estimatedDelivery = "Tomorrow • 2:00–4:00 PM",
            address = "${address.street}, ${address.area}, ${address.city}",
            courierName = "Ahmed",
            courierPhone = "+20 100 892 3411",
            paymentMethod = paymentMethod,
            etaMinutes = 20
        )

        try {
            orderDao.insertOrder(
                OrderEntity(
                    orderId = newOrder.orderId,
                    date = newOrder.date,
                    subtotal = newOrder.subtotal,
                    deliveryFee = newOrder.deliveryFee,
                    discount = newOrder.discount,
                    total = newOrder.total,
                    status = newOrder.status.name,
                    paymentStatus = newOrder.paymentStatus.name,
                    estimatedDelivery = newOrder.estimatedDelivery,
                    address = newOrder.address,
                    courierName = newOrder.courierName,
                    courierPhone = newOrder.courierPhone,
                    paymentMethod = newOrder.paymentMethod,
                    etaMinutes = newOrder.etaMinutes,
                    itemsSummary = "${items.size} item(s)"
                )
            )
        } catch (_: Exception) {}

        _ordersFlow.value = listOf(newOrder) + _ordersFlow.value
        Result.success(newOrder)
    }

    override suspend fun getOrderById(orderId: String): Result<Order?> = withContext(Dispatchers.IO) {
        val found = _ordersFlow.value.find { it.orderId == orderId }
        Result.success(found)
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Order> = withContext(Dispatchers.IO) {
        val currentOrders = _ordersFlow.value
        val target = currentOrders.find { it.orderId == orderId }
            ?: return@withContext Result.failure(IllegalArgumentException("Order not found"))

        val updated = target.copy(
            status = newStatus,
            etaMinutes = if (newStatus == OrderStatus.OUT_FOR_DELIVERY) 15 else if (newStatus == OrderStatus.DELIVERED) 0 else target.etaMinutes,
            paymentStatus = if (newStatus == OrderStatus.DELIVERED) PaymentStatus.PAID else target.paymentStatus
        )

        try {
            apiService.updateOrderStatus(orderId, mapOf("status" to newStatus.name))
        } catch (_: Exception) {}

        try {
            orderDao.updateOrderStatus(orderId, newStatus.name)
        } catch (_: Exception) {}

        _ordersFlow.value = currentOrders.map { if (it.orderId == orderId) updated else it }
        Result.success(updated)
    }
}
