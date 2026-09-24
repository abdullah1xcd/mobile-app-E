package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.data.local.AppDatabase
import com.example.data.local.SessionPreferences
import com.example.data.remote.ApiClient
import com.example.data.repository.AuthRepository
import com.example.data.repository.CartRepository
import com.example.data.repository.CouponRepository
import com.example.data.repository.OrderRepository
import com.example.data.repository.ProductRepository
import com.example.model.Address
import com.example.model.CartItem
import com.example.model.NotificationItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
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

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    // Clean Architecture Repositories & Data Sources
    private val sessionPrefs = SessionPreferences(application)
    private val database = AppDatabase.getDatabase(application)
    private val apiService = ApiClient.getApiService(application)

    private val authRepository = AuthRepository(sessionPrefs, apiService)
    private val productRepository = ProductRepository(database.productDao(), apiService)
    private val cartRepository = CartRepository(database.cartDao())
    private val orderRepository = OrderRepository(database.orderDao(), apiService)
    private val couponRepository = CouponRepository(apiService)

    private val _uiState = MutableStateFlow(
        ShopUiState(
            currentUser = sessionPrefs.getCurrentUser() ?: User(
                id = "usr-demo",
                name = "Abdullah",
                email = "abdo@email.com",
                role = UserRole.CUSTOMER
            ),
            pushNotificationsEnabled = sessionPrefs.pushNotificationsEnabledFlow.value,
            fastCheckoutEnabled = sessionPrefs.fastCheckoutEnabledFlow.value
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    init {
        // 1. Observe Authentication Session
        viewModelScope.launch {
            authRepository.currentUserFlow.collect { user ->
                _uiState.update { current ->
                    current.copy(
                        currentUser = user,
                        userName = user?.name ?: "Guest",
                        userEmail = user?.email ?: "guest@lumina.com"
                    )
                }
            }
        }

        // 2. Observe Products Repository
        viewModelScope.launch {
            productRepository.getProducts().collect { productList ->
                _uiState.update { it.copy(products = productList) }
            }
        }

        // 3. Observe Cart Repository (Room DB)
        viewModelScope.launch {
            cartRepository.cartItemsFlow.collect { items ->
                _uiState.update { it.copy(cartItems = items) }
                if (items.isEmpty() && _uiState.value.cartItems.isEmpty()) {
                    // Seed initial items into Room for seamless first run
                    val nike = SampleData.products.firstOrNull() ?: return@collect
                    val watch = SampleData.products.getOrNull(1) ?: return@collect
                    cartRepository.addToCart(nike, 42, "Navy / White", 1)
                    cartRepository.addToCart(watch, 44, "Midnight Black", 1)
                }
            }
        }

        // 4. Observe Orders Repository
        viewModelScope.launch {
            orderRepository.ordersFlow.collect { orderList ->
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
            sessionPrefs.pushNotificationsEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(pushNotificationsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            sessionPrefs.fastCheckoutEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(fastCheckoutEnabled = enabled) }
            }
        }
    }

    // --- Authentication Actions ---
    fun login(email: String, password: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authError = null) }
            val result = authRepository.login(email, password)
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
            val result = authRepository.register(name, email, password, role, phone)
            if (result.isSuccess) {
                _uiState.update { it.copy(authLoading = false, authError = null) }
                _snackbarEvent.emit("Account created! Welcome to Lumina.")
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

    fun logout(onLoggedOut: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.logout()
            _snackbarEvent.emit("Logged out of Lumina")
            onLoggedOut()
        }
    }

    // --- Product Selection & Search ---
    fun selectProduct(product: Product?) {
        _uiState.update { current ->
            current.copy(
                selectedProduct = product,
                recentlyViewed = product ?: current.recentlyViewed
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(categoryId: String) {
        _uiState.update {
            it.copy(filters = it.filters.copy(selectedCategory = categoryId))
        }
    }

    fun updateSortOption(sortOption: SortOption) {
        _uiState.update {
            it.copy(filters = it.filters.copy(sortOption = sortOption))
        }
    }

    fun updateMinRating(rating: Double) {
        _uiState.update {
            it.copy(filters = it.filters.copy(minRating = rating))
        }
    }

    fun toggleInStockOnly(inStock: Boolean) {
        _uiState.update {
            it.copy(filters = it.filters.copy(inStockOnly = inStock))
        }
    }

    fun updateMaxPrice(price: Double) {
        _uiState.update {
            it.copy(filters = it.filters.copy(maxPrice = price))
        }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(filters = FilterState(selectedCategory = it.filters.selectedCategory))
        }
    }

    // --- Cart Actions (Delegated to Room CartRepository) ---
    fun addToCart(product: Product, size: Int? = null, color: String? = null, quantity: Int = 1) {
        viewModelScope.launch {
            cartRepository.addToCart(product, size, color, quantity)
            _snackbarEvent.emit("Added ${product.name} to cart 🛍️")
        }
    }

    fun updateCartQuantity(item: CartItem, delta: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(item, delta)
        }
    }

    fun removeFromCart(item: CartItem) {
        viewModelScope.launch {
            cartRepository.removeFromCart(item)
            _snackbarEvent.emit("Removed ${item.product.name} from cart")
        }
    }

    fun toggleWishlist(productId: String) {
        _uiState.update { current ->
            val set = current.wishlistIds.toMutableSet()
            val added = if (set.contains(productId)) {
                set.remove(productId)
                false
            } else {
                set.add(productId)
                true
            }
            current.copy(wishlistIds = set).also {
                viewModelScope.launch {
                    val msg = if (added) "Saved to your Wishlist ❤️" else "Removed from Wishlist"
                    _snackbarEvent.emit(msg)
                }
            }
        }
    }

    // --- Coupon Validation (Delegated to CouponRepository) ---
    fun applyPromoCode(code: String) {
        viewModelScope.launch {
            val result = couponRepository.validateCoupon(code, _uiState.value.subtotal)
            if (result.isValid) {
                _uiState.update {
                    it.copy(
                        appliedPromoCode = result.code,
                        promoDiscount = result.discountAmount,
                        promoError = null,
                        promoSuccessMessage = result.message
                    )
                }
                _snackbarEvent.emit("${result.message} 🎉")
            } else {
                _uiState.update {
                    it.copy(
                        promoError = result.message,
                        promoSuccessMessage = null
                    )
                }
            }
        }
    }

    fun selectAddress(addressId: String) {
        _uiState.update { it.copy(selectedAddressId = addressId) }
    }

    fun addAddress(title: String, street: String) {
        val newAddr = Address(
            id = "addr-${System.currentTimeMillis()}",
            title = title,
            street = street,
            city = "Cairo, Egypt",
            isDefault = false
        )
        _uiState.update { current ->
            current.copy(
                addresses = current.addresses + newAddr,
                selectedAddressId = newAddr.id
            )
        }
        viewModelScope.launch { _snackbarEvent.emit("Address saved & selected") }
    }

    fun selectPaymentMethod(method: String) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    // --- Order Creation (Authoritative & Room-Persisted) ---
    fun placeOrder(): Order? {
        val current = _uiState.value
        if (current.cartItems.isEmpty()) return null

        val address = current.selectedAddress ?: Address(
            id = "addr-default",
            title = "Home",
            street = "Villa 221, South 90th St, New Cairo"
        )

        var createdOrder: Order? = null

        viewModelScope.launch {
            val result = orderRepository.createOrder(
                items = current.cartItems,
                address = address,
                paymentMethod = current.selectedPaymentMethod,
                couponDiscount = current.promoDiscount
            )

            if (result.isSuccess) {
                val newOrder = result.getOrNull()!!
                createdOrder = newOrder

                // Clear Cart via Room
                cartRepository.clearCart()

                val newNotif = NotificationItem(
                    id = "notif-${System.currentTimeMillis()}",
                    title = "Order #${newOrder.orderId} Confirmed!",
                    message = "We're packing your order. Estimated delivery: ${newOrder.estimatedDelivery}.",
                    timeAgo = "Just now",
                    group = "Today",
                    iconEmoji = "🎉",
                    isUnread = true,
                    orderId = newOrder.orderId
                )

                _uiState.update {
                    it.copy(
                        appliedPromoCode = null,
                        promoDiscount = 0.0,
                        lastPlacedOrder = newOrder,
                        currentTrackingOrder = newOrder,
                        notifications = listOf(newNotif) + it.notifications
                    )
                }
            }
        }

        return _uiState.value.lastPlacedOrder ?: _uiState.value.orders.firstOrNull()
    }

    // --- Merchant Admin Order Status Transition ---
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        // Enforce role check: only ADMIN or STAFF
        if (!_uiState.value.isAdmin) {
            viewModelScope.launch {
                _snackbarEvent.emit("Access Denied: Only Store Admins can update order status.")
            }
            return
        }

        viewModelScope.launch {
            val result = orderRepository.updateOrderStatus(orderId, newStatus)
            if (result.isSuccess) {
                val updated = result.getOrNull()!!

                var notifTitle = "Order #$orderId Update"
                var notifMsg = "Your order status changed to ${newStatus.label}"
                var icon = "📦"

                when (newStatus) {
                    OrderStatus.CONFIRMED -> {
                        notifTitle = "Order #$orderId Confirmed!"
                        notifMsg = "Merchant accepted your order and started preparation."
                        icon = "✅"
                    }
                    OrderStatus.PREPARING -> {
                        notifTitle = "Order #$orderId Packing"
                        notifMsg = "Your items are being packed in the warehouse."
                        icon = "📦"
                    }
                    OrderStatus.SHIPPED -> {
                        notifTitle = "Order #$orderId Shipped!"
                        notifMsg = "Handed to courier Ahmed (0100 892 3411)."
                        icon = "🚚"
                    }
                    OrderStatus.OUT_FOR_DELIVERY -> {
                        notifTitle = "🚚 Order #$orderId Out for Delivery!"
                        notifMsg = "Courier is approaching your doorstep in ~15 mins."
                        icon = "📍"
                    }
                    OrderStatus.DELIVERED -> {
                        notifTitle = "🎉 Order #$orderId Delivered!"
                        notifMsg = "Enjoy your items! Tap to rate your experience."
                        icon = "⭐"
                    }
                    else -> {}
                }

                val newNotif = NotificationItem(
                    id = "notif-${System.currentTimeMillis()}",
                    title = notifTitle,
                    message = notifMsg,
                    timeAgo = "Just now",
                    group = "Today",
                    iconEmoji = icon,
                    isUnread = true,
                    orderId = orderId
                )

                _uiState.update { current ->
                    current.copy(
                        currentTrackingOrder = if (current.currentTrackingOrder?.orderId == orderId) updated else current.currentTrackingOrder,
                        lastPlacedOrder = if (current.lastPlacedOrder?.orderId == orderId) updated else current.lastPlacedOrder,
                        notifications = listOf(newNotif) + current.notifications
                    )
                }

                _snackbarEvent.emit("Admin: Order #$orderId marked as ${newStatus.label}")
            }
        }
    }

    fun startTrackingOrder(order: Order) {
        _uiState.update { it.copy(currentTrackingOrder = order) }
    }

    fun markNotificationsAsRead() {
        _uiState.update { current ->
            current.copy(notifications = current.notifications.map { it.copy(isUnread = false) })
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        sessionPrefs.setPushNotificationsEnabled(enabled)
    }

    fun toggleFastCheckout(enabled: Boolean) {
        sessionPrefs.setFastCheckoutEnabled(enabled)
    }
}
