package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Order
import com.example.model.Product
import com.example.ui.components.BottomNavBar
import com.example.ui.components.HelpSupportDialog
import com.example.ui.components.LogoutConfirmationDialog
import com.example.ui.components.NavTab
import com.example.ui.components.PaymentMethodsDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OrderConfirmationScreen
import com.example.ui.screens.OrderTrackingScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.AppBackground
import com.example.viewmodel.ShopViewModel
import kotlinx.coroutines.flow.collectLatest

enum class SubScreen {
    NONE,
    AUTH,
    PRODUCT_DETAIL,
    CHECKOUT,
    ORDER_CONFIRMATION,
    ORDER_TRACKING,
    WISHLIST,
    NOTIFICATIONS,
    ORDERS,
    ADMIN_DASHBOARD
}

@Composable
fun AppNavigation(
    viewModel: ShopViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by remember { mutableStateOf(NavTab.HOME) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }
    var selectedOrderForTracking by remember { mutableStateOf<Order?>(null) }

    // Dialog state handlers
    var showPaymentMethodsDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Collect snackbar events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Intercept back presses when on a subscreen
    BackHandler(enabled = currentSubScreen != SubScreen.NONE) {
        when (currentSubScreen) {
            SubScreen.CHECKOUT -> currentSubScreen = SubScreen.NONE
            SubScreen.ORDER_CONFIRMATION -> currentSubScreen = SubScreen.NONE
            SubScreen.ORDER_TRACKING -> {
                currentSubScreen = if (uiState.lastPlacedOrder != null && selectedOrderForTracking?.orderId == uiState.lastPlacedOrder?.orderId) {
                    SubScreen.NONE
                } else {
                    SubScreen.ORDERS
                }
            }
            else -> currentSubScreen = SubScreen.NONE
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Show bottom nav only when in primary tab browsing mode
            if (currentSubScreen == SubScreen.NONE) {
                BottomNavBar(
                    selectedTab = currentTab,
                    cartItemCount = uiState.cartItemCount,
                    onTabSelected = { tab ->
                        currentTab = tab
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentSubScreen) {
                SubScreen.AUTH -> {
                    AuthScreen(
                        onLogin = { email, pass ->
                            viewModel.login(email, pass) { success ->
                                if (success) currentSubScreen = SubScreen.NONE
                            }
                        },
                        onRegister = { name, email, pass, role, phone ->
                            viewModel.register(name, email, pass, role, phone) { success ->
                                if (success) currentSubScreen = SubScreen.NONE
                            }
                        },
                        isLoading = uiState.authLoading,
                        errorMessage = uiState.authError,
                        onContinueAsGuest = { currentSubScreen = SubScreen.NONE }
                    )
                }

                SubScreen.PRODUCT_DETAIL -> {
                    uiState.selectedProduct?.let { product ->
                        ProductDetailScreen(
                            product = product,
                            isFavorite = uiState.wishlistIds.contains(product.id),
                            onBackClick = { currentSubScreen = SubScreen.NONE },
                            onFavoriteToggle = { viewModel.toggleWishlist(product.id) },
                            onAddToCart = { prod, size, color, qty ->
                                viewModel.addToCart(prod, size, color, qty)
                            }
                        )
                    }
                }

                SubScreen.CHECKOUT -> {
                    CheckoutScreen(
                        uiState = uiState,
                        onBackClick = { currentSubScreen = SubScreen.NONE },
                        onSelectAddress = { viewModel.selectAddress(it) },
                        onAddNewAddress = { title, street -> viewModel.addAddress(title, street) },
                        onSelectPaymentMethod = { viewModel.selectPaymentMethod(it) },
                        onPlaceOrder = {
                            val created = viewModel.placeOrder()
                            if (created != null) {
                                selectedOrderForTracking = created
                                currentSubScreen = SubScreen.ORDER_CONFIRMATION
                            }
                        }
                    )
                }

                SubScreen.ORDER_CONFIRMATION -> {
                    val orderToDisplay = uiState.lastPlacedOrder ?: uiState.orders.firstOrNull()
                    if (orderToDisplay != null) {
                        OrderConfirmationScreen(
                            order = orderToDisplay,
                            onTrackOrder = {
                                selectedOrderForTracking = orderToDisplay
                                currentSubScreen = SubScreen.ORDER_TRACKING
                            },
                            onContinueShopping = {
                                currentSubScreen = SubScreen.NONE
                                currentTab = NavTab.HOME
                            }
                        )
                    } else {
                        currentSubScreen = SubScreen.NONE
                    }
                }

                SubScreen.ORDER_TRACKING -> {
                    val trackingOrder = selectedOrderForTracking
                        ?: uiState.currentTrackingOrder
                        ?: uiState.orders.firstOrNull()

                    if (trackingOrder != null) {
                        OrderTrackingScreen(
                            order = trackingOrder,
                            onBackClick = {
                                currentSubScreen = if (uiState.lastPlacedOrder != null && selectedOrderForTracking?.orderId == uiState.lastPlacedOrder?.orderId) {
                                    SubScreen.NONE
                                } else {
                                    SubScreen.ORDERS
                                }
                            }
                        )
                    } else {
                        currentSubScreen = SubScreen.NONE
                    }
                }

                SubScreen.WISHLIST -> {
                    WishlistScreen(
                        uiState = uiState,
                        onBackClick = { currentSubScreen = SubScreen.NONE },
                        onProductClick = { product ->
                            viewModel.selectProduct(product)
                            currentSubScreen = SubScreen.PRODUCT_DETAIL
                        },
                        onFavoriteToggle = { productId ->
                            viewModel.toggleWishlist(productId)
                        },
                        onAddToCart = { product ->
                            viewModel.addToCart(product)
                        },
                        onExploreClick = {
                            currentSubScreen = SubScreen.NONE
                            currentTab = NavTab.EXPLORE
                        }
                    )
                }

                SubScreen.NOTIFICATIONS -> {
                    NotificationsScreen(
                        uiState = uiState,
                        onBackClick = { currentSubScreen = SubScreen.NONE },
                        onNotificationClick = { notif ->
                            if (notif.orderId != null) {
                                val order = uiState.orders.find { it.orderId == notif.orderId }
                                if (order != null) {
                                    selectedOrderForTracking = order
                                    currentSubScreen = SubScreen.ORDER_TRACKING
                                }
                            }
                        },
                        onMarkAllRead = { viewModel.markNotificationsAsRead() }
                    )
                }

                SubScreen.ORDERS -> {
                    OrdersScreen(
                        uiState = uiState,
                        onBackClick = { currentSubScreen = SubScreen.NONE },
                        onTrackOrder = { order ->
                            selectedOrderForTracking = order
                            currentSubScreen = SubScreen.ORDER_TRACKING
                        }
                    )
                }

                SubScreen.ADMIN_DASHBOARD -> {
                    // Role Protected: Only ADMIN can access
                    if (uiState.isAdmin) {
                        AdminDashboardScreen(
                            uiState = uiState,
                            onBackClick = { currentSubScreen = SubScreen.NONE },
                            onUpdateOrderStatus = { orderId, newStatus ->
                                viewModel.updateOrderStatus(orderId, newStatus)
                            },
                            onViewOrderCustomerView = { order ->
                                selectedOrderForTracking = order
                                currentSubScreen = SubScreen.ORDER_TRACKING
                            }
                        )
                    } else {
                        currentSubScreen = SubScreen.NONE
                    }
                }

                SubScreen.NONE -> {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "MainTabsAnimation"
                    ) { targetTab ->
                        when (targetTab) {
                            NavTab.HOME -> {
                                HomeScreen(
                                    uiState = uiState,
                                    onProductClick = { product ->
                                        viewModel.selectProduct(product)
                                        currentSubScreen = SubScreen.PRODUCT_DETAIL
                                    },
                                    onFavoriteToggle = { viewModel.toggleWishlist(it) },
                                    onAddToCart = { viewModel.addToCart(it) },
                                    onCategorySelect = { categoryId ->
                                        viewModel.selectCategory(categoryId)
                                        currentTab = NavTab.EXPLORE
                                    },
                                    onNavigateToSearch = { currentTab = NavTab.EXPLORE },
                                    onNavigateToNotifications = {
                                        currentSubScreen = SubScreen.NOTIFICATIONS
                                    },
                                    onNavigateToExplore = { currentTab = NavTab.EXPLORE }
                                )
                            }

                            NavTab.EXPLORE -> {
                                ExploreScreen(
                                    uiState = uiState,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onCategorySelect = { viewModel.selectCategory(it) },
                                    onSortChange = { viewModel.updateSortOption(it) },
                                    onRatingFilter = { viewModel.updateMinRating(it) },
                                    onToggleInStock = { viewModel.toggleInStockOnly(!uiState.filters.inStockOnly) },
                                    onResetFilters = { viewModel.resetFilters() },
                                    onProductClick = { product ->
                                        viewModel.selectProduct(product)
                                        currentSubScreen = SubScreen.PRODUCT_DETAIL
                                    },
                                    onFavoriteToggle = { viewModel.toggleWishlist(it) },
                                    onAddToCart = { viewModel.addToCart(it) }
                                )
                            }

                            NavTab.CART -> {
                                CartScreen(
                                    uiState = uiState,
                                    onQuantityChange = { item, delta ->
                                        viewModel.updateCartQuantity(item, delta)
                                    },
                                    onRemoveItem = { item ->
                                        viewModel.removeFromCart(item)
                                    },
                                    onApplyPromo = { code ->
                                        viewModel.applyPromoCode(code)
                                    },
                                    onCheckoutClick = {
                                        currentSubScreen = SubScreen.CHECKOUT
                                    },
                                    onStartShopping = {
                                        currentTab = NavTab.EXPLORE
                                    }
                                )
                            }

                            NavTab.PROFILE -> {
                                ProfileScreen(
                                    uiState = uiState,
                                    onNavigateToOrders = { currentSubScreen = SubScreen.ORDERS },
                                    onNavigateToWishlist = { currentSubScreen = SubScreen.WISHLIST },
                                    onNavigateToNotifications = { currentSubScreen = SubScreen.NOTIFICATIONS },
                                    onShowAddresses = { currentSubScreen = SubScreen.CHECKOUT },
                                    onShowPaymentMethods = { showPaymentMethodsDialog = true },
                                    onShowHelp = { showHelpDialog = true },
                                    onShowSettings = { showSettingsDialog = true },
                                    onNavigateToAdmin = { currentSubScreen = SubScreen.ADMIN_DASHBOARD },
                                    onLogout = { showLogoutDialog = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showPaymentMethodsDialog) {
        PaymentMethodsDialog(onDismiss = { showPaymentMethodsDialog = false })
    }

    if (showHelpDialog) {
        HelpSupportDialog(onDismiss = { showHelpDialog = false })
    }

    if (showSettingsDialog) {
        SettingsDialog(
            pushEnabled = uiState.pushNotificationsEnabled,
            fastCheckoutEnabled = uiState.fastCheckoutEnabled,
            onTogglePush = { viewModel.togglePushNotifications(it) },
            onToggleFastCheckout = { viewModel.toggleFastCheckout(it) },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            userName = uiState.userName,
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout {
                    currentSubScreen = SubScreen.AUTH
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}
