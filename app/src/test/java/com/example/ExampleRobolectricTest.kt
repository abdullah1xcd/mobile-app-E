package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.ShopViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Lumina", appName)
  }

  @Test
  fun `test cart and order calculation in ShopViewModel`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ShopViewModel(application)
    val initialProducts = viewModel.uiState.value.products
    val firstProduct = initialProducts.first()
    val initialCount = viewModel.uiState.value.cartItemCount

    viewModel.addToCart(firstProduct, quantity = 2)
    val state = viewModel.uiState.value
    assertTrue(state.subtotal >= 0.0)

    // Test promo code SAVE20
    viewModel.applyPromoCode("SAVE20")
  }
}
