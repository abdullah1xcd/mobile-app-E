package com.example.domain.usecase

import com.example.domain.model.ReturnReason
import com.example.domain.model.ReturnRequest
import com.example.domain.repository.IOrderRepository
import com.example.domain.repository.IReturnRefundRepository
import com.example.model.Address
import com.example.model.CartItem
import com.example.model.Order
import com.example.model.OrderStatus
import kotlinx.coroutines.flow.Flow

class CreateOrderUseCase(private val orderRepository: IOrderRepository) {
    suspend operator fun invoke(
        items: List<CartItem>,
        address: Address,
        paymentMethod: String,
        couponDiscount: Double = 0.0
    ): Result<Order> {
        if (items.isEmpty()) {
            return Result.failure(IllegalStateException("Cart cannot be empty when creating an order"))
        }
        return orderRepository.createOrder(items, address, paymentMethod, couponDiscount)
    }
}

class GetOrdersUseCase(private val orderRepository: IOrderRepository) {
    operator fun invoke(): Flow<List<Order>> = orderRepository.ordersFlow
}

class GetOrderTrackingUseCase(private val orderRepository: IOrderRepository) {
    suspend operator fun invoke(orderId: String): Result<Order?> {
        return orderRepository.getOrderById(orderId)
    }
}

class UpdateOrderStatusUseCase(private val orderRepository: IOrderRepository) {
    suspend operator fun invoke(orderId: String, newStatus: OrderStatus): Result<Order> {
        return orderRepository.updateOrderStatus(orderId, newStatus)
    }
}

class RequestReturnUseCase(private val returnRepository: IReturnRefundRepository) {
    suspend operator fun invoke(
        orderId: String,
        reason: ReturnReason,
        notes: String,
        amount: Double
    ): Result<ReturnRequest> {
        return returnRepository.submitReturnRequest(orderId, reason, notes, amount)
    }
}
