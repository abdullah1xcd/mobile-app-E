package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.di.AppContainer
import com.example.model.Address
import com.example.model.CartItem
import com.example.model.NotificationItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.User
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortOption(val label: String) {
    POPULAR("Popular"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    RATING("Top Rated")
}

data class FilterState(
    val selectedCategory: String = "all",
    val minRating: Double = 0.0,
    val inStockOnly: Boolean = false,
    val sortOption: SortOption = SortOption.POPULAR,
    val maxPrice: Double = 60000.0
)

data class ShopUiState(
    val currentUser: User? = null,
    val userName: String = "Abdullah",
    val userEmail: String = "abdo@email.com",
    val authLoading: Boolean = false,
    val authError: String? = null,
    val pushNotificationsEnabled: Boolean = true,
    val fastCheckoutEnabled: Boolean = true,
    val products: List<Product> = SampleData.products,
    val cartItems: List<CartItem> = emptyList(),
    val appliedPromoCode: String? = null,
    val promoDiscount: Double = 0.0,
    val promoError: String? = null,
    val promoSuccessMessage: String? = null,
    val wishlistIds: Set<String> = setOf("prod-1", "prod-2"),
    val orders: List<Order> = emptyList(),
    val addresses: List<Address> = SampleData.defaultAddresses,
    val selectedAddressId: String = "addr-1",
    val notifications: List<NotificationItem> = SampleData.initialNotifications,
    val searchQuery: String = "",
    val filters: FilterState = FilterState(),
    val recentlyViewed: Product = SampleData.products[0],
    val selectedProduct: Product? = null,
    val currentTrackingOrder: Order? = null,
    val selectedPaymentMethod: String = "Cash on Delivery",
    val lastPlacedOrder: Order? = null
) {
    val isLoggedIn: Boolean
        get() = currentUser != null

    val isAdmin: Boolean
        get() = currentUser?.role == UserRole.ADMIN

    val subtotal: Double
        get() = cartItems.sumOf { it.totalItemPrice }

    val deliveryFee: Double
        get() = if (cartItems.isEmpty()) 0.0 else if (subtotal >= 4000.0) 0.0 else 100.0

    val total: Double
        get() = (subtotal + deliveryFee - promoDiscount).coerceAtLeast(0.0)

    val cartItemCount: Int
        get() = cartItems.sumOf { it.quantity }

    val unreadNotificationCount: Int
        get() = notifications.count { it.isUnread }

    val selectedAddress: Address?
        get() = addresses.find { it.id == selectedAddressId } ?: addresses.firstOrNull()

    val filteredProducts: List<Product>
        get() {
            var list = products

            if (filters.selectedCategory != "all") {
                list = list.filter { it.category.equals(filters.selectedCategory, ignoreCase = true) }
            }

            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                list = list.filter {
                    it.name.lowercase().contains(q) ||
                    it.brand.lowercase().contains(q) ||
                    it.category.lowercase().contains(q) ||
                    it.description.lowercase().contains(q)
                }
            }

            if (filters.minRating > 0.0) {
                list = list.filter { it.rating >= filters.minRating }
            }

            if (filters.inStockOnly) {
                list = list.filter { it.inStock }
            }

            list = list.filter { it.price <= filters.maxPrice }

            return when (filters.sortOption) {
                SortOption.POPULAR -> list.sortedByDescending { if (it.isPopular) 1 else 0 }
                SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.price }
                SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.price }
                SortOption.RATING -> list.sortedByDescending { it.rating }
            }
        }
}

/**
 * Domain-driven application coordinator backed by AppContainer and Domain Use Cases.
 */
class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val container = AppContainer.getInstance(application)

    private val _uiState = MutableStateFlow(
        ShopUiState(
            currentUser = container.getCurrentUserUseCase() ?: User(
                id = "usr-demo",
                name = "Abdullah",
                email = "abdo@email.com",
                role = UserRole.CUSTOMER
            ),
            pushNotificationsEnabled = container.sessionPrefs.pushNotificationsEnabledFlow.value,
            fastCheckoutEnabled = container.sessionPrefs.fastCheckoutEnabledFlow.value
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    init {
        // 1. Observe Authentication Session via Use Case
        viewModelScope.launch {
            container.getCurrentUserUseCase.currentUserFlow.collect { user ->
                _uiState.update { current ->
                    current.copy(
                        currentUser = user,
                        userName = user?.name ?: "Guest",
                        userEmail = user?.email ?: "guest@lumina.com"
                    )
                }
            }
        }

        // 2. Observe Products Repository via Use Case
        viewModelScope.launch {
            container.getProductsUseCase().collect { productList ->
                _uiState.update { it.copy(products = productList) }
            }
        }

        // 3. Observe Cart Repository via Use Case
        viewModelScope.launch {
            container.getCartUseCase().collect { items ->
                _uiState.update { it.copy(cartItems = items) }
                if (items.isEmpty() && _uiState.value.cartItems.isEmpty()) {
                    val nike = SampleData.products.firstOrNull() ?: return@collect
                    val watch = SampleData.products.getOrNull(1) ?: return@collect
                    container.addToCartUseCase(nike, 42, "Navy / White", 1)
                    container.addToCartUseCase(watch, 44, "Midnight Black", 1)
                }
            }
        }

        // 4. Observe Orders Repository via Use Case
        viewModelScope.launch {
            container.getOrdersUseCase().collect { orderList ->
                _uiState.update { current ->
                    current.copy(
                        orders = orderList,
                        currentTrackingOrder = current.currentTrackingOrder ?: orderList.firstOrNull()
                    )
                }
            }
        }

        // 5. Observe Preferences
        viewModelScope.launch {
            container.sessionPrefs.pushNotificationsEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(pushNotificationsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            container.sessionPrefs.fastCheckoutEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(fastCheckoutEnabled = enabled) }
            }
        }
    }

    // --- Authentication Actions (Delegating to Use Cases) ---
    fun login(email: String, password: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authError = null) }
            val result = container.loginUseCase(email, password)
            if (result.isSuccess) {
                val user = result.getOrNull()
                _uiState.update { it.copy(authLoading = false, authError = null) }
                _snackbarEvent.emit("Welcome back, ${user?.name}!")
                onComplete(true)
            } else {
                _uiState.update {
                    it.copy(
                        authLoading = false,
                        authError = result.exceptionOrNull()?.message ?: "Login failed. Check your credentials."
                    )
                }
                onComplete(false)
            }
        }
    }

    fun register(name: String, email: String, password: String, role: UserRole, phone: String?, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authError = null) }
            val result = container.registerUseCase(name, email, password, role, phone)
            if (result.isSuccess) {
                val user = result.getOrNull()
                _uiState.update { it.copy(authLoading = false, authError = null) }
                _snackbarEvent.emit("Account created for ${user?.name} (${user?.role})")
                onComplete(true)
            } else {
                _uiState.update {
                    it.copy(
                        authLoading = false,
                        authError = result.exceptionOrNull()?.message ?: "Registration failed."
                    )
                }
                onComplete(false)
            }
        }
    }

    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            container.logoutUseCase()
            _snackbarEvent.emit("Logged out successfully")
            onComplete()
        }
    }

    // --- Cart Actions (Delegating to Use Cases) ---
    fun addToCart(product: Product, size: Int? = null, color: String? = null, quantity: Int = 1) {
        viewModelScope.launch {
            container.addToCartUseCase(product, size, color, quantity)
            _snackbarEvent.emit("Added ${product.name} to cart")
        }
    }

    fun updateCartItemQuantity(item: CartItem, delta: Int) {
        viewModelScope.launch {
            container.updateCartQuantityUseCase(item, delta)
        }
    }

    fun removeCartItem(item: CartItem) {
        viewModelScope.launch {
            container.removeFromCartUseCase(item)
            _snackbarEvent.emit("Removed ${item.product.name} from cart")
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            container.clearCartUseCase()
        }
    }

    // --- Promo Code Validation (Delegating to Use Case) ---
    fun applyPromoCode(code: String) {
        viewModelScope.launch {
            val result = container.validateCouponUseCase(code, _uiState.value.subtotal)
            if (result.isValid) {
                _uiState.update {
                    it.copy(
                        appliedPromoCode = result.code,
                        promoDiscount = result.discountAmount,
                        promoError = null,
                        promoSuccessMessage = result.message
                    )
                }
                _snackbarEvent.emit(result.message)
            } else {
                _uiState.update {
                    it.copy(
                        promoError = result.message,
                        promoSuccessMessage = null
                    )
                }
                _snackbarEvent.emit(result.message)
            }
        }
    }

    fun removePromoCode() {
        _uiState.update {
            it.copy(
                appliedPromoCode = null,
                promoDiscount = 0.0,
                promoError = null,
                promoSuccessMessage = null
            )
        }
        viewModelScope.launch { _snackbarEvent.emit("Promo code removed") }
    }

    // --- Order Checkout Actions (Delegating to Use Cases) ---
    fun placeOrder(onSuccess: (Order) -> Unit) {
        val state = _uiState.value
        val address = state.selectedAddress ?: return
        val items = state.cartItems
        if (items.isEmpty()) return

        viewModelScope.launch {
            val orderResult = container.createOrderUseCase(
                items = items,
                address = address,
                paymentMethod = state.selectedPaymentMethod,
                couponDiscount = state.promoDiscount
            )

            orderResult.fold(
                onSuccess = { newOrder ->
                    container.processPaymentUseCase(
                        orderId = newOrder.orderId,
                        amount = newOrder.total,
                        paymentMethod = state.selectedPaymentMethod
                    )
                    container.clearCartUseCase()
                    _uiState.update {
                        it.copy(
                            appliedPromoCode = null,
                            promoDiscount = 0.0,
                            lastPlacedOrder = newOrder,
                            currentTrackingOrder = newOrder
                        )
                    }
                    _snackbarEvent.emit("Order #${newOrder.orderId} placed successfully!")
                    onSuccess(newOrder)
                },
                onFailure = { error ->
                    _snackbarEvent.emit(error.message ?: "Failed to place order")
                }
            )
        }
    }

    // --- Admin Operations (Delegating to Use Case with Role Checking) ---
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        if (!_uiState.value.isAdmin) {
            viewModelScope.launch {
                _snackbarEvent.emit("Unauthorized: Admin role required to transition order status")
            }
            return
        }

        viewModelScope.launch {
            val result = container.updateOrderStatusUseCase(orderId, newStatus)
            result.fold(
                onSuccess = { updated ->
                    _uiState.update { state ->
                        state.copy(
                            currentTrackingOrder = if (state.currentTrackingOrder?.orderId == orderId) updated else state.currentTrackingOrder
                        )
                    }
                    _snackbarEvent.emit("Order #${updated.orderId} moved to ${newStatus.label}")
                },
                onFailure = { error ->
                    _snackbarEvent.emit(error.message ?: "Failed to update order status")
                }
            )
        }
    }

    // --- UI State Modifiers ---
    fun toggleWishlist(productId: String) {
        _uiState.update { current ->
            val set = current.wishlistIds.toMutableSet()
            if (set.contains(productId)) set.remove(productId) else set.add(productId)
            current.copy(wishlistIds = set)
        }
    }

    fun selectProduct(product: Product) {
        _uiState.update { it.copy(selectedProduct = product, recentlyViewed = product) }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(filters = it.filters.copy(selectedCategory = category)) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSearchQuery() {
        _uiState.update { it.copy(searchQuery = "") }
    }

    fun setSortOption(option: SortOption) {
        _uiState.update { it.copy(filters = it.filters.copy(sortOption = option)) }
    }

    fun setMinRating(rating: Double) {
        _uiState.update { it.copy(filters = it.filters.copy(minRating = rating)) }
    }

    fun toggleInStockOnly() {
        _uiState.update { it.copy(filters = it.filters.copy(inStockOnly = !it.filters.inStockOnly)) }
    }

    fun setMaxPrice(price: Double) {
        _uiState.update { it.copy(filters = it.filters.copy(maxPrice = price)) }
    }

    fun resetFilters() {
        _uiState.update { it.copy(filters = FilterState()) }
    }

    fun selectAddress(addressId: String) {
        _uiState.update { it.copy(selectedAddressId = addressId) }
    }

    fun selectPaymentMethod(method: String) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun selectOrderForTracking(order: Order) {
        _uiState.update { it.copy(currentTrackingOrder = order) }
    }

    fun markNotificationRead(notificationId: String) {
        _uiState.update { current ->
            val updated = current.notifications.map {
                if (it.id == notificationId) it.copy(isUnread = false) else it
            }
            current.copy(notifications = updated)
        }
    }

    fun markAllNotificationsRead() {
        _uiState.update { current ->
            val updated = current.notifications.map { it.copy(isUnread = false) }
            current.copy(notifications = updated)
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        container.sessionPrefs.setPushNotificationsEnabled(enabled)
    }

    fun toggleFastCheckout(enabled: Boolean) {
        container.sessionPrefs.setFastCheckoutEnabled(enabled)
    }

    // --- Aliases and helpers for Navigation & Screens ---
    fun setSearchQuery(query: String) = onSearchQueryChange(query)
    fun updateSortOption(option: SortOption) = setSortOption(option)
    fun updateMinRating(rating: Double) = setMinRating(rating)
    fun toggleInStockOnly(enabled: Boolean = !uiState.value.filters.inStockOnly) {
        _uiState.update { it.copy(filters = it.filters.copy(inStockOnly = enabled)) }
    }
    fun updateCartQuantity(item: CartItem, delta: Int) = updateCartItemQuantity(item, delta)
    fun removeFromCart(item: CartItem) = removeCartItem(item)
    fun markNotificationsAsRead() = markAllNotificationsRead()

    fun addAddress(title: String, street: String) {
        val newAddr = Address(
            id = "addr-" + System.currentTimeMillis(),
            title = title,
            street = street,
            area = "Default Area",
            city = "Cairo"
        )
        _uiState.update { state ->
            state.copy(
                addresses = state.addresses + newAddr,
                selectedAddressId = newAddr.id
            )
        }
    }

    fun placeOrder(): Order? {
        val state = _uiState.value
        val address = state.selectedAddress ?: return null
        val items = state.cartItems
        if (items.isEmpty()) return null

        var createdOrder: Order? = null
        viewModelScope.launch {
            val orderResult = container.createOrderUseCase(
                items = items,
                address = address,
                paymentMethod = state.selectedPaymentMethod,
                couponDiscount = state.promoDiscount
            )
            orderResult.fold(
                onSuccess = { newOrder ->
                    createdOrder = newOrder
                    container.processPaymentUseCase(
                        orderId = newOrder.orderId,
                        amount = newOrder.total,
                        paymentMethod = state.selectedPaymentMethod
                    )
                    container.clearCartUseCase()
                    _uiState.update {
                        it.copy(
                            appliedPromoCode = null,
                            promoDiscount = 0.0,
                            lastPlacedOrder = newOrder,
                            currentTrackingOrder = newOrder
                        )
                    }
                    _snackbarEvent.emit("Order #${newOrder.orderId} placed successfully!")
                },
                onFailure = { error ->
                    _snackbarEvent.emit(error.message ?: "Failed to place order")
                }
            )
        }
        return state.lastPlacedOrder ?: Order(
            orderId = (10450 + kotlin.random.Random.nextInt(10, 999)).toString(),
            date = "Today • Just now",
            items = items,
            subtotal = state.subtotal,
            deliveryFee = state.deliveryFee,
            discount = state.promoDiscount,
            total = state.total,
            status = OrderStatus.CONFIRMED,
            paymentStatus = com.example.model.PaymentStatus.PENDING,
            estimatedDelivery = "Tomorrow • 2:00–4:00 PM",
            address = "${address.street}, ${address.area}, ${address.city}",
            courierName = "Ahmed",
            courierPhone = "+20 100 892 3411",
            paymentMethod = state.selectedPaymentMethod,
            etaMinutes = 20
        )
    }
}
