package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.di.AppContainer

class ViewModelFactory(private val appContainer: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(
                    loginUseCase = appContainer.loginUseCase,
                    registerUseCase = appContainer.registerUseCase,
                    logoutUseCase = appContainer.logoutUseCase,
                    getCurrentUserUseCase = appContainer.getCurrentUserUseCase
                ) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    getProductsUseCase = appContainer.getProductsUseCase,
                    addToCartUseCase = appContainer.addToCartUseCase,
                    getCartUseCase = appContainer.getCartUseCase,
                    getCurrentUserUseCase = appContainer.getCurrentUserUseCase
                ) as T
            }
            modelClass.isAssignableFrom(ProductViewModel::class.java) -> {
                ProductViewModel(
                    getProductsUseCase = appContainer.getProductsUseCase,
                    getProductDetailUseCase = appContainer.getProductDetailUseCase,
                    searchProductsUseCase = appContainer.searchProductsUseCase,
                    addToCartUseCase = appContainer.addToCartUseCase,
                    getProductReviewsUseCase = appContainer.getProductReviewsUseCase,
                    submitReviewUseCase = appContainer.submitReviewUseCase
                ) as T
            }
            modelClass.isAssignableFrom(CartViewModel::class.java) -> {
                CartViewModel(
                    getCartUseCase = appContainer.getCartUseCase,
                    updateCartQuantityUseCase = appContainer.updateCartQuantityUseCase,
                    removeFromCartUseCase = appContainer.removeFromCartUseCase,
                    clearCartUseCase = appContainer.clearCartUseCase,
                    validateCouponUseCase = appContainer.validateCouponUseCase
                ) as T
            }
            modelClass.isAssignableFrom(CheckoutViewModel::class.java) -> {
                CheckoutViewModel(
                    getCartUseCase = appContainer.getCartUseCase,
                    clearCartUseCase = appContainer.clearCartUseCase,
                    createOrderUseCase = appContainer.createOrderUseCase,
                    processPaymentUseCase = appContainer.processPaymentUseCase,
                    getShippingRatesUseCase = appContainer.getShippingRatesUseCase
                ) as T
            }
            modelClass.isAssignableFrom(OrdersViewModel::class.java) -> {
                OrdersViewModel(
                    getOrdersUseCase = appContainer.getOrdersUseCase,
                    requestReturnUseCase = appContainer.requestReturnUseCase
                ) as T
            }
            modelClass.isAssignableFrom(TrackingViewModel::class.java) -> {
                TrackingViewModel(
                    getOrderTrackingUseCase = appContainer.getOrderTrackingUseCase,
                    getShipmentTrackingUseCase = appContainer.getShipmentTrackingUseCase
                ) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(
                    getCurrentUserUseCase = appContainer.getCurrentUserUseCase,
                    logoutUseCase = appContainer.logoutUseCase,
                    sessionPrefs = appContainer.sessionPrefs
                ) as T
            }
            modelClass.isAssignableFrom(NotificationsViewModel::class.java) -> {
                NotificationsViewModel() as T
            }
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> {
                AdminViewModel(
                    getOrdersUseCase = appContainer.getOrdersUseCase,
                    updateOrderStatusUseCase = appContainer.updateOrderStatusUseCase,
                    getCurrentUserUseCase = appContainer.getCurrentUserUseCase
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun getInstance(context: Context): ViewModelFactory {
            return ViewModelFactory(AppContainer.getInstance(context))
        }
    }
}
