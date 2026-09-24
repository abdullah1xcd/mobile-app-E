package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Order
import com.example.model.Product
import com.example.ui.components.BottomNavBar
import com.example.ui.components.NavTab
import com.example.ui.screens.AdminDashboardScreen
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
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Primary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ShopViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

enum class SubScreen {
    NONE,
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
fun MainAppContainer(
    viewModel: ShopViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(NavTab.HOME) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }
    var selectedOrderForTracking by remember { mutableStateOf<Order?>(null) }

    // Dialog state handlers for Profile items
    var showPaymentMethodsDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var fastCheckoutEnabled by remember { mutableStateOf(true) }

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
            // Show bottom nav only when not in full-page deep flows
            if (currentSubScreen == SubScreen.NONE) {
                BottomNavBar(
                    selectedTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                        currentSubScreen = SubScreen.NONE
                    },
                    cartItemCount = uiState.cartItemCount
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentSubScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_navigation"
            ) { subScreen ->
                when (subScreen) {
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
                        } ?: run {
                            currentSubScreen = SubScreen.NONE
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
                                val newOrder = viewModel.placeOrder()
                                if (newOrder != null) {
                                    selectedOrderForTracking = newOrder
                                    currentSubScreen = SubScreen.ORDER_CONFIRMATION
                                }
                            }
                        )
                    }

                    SubScreen.ORDER_CONFIRMATION -> {
                        val activeOrder = uiState.lastPlacedOrder ?: uiState.orders.first()
                        OrderConfirmationScreen(
                            order = activeOrder,
                            onTrackOrder = {
                                selectedOrderForTracking = activeOrder
                                currentSubScreen = SubScreen.ORDER_TRACKING
                            },
                            onContinueShopping = {
                                currentSubScreen = SubScreen.NONE
                                currentTab = NavTab.HOME
                            }
                        )
                    }

                    SubScreen.ORDER_TRACKING -> {
                        val trackOrder = selectedOrderForTracking ?: uiState.orders.first()
                        OrderTrackingScreen(
                            order = trackOrder,
                            onBackClick = { currentSubScreen = SubScreen.NONE }
                        )
                    }

                    SubScreen.WISHLIST -> {
                        WishlistScreen(
                            uiState = uiState,
                            onBackClick = { currentSubScreen = SubScreen.NONE },
                            onProductClick = { prod ->
                                viewModel.selectProduct(prod)
                                currentSubScreen = SubScreen.PRODUCT_DETAIL
                            },
                            onFavoriteToggle = { viewModel.toggleWishlist(it) },
                            onAddToCart = { viewModel.addToCart(it) },
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
                                    val matched = uiState.orders.find { it.orderId == notif.orderId }
                                    selectedOrderForTracking = matched ?: uiState.orders.firstOrNull()
                                    currentSubScreen = SubScreen.ORDER_TRACKING
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
                    }

                    SubScreen.NONE -> {
                        when (currentTab) {
                            NavTab.HOME -> {
                                HomeScreen(
                                    uiState = uiState,
                                    onProductClick = { prod ->
                                        viewModel.selectProduct(prod)
                                        currentSubScreen = SubScreen.PRODUCT_DETAIL
                                    },
                                    onFavoriteToggle = { viewModel.toggleWishlist(it) },
                                    onAddToCart = { viewModel.addToCart(it) },
                                    onCategorySelect = { catId -> viewModel.selectCategory(catId) },
                                    onNavigateToSearch = {
                                        currentTab = NavTab.EXPLORE
                                    },
                                    onNavigateToNotifications = {
                                        currentSubScreen = SubScreen.NOTIFICATIONS
                                    },
                                    onNavigateToExplore = {
                                        currentTab = NavTab.EXPLORE
                                    }
                                )
                            }

                            NavTab.EXPLORE -> {
                                ExploreScreen(
                                    uiState = uiState,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onCategorySelect = { viewModel.selectCategory(it) },
                                    onSortChange = { viewModel.updateSortOption(it) },
                                    onRatingFilter = { viewModel.updateMinRating(it) },
                                    onToggleInStock = { viewModel.toggleInStockOnly() },
                                    onResetFilters = { viewModel.resetFilters() },
                                    onProductClick = { prod ->
                                        viewModel.selectProduct(prod)
                                        currentSubScreen = SubScreen.PRODUCT_DETAIL
                                    },
                                    onFavoriteToggle = { viewModel.toggleWishlist(it) },
                                    onAddToCart = { viewModel.addToCart(it) }
                                )
                            }

                            NavTab.CART -> {
                                CartScreen(
                                    uiState = uiState,
                                    onQuantityChange = { item, delta -> viewModel.updateCartQuantity(item, delta) },
                                    onRemoveItem = { item -> viewModel.removeFromCart(item) },
                                    onApplyPromo = { code -> viewModel.applyPromoCode(code) },
                                    onCheckoutClick = { currentSubScreen = SubScreen.CHECKOUT },
                                    onStartShopping = { currentTab = NavTab.EXPLORE }
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
                                    onLogout = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Logged in as ${uiState.userName} (${uiState.userEmail})")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Payment Methods Dialog
    if (showPaymentMethodsDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentMethodsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saved Payment Methods", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("• Visa ending in 4821 (Default)", fontSize = 13.sp, color = TextPrimary)
                    Text("• Mastercard ending in 9104", fontSize = 13.sp, color = TextPrimary)
                    Text("• Vodafone Cash (0100****456)", fontSize = 13.sp, color = TextPrimary)
                    Text("• Fawry Pay", fontSize = 13.sp, color = TextPrimary)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPaymentMethodsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Done")
                }
            }
        )
    }

    // Help & Support Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Help & Support", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Need help with an order or inquiry?", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("• Customer Care Hotline: 19042", fontSize = 13.sp, color = TextSecondary)
                    Text("• Email: support@lumina-commerce.com", fontSize = 13.sp, color = TextSecondary)
                    Text("• Average courier delivery: 25-40 minutes", fontSize = 13.sp, color = TextSecondary)
                    Text("• Free 14-day hassle-free doorstep returns", fontSize = 13.sp, color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Got it")
                }
            }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Preferences & Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Push Notifications", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Delivery updates and flash sales", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = pushNotificationsEnabled,
                            onCheckedChange = { pushNotificationsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Primary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("1-Click Instant Checkout", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Auto-select default card and address", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = fastCheckoutEnabled,
                            onCheckedChange = { fastCheckoutEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Primary)
                        )
                    }

                    Text("App Version 1.0.0 (Lumina Premium)", fontSize = 11.sp, color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Save")
                }
            }
        )
    }
}
