package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.domain.model.PaymentResult
import com.example.domain.model.ShippingRate
import com.example.domain.usecase.ClearCartUseCase
import com.example.domain.usecase.CreateOrderUseCase
import com.example.domain.usecase.GetCartUseCase
import com.example.domain.usecase.GetShippingRatesUseCase
import com.example.domain.usecase.ProcessPaymentUseCase
import com.example.model.Address
import com.example.model.CartItem
import com.example.model.Order
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val cartItems: List<CartItem> = emptyList(),
    val addresses: List<Address> = SampleData.defaultAddresses,
    val selectedAddressId: String = "addr-1",
    val selectedPaymentMethod: String = "Cash on Delivery",
    val shippingRates: List<ShippingRate> = emptyList(),
    val selectedCarrierId: String? = null,
    val appliedCouponDiscount: Double = 0.0,
    val isProcessing: Boolean = false,
    val lastPlacedOrder: Order? = null,
    val errorMessage: String? = null
) {
    val selectedAddress: Address?
        get() = addresses.find { it.id == selectedAddressId } ?: addresses.firstOrNull()

    val subtotal: Double
        get() = cartItems.sumOf { it.totalItemPrice }

    val deliveryFee: Double
        get() = if (cartItems.isEmpty()) 0.0 else if (subtotal >= 4000.0) 0.0 else 100.0

    val total: Double
        get() = (subtotal + deliveryFee - appliedCouponDiscount).coerceAtLeast(0.0)
}

class CheckoutViewModel(
    private val getCartUseCase: GetCartUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val getShippingRatesUseCase: GetShippingRatesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            getCartUseCase().collect { items ->
                _uiState.update { it.copy(cartItems = items) }
                loadShippingRates()
            }
        }
    }

    fun selectAddress(addressId: String) {
        _uiState.update { it.copy(selectedAddressId = addressId) }
        loadShippingRates()
    }

    fun selectPaymentMethod(method: String) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun setCouponDiscount(discount: Double) {
        _uiState.update { it.copy(appliedCouponDiscount = discount) }
    }

    fun loadShippingRates() {
        val city = _uiState.value.selectedAddress?.city ?: "Cairo"
        val subtotal = _uiState.value.subtotal
        viewModelScope.launch {
            val rates = getShippingRatesUseCase(city, subtotal)
            _uiState.update {
                it.copy(
                    shippingRates = rates,
                    selectedCarrierId = rates.firstOrNull()?.carrierId
                )
            }
        }
    }

    fun placeOrder(cardLast4: String? = null, onSuccess: (Order) -> Unit) {
        val state = _uiState.value
        val address = state.selectedAddress ?: return
        val items = state.cartItems
        if (items.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            // 1. Create order
            val orderResult = createOrderUseCase(
                items = items,
                address = address,
                paymentMethod = state.selectedPaymentMethod,
                couponDiscount = state.appliedCouponDiscount
            )

            orderResult.fold(
                onSuccess = { order ->
                    // 2. Process payment transaction
                    processPaymentUseCase(
                        orderId = order.orderId,
                        amount = order.total,
                        paymentMethod = state.selectedPaymentMethod,
                        cardLast4 = cardLast4
                    )

                    // 3. Clear cart
                    clearCartUseCase()

                    _uiState.update { it.copy(isProcessing = false, lastPlacedOrder = order) }
                    _events.emit("Order #${order.orderId} placed successfully!")
                    onSuccess(order)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = error.message ?: "Failed to place order"
                        )
                    }
                }
            )
        }
    }
}
