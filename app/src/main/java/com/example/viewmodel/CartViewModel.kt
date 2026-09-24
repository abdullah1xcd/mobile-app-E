package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.ClearCartUseCase
import com.example.domain.usecase.GetCartUseCase
import com.example.domain.usecase.RemoveFromCartUseCase
import com.example.domain.usecase.UpdateCartQuantityUseCase
import com.example.domain.usecase.ValidateCouponUseCase
import com.example.model.CartItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val appliedPromoCode: String? = null,
    val promoDiscount: Double = 0.0,
    val promoError: String? = null,
    val promoSuccessMessage: String? = null
) {
    val subtotal: Double
        get() = cartItems.sumOf { it.totalItemPrice }

    val deliveryFee: Double
        get() = if (cartItems.isEmpty()) 0.0 else if (subtotal >= 4000.0) 0.0 else 100.0

    val total: Double
        get() = (subtotal + deliveryFee - promoDiscount).coerceAtLeast(0.0)

    val itemCount: Int
        get() = cartItems.sumOf { it.quantity }
}

class CartViewModel(
    private val getCartUseCase: GetCartUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val validateCouponUseCase: ValidateCouponUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            getCartUseCase().collect { items ->
                _uiState.update { it.copy(cartItems = items) }
            }
        }
    }

    fun updateQuantity(item: CartItem, delta: Int) {
        viewModelScope.launch {
            updateCartQuantityUseCase(item, delta)
        }
    }

    fun removeItem(item: CartItem) {
        viewModelScope.launch {
            removeFromCartUseCase(item)
            _events.emit("Removed ${item.product.name} from cart")
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            clearCartUseCase()
        }
    }

    fun applyCoupon(code: String) {
        viewModelScope.launch {
            val result = validateCouponUseCase(code, _uiState.value.subtotal)
            if (result.isValid) {
                _uiState.update {
                    it.copy(
                        appliedPromoCode = result.code,
                        promoDiscount = result.discountAmount,
                        promoError = null,
                        promoSuccessMessage = result.message
                    )
                }
                _events.emit(result.message)
            } else {
                _uiState.update {
                    it.copy(
                        promoError = result.message,
                        promoSuccessMessage = null
                    )
                }
                _events.emit(result.message)
            }
        }
    }

    fun removeCoupon() {
        _uiState.update {
            it.copy(
                appliedPromoCode = null,
                promoDiscount = 0.0,
                promoError = null,
                promoSuccessMessage = null
            )
        }
    }
}
