package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.di.AppContainer
import com.example.model.Product
import com.example.model.UserRole
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.CartViewModel
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.ProductViewModel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FeatureViewModelsTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val container = AppContainer.getInstance(application)

    @Test
    fun `test AuthViewModel role selection`() = runTest {
        val authVm = AuthViewModel(
            loginUseCase = container.loginUseCase,
            registerUseCase = container.registerUseCase,
            logoutUseCase = container.logoutUseCase,
            getCurrentUserUseCase = container.getCurrentUserUseCase
        )

        authVm.setRole(UserRole.ADMIN)
        assertEquals(UserRole.ADMIN, authVm.uiState.value.selectedRole)

        authVm.setRole(UserRole.CUSTOMER)
        assertEquals(UserRole.CUSTOMER, authVm.uiState.value.selectedRole)
    }

    @Test
    fun `test HomeViewModel product loading`() = runTest {
        val homeVm = HomeViewModel(
            getProductsUseCase = container.getProductsUseCase,
            addToCartUseCase = container.addToCartUseCase,
            getCartUseCase = container.getCartUseCase,
            getCurrentUserUseCase = container.getCurrentUserUseCase
        )

        advanceUntilIdle()
        assertNotNull(homeVm.uiState.value.products)
        homeVm.selectCategory("Electronics")
        assertEquals("Electronics", homeVm.uiState.value.selectedCategory)
    }

    @Test
    fun `test CartViewModel coupon and calculations`() = runTest {
        val cartVm = CartViewModel(
            getCartUseCase = container.getCartUseCase,
            updateCartQuantityUseCase = container.updateCartQuantityUseCase,
            removeFromCartUseCase = container.removeFromCartUseCase,
            clearCartUseCase = container.clearCartUseCase,
            validateCouponUseCase = container.validateCouponUseCase
        )

        cartVm.applyCoupon("SAVE20")
        advanceUntilIdle()

        cartVm.removeCoupon()
        assertEquals(0.0, cartVm.uiState.value.promoDiscount, 0.001)
    }

    @Test
    fun `test ProductViewModel search and filter`() = runTest {
        val productVm = ProductViewModel(
            getProductsUseCase = container.getProductsUseCase,
            getProductDetailUseCase = container.getProductDetailUseCase,
            searchProductsUseCase = container.searchProductsUseCase,
            addToCartUseCase = container.addToCartUseCase,
            getProductReviewsUseCase = container.getProductReviewsUseCase,
            submitReviewUseCase = container.submitReviewUseCase
        )

        val sample = Product(
            id = "test-prod",
            name = "Test Noise Cancelling Headphones",
            brand = "Sony",
            category = "Electronics",
            price = 4500.0,
            rating = 4.9,
            reviewCount = 88,
            description = "High quality headphones",
            emojiIcon = "🎧"
        )
        productVm.selectProduct(sample)
        assertEquals("test-prod", productVm.uiState.value.selectedProduct?.id)
    }
}
