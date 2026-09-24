package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.SessionPreferences
import com.example.data.remote.ApiClient
import com.example.data.repository.AuthRepository
import com.example.data.repository.CartRepository
import com.example.data.repository.CouponRepository
import com.example.data.repository.OrderRepository
import com.example.data.repository.PaymentRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.ReturnRefundRepository
import com.example.data.repository.ReviewRepository
import com.example.data.repository.ShippingRepository
import com.example.domain.usecase.AddToCartUseCase
import com.example.domain.usecase.ClearCartUseCase
import com.example.domain.usecase.CreateOrderUseCase
import com.example.domain.usecase.GetCartUseCase
import com.example.domain.usecase.GetCurrentUserUseCase
import com.example.domain.usecase.GetOrderTrackingUseCase
import com.example.domain.usecase.GetOrdersUseCase
import com.example.domain.usecase.GetProductDetailUseCase
import com.example.domain.usecase.GetProductReviewsUseCase
import com.example.domain.usecase.GetProductsUseCase
import com.example.domain.usecase.GetShipmentTrackingUseCase
import com.example.domain.usecase.GetShippingRatesUseCase
import com.example.domain.usecase.LoginUseCase
import com.example.domain.usecase.LogoutUseCase
import com.example.domain.usecase.ProcessPaymentUseCase
import com.example.domain.usecase.RegisterUseCase
import com.example.domain.usecase.RemoveFromCartUseCase
import com.example.domain.usecase.RequestReturnUseCase
import com.example.domain.usecase.SearchProductsUseCase
import com.example.domain.usecase.SubmitReviewUseCase
import com.example.domain.usecase.UpdateCartQuantityUseCase
import com.example.domain.usecase.UpdateOrderStatusUseCase
import com.example.domain.usecase.ValidateCouponUseCase

/**
 * Dependency Injection Container providing single sources of truth,
 * repositories, and clean domain Use Cases across the application.
 */
class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext

    // Local & Remote Sources
    val database = AppDatabase.getDatabase(appContext)
    val sessionPrefs = SessionPreferences(appContext)
    val apiService = ApiClient.getApiService(appContext)
    val paymentApi = ApiClient.getPaymentApi(appContext)
    val shippingApi = ApiClient.getShippingApi(appContext)
    val reviewApi = ApiClient.getReviewApi(appContext)
    val returnRefundApi = ApiClient.getReturnRefundApi(appContext)

    // Repositories
    val authRepository = AuthRepository(sessionPrefs, apiService)
    val productRepository = ProductRepository(database.productDao(), apiService)
    val cartRepository = CartRepository(database.cartDao())
    val orderRepository = OrderRepository(database.orderDao(), apiService)
    val couponRepository = CouponRepository(apiService)
    val paymentRepository = PaymentRepository(paymentApi)
    val shippingRepository = ShippingRepository(shippingApi)
    val reviewRepository = ReviewRepository(reviewApi)
    val returnRefundRepository = ReturnRefundRepository(returnRefundApi)

    // Domain Use Cases: Auth
    val loginUseCase = LoginUseCase(authRepository)
    val registerUseCase = RegisterUseCase(authRepository)
    val logoutUseCase = LogoutUseCase(authRepository)
    val getCurrentUserUseCase = GetCurrentUserUseCase(authRepository)

    // Domain Use Cases: Product
    val getProductsUseCase = GetProductsUseCase(productRepository)
    val getProductDetailUseCase = GetProductDetailUseCase(productRepository)
    val searchProductsUseCase = SearchProductsUseCase(productRepository)

    // Domain Use Cases: Cart
    val getCartUseCase = GetCartUseCase(cartRepository)
    val addToCartUseCase = AddToCartUseCase(cartRepository)
    val updateCartQuantityUseCase = UpdateCartQuantityUseCase(cartRepository)
    val removeFromCartUseCase = RemoveFromCartUseCase(cartRepository)
    val clearCartUseCase = ClearCartUseCase(cartRepository)

    // Domain Use Cases: Orders & Checkout
    val validateCouponUseCase = ValidateCouponUseCase(couponRepository)
    val createOrderUseCase = CreateOrderUseCase(orderRepository)
    val getOrdersUseCase = GetOrdersUseCase(orderRepository)
    val getOrderTrackingUseCase = GetOrderTrackingUseCase(orderRepository)
    val updateOrderStatusUseCase = UpdateOrderStatusUseCase(orderRepository)
    val requestReturnUseCase = RequestReturnUseCase(returnRefundRepository)

    // Domain Use Cases: Payment, Shipping, Reviews
    val processPaymentUseCase = ProcessPaymentUseCase(paymentRepository)
    val getShippingRatesUseCase = GetShippingRatesUseCase(shippingRepository)
    val getShipmentTrackingUseCase = GetShipmentTrackingUseCase(shippingRepository)
    val getProductReviewsUseCase = GetProductReviewsUseCase(reviewRepository)
    val submitReviewUseCase = SubmitReviewUseCase(reviewRepository)

    companion object {
        @Volatile
        private var instance: AppContainer? = null

        fun getInstance(context: Context): AppContainer {
            return instance ?: synchronized(this) {
                val inst = AppContainer(context.applicationContext)
                instance = inst
                inst
            }
        }
    }
}
