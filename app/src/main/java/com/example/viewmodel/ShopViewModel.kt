package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.model.Address
import com.example.model.CartItem
import com.example.model.NotificationItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.Product
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

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
    val userName: String = "Abdullah",
    val userEmail: String = "abdo@email.com",
    val products: List<Product> = SampleData.products,
    val cartItems: List<CartItem> = emptyList(),
    val appliedPromoCode: String? = null,
    val promoDiscount: Double = 0.0,
    val promoError: String? = null,
    val promoSuccessMessage: String? = null,
    val wishlistIds: Set<String> = setOf("prod-1", "prod-2"),
    val orders: List<Order> = listOf(SampleData.createInitialOrder()),
    val addresses: List<Address> = SampleData.defaultAddresses,
    val selectedAddressId: String = "addr-1",
    val notifications: List<NotificationItem> = SampleData.initialNotifications,
    val searchQuery: String = "",
    val filters: FilterState = FilterState(),
    val recentlyViewed: Product = SampleData.products[0],
    val selectedProduct: Product? = null,
    val currentTrackingOrder: Order? = null,
    val selectedPaymentMethod: String = "Credit / Debit Card",
    val lastPlacedOrder: Order? = null
) {
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

class ShopViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    init {
        // Initialize cart with sample items as shown in user prompt
        val nike = SampleData.products[0]
        val watch = SampleData.products[1]
        _uiState.update { state ->
            state.copy(
                cartItems = listOf(
                    CartItem(product = nike, selectedSize = 42, selectedColor = "Navy / White", quantity = 1),
                    CartItem(product = watch, selectedSize = 44, selectedColor = "Midnight Black", quantity = 1)
                ),
                currentTrackingOrder = state.orders.firstOrNull()
            )
        }
    }

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

    fun toggleInStockOnly() {
        _uiState.update {
            it.copy(filters = it.filters.copy(inStockOnly = !it.filters.inStockOnly))
        }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                filters = FilterState()
            )
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
            viewModelScope.launch {
                _snackbarEvent.emit(if (added) "Added to wishlist ❤️" else "Removed from wishlist")
            }
            current.copy(wishlistIds = set)
        }
    }

    fun addToCart(product: Product, size: Int? = null, color: String? = null, quantity: Int = 1) {
        _uiState.update { current ->
            val existingIndex = current.cartItems.indexOfFirst {
                it.product.id == product.id && it.selectedSize == size && it.selectedColor == color
            }

            val updatedList = current.cartItems.toMutableList()
            if (existingIndex >= 0) {
                val existing = updatedList[existingIndex]
                updatedList[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
            } else {
                val actualSize = size ?: product.sizes.firstOrNull()
                val actualColor = color ?: product.colors.firstOrNull()
                updatedList.add(CartItem(product = product, selectedSize = actualSize, selectedColor = actualColor, quantity = quantity))
            }

            viewModelScope.launch {
                _snackbarEvent.emit("Added ${product.name} to cart 🛒")
            }

            current.copy(cartItems = updatedList)
        }
    }

    fun updateCartQuantity(item: CartItem, delta: Int) {
        _uiState.update { current ->
            val updated = current.cartItems.mapNotNull {
                if (it.product.id == item.product.id && it.selectedSize == item.selectedSize && it.selectedColor == item.selectedColor) {
                    val newQty = it.quantity + delta
                    if (newQty <= 0) null else it.copy(quantity = newQty)
                } else it
            }
            current.copy(cartItems = updated)
        }
    }

    fun removeFromCart(item: CartItem) {
        _uiState.update { current ->
            val updated = current.cartItems.filterNot {
                it.product.id == item.product.id && it.selectedSize == item.selectedSize && it.selectedColor == item.selectedColor
            }
            viewModelScope.launch {
                _snackbarEvent.emit("Removed item from cart")
            }
            current.copy(cartItems = updated)
        }
    }

    fun applyPromoCode(code: String) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isEmpty()) {
            _uiState.update { it.copy(promoError = "Please enter a promo code", promoSuccessMessage = null) }
            return
        }

        when (cleanCode) {
            "SAVE20" -> {
                val discount = 200.0
                _uiState.update {
                    it.copy(
                        appliedPromoCode = cleanCode,
                        promoDiscount = discount,
                        promoError = null,
                        promoSuccessMessage = "Coupon SAVE20 applied! Saved 200 EGP"
                    )
                }
                viewModelScope.launch { _snackbarEvent.emit("Saved 200 EGP with SAVE20 🎉") }
            }
            "NOON40", "LUMINA40" -> {
                val discount = 400.0
                _uiState.update {
                    it.copy(
                        appliedPromoCode = cleanCode,
                        promoDiscount = discount,
                        promoError = null,
                        promoSuccessMessage = "Coupon applied! Saved 400 EGP"
                    )
                }
                viewModelScope.launch { _snackbarEvent.emit("Saved 400 EGP! 🎉") }
            }
            "WELCOME" -> {
                val discount = 150.0
                _uiState.update {
                    it.copy(
                        appliedPromoCode = cleanCode,
                        promoDiscount = discount,
                        promoError = null,
                        promoSuccessMessage = "Welcome coupon applied! Saved 150 EGP"
                    )
                }
                viewModelScope.launch { _snackbarEvent.emit("Welcome gift applied: 150 EGP off! ✨") }
            }
            else -> {
                _uiState.update {
                    it.copy(
                        promoError = "Invalid promo code. Try SAVE20 or WELCOME",
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

    fun placeOrder(): Order? {
        val current = _uiState.value
        if (current.cartItems.isEmpty()) return null

        val newOrderId = (10450 + Random.nextInt(10, 999)).toString()
        val addressStr = current.selectedAddress?.street ?: "Villa 221, South 90th St, New Cairo"

        val newOrder = Order(
            orderId = newOrderId,
            date = "Today • Just now",
            items = current.cartItems,
            subtotal = current.subtotal,
            deliveryFee = current.deliveryFee,
            discount = current.promoDiscount,
            total = current.total,
            status = OrderStatus.CONFIRMED,
            estimatedDelivery = "Tomorrow • 2:00–4:00 PM",
            address = addressStr,
            courierName = "Ahmed",
            courierPhone = "+20 100 892 3411",
            paymentMethod = current.selectedPaymentMethod,
            etaMinutes = 25
        )

        val newNotif = NotificationItem(
            id = "notif-${System.currentTimeMillis()}",
            title = "Order #$newOrderId Confirmed!",
            message = "We're packing your order. Estimated delivery: Tomorrow 2:00-4:00 PM.",
            timeAgo = "Just now",
            group = "Today",
            iconEmoji = "🎉",
            isUnread = true,
            orderId = newOrderId
        )

        _uiState.update {
            it.copy(
                orders = listOf(newOrder) + it.orders,
                cartItems = emptyList(),
                appliedPromoCode = null,
                promoDiscount = 0.0,
                lastPlacedOrder = newOrder,
                currentTrackingOrder = newOrder,
                notifications = listOf(newNotif) + it.notifications
            )
        }

        return newOrder
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        var notifTitle = ""
        var notifMsg = ""
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
            val updatedOrders = current.orders.map { order ->
                if (order.orderId == orderId) {
                    order.copy(
                        status = newStatus,
                        etaMinutes = if (newStatus == OrderStatus.OUT_FOR_DELIVERY) 15 else if (newStatus == OrderStatus.DELIVERED) 0 else order.etaMinutes
                    )
                } else order
            }

            val updatedTracking = if (current.currentTrackingOrder?.orderId == orderId) {
                current.currentTrackingOrder.copy(
                    status = newStatus,
                    etaMinutes = if (newStatus == OrderStatus.OUT_FOR_DELIVERY) 15 else if (newStatus == OrderStatus.DELIVERED) 0 else current.currentTrackingOrder.etaMinutes
                )
            } else current.currentTrackingOrder

            val updatedLastPlaced = if (current.lastPlacedOrder?.orderId == orderId) {
                current.lastPlacedOrder.copy(
                    status = newStatus,
                    etaMinutes = if (newStatus == OrderStatus.OUT_FOR_DELIVERY) 15 else if (newStatus == OrderStatus.DELIVERED) 0 else current.lastPlacedOrder.etaMinutes
                )
            } else current.lastPlacedOrder

            current.copy(
                orders = updatedOrders,
                currentTrackingOrder = updatedTracking,
                lastPlacedOrder = updatedLastPlaced,
                notifications = listOf(newNotif) + current.notifications
            )
        }

        viewModelScope.launch {
            _snackbarEvent.emit("Admin: Order #$orderId marked as ${newStatus.label}")
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
}
