package com.example.domain.repository

import com.example.domain.model.PaymentResult
import com.example.domain.model.ProductReview
import com.example.domain.model.ReturnReason
import com.example.domain.model.ReturnRequest
import com.example.domain.model.ShipmentTracking
import com.example.domain.model.ShippingRate
import com.example.model.Address
import com.example.model.CartItem
import com.example.model.CouponValidationResult
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.User
import com.example.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface IAuthRepository {
    val currentUserFlow: StateFlow<User?>
    suspend fun login(email: String, pass: String): Result<User>
    suspend fun register(name: String, email: String, pass: String, role: UserRole, phone: String?): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(): Result<String>
    fun getCurrentUser(): User?
    fun isAuthenticated(): Boolean
}

interface IProductRepository {
    fun getProducts(): Flow<List<Product>>
    suspend fun getProductById(id: String): Result<Product?>
    suspend fun searchProducts(query: String, category: String?): List<Product>
    suspend fun refreshProducts(): Result<Unit>
}

interface ICartRepository {
    val cartItemsFlow: Flow<List<CartItem>>
    suspend fun addToCart(product: Product, size: Int?, color: String?, quantity: Int)
    suspend fun updateQuantity(item: CartItem, delta: Int)
    suspend fun removeFromCart(item: CartItem)
    suspend fun clearCart()
}

interface IOrderRepository {
    val ordersFlow: Flow<List<Order>>
    suspend fun createOrder(
        items: List<CartItem>,
        address: Address,
        paymentMethod: String,
        couponDiscount: Double
    ): Result<Order>
    suspend fun getOrderById(orderId: String): Result<Order?>
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Order>
}

interface ICouponRepository {
    suspend fun validateCoupon(code: String, cartSubtotal: Double): CouponValidationResult
}

interface IPaymentRepository {
    suspend fun processPayment(
        orderId: String,
        amount: Double,
        paymentMethod: String,
        cardLast4: String? = null
    ): Result<PaymentResult>
}

interface IShippingRepository {
    suspend fun getAvailableShippingRates(city: String, subtotal: Double): List<ShippingRate>
    suspend fun getShipmentTracking(orderId: String): Result<ShipmentTracking>
}

interface IReviewRepository {
    suspend fun getReviewsForProduct(productId: String): List<ProductReview>
    suspend fun submitReview(productId: String, rating: Int, comment: String): Result<ProductReview>
}

interface IReturnRefundRepository {
    suspend fun submitReturnRequest(orderId: String, reason: ReturnReason, notes: String, amount: Double): Result<ReturnRequest>
    suspend fun getReturnRequests(): List<ReturnRequest>
}
