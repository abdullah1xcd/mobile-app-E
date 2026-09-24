package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.Product
import com.example.ui.components.CategoryChip
import com.example.ui.components.ProductCard
import com.example.ui.components.PromoBanner
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AppBackground
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardSurface
import com.example.ui.theme.Primary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ShopUiState
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    uiState: ShopUiState,
    onProductClick: (Product) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onCategorySelect: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Countdown timer for Flash Deals (simulated dynamic countdown)
    var countdownSeconds by remember { mutableStateOf(4 * 3600 + 38 * 60 + 22) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (countdownSeconds > 0) countdownSeconds--
        }
    }

    val hours = countdownSeconds / 3600
    val minutes = (countdownSeconds % 3600) / 60
    val seconds = countdownSeconds % 60
    val countdownText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Top Header: User Greeting + Notifications
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "👋 Hi, ${uiState.userName}",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Good morning • What are you looking for?",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = CardSurface,
                    shadowElevation = 1.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.size(46.dp)
                ) {
                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("notifications_bell_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (uiState.unreadNotificationCount > 0) {
                                    Badge(
                                        containerColor = AccentRose,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = "${uiState.unreadNotificationCount}",
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.unreadNotificationCount > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Search Bar Bar (clean, rapid touch target)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onNavigateToSearch)
                    .testTag("home_search_bar"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Search products, brands, sneakers...",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // 3. Categories Horizontal Carousel
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SectionHeader(
                title = "Categories",
                actionLabel = "See all",
                onActionClick = onNavigateToExplore,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(SampleData.categories) { cat ->
                    CategoryChip(
                        id = cat.id,
                        name = cat.name,
                        iconEmoji = cat.icon,
                        isSelected = uiState.filters.selectedCategory == cat.id,
                        onClick = {
                            onCategorySelect(cat.id)
                            if (cat.id != "all") {
                                onNavigateToExplore()
                            }
                        }
                    )
                }
            }
        }

        // 4. Big Promotion Banner
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                PromoBanner(
                    onShopNowClick = onNavigateToExplore
                )
            }
        }

        // 5. Popular Products (as specified in prompt: Nike 3,499, Watch 2,199)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "Popular Products",
                actionLabel = "See all",
                onActionClick = onNavigateToExplore,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            val popularList = uiState.products.filter { it.isPopular }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(popularList) { prod ->
                    ProductCard(
                        product = prod,
                        isFavorite = uiState.wishlistIds.contains(prod.id),
                        onProductClick = { onProductClick(prod) },
                        onFavoriteToggle = { onFavoriteToggle(prod.id) },
                        onAddToCart = { onAddToCart(prod) },
                        modifier = Modifier.width(185.dp)
                    )
                }
            }
        }

        // 6. Flash Deals with Live Countdown
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AccentAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Flash Deals",
                            tint = AccentAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Flash Deals",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Countdown badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentRose.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Ends in $countdownText",
                        color = AccentRose,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            val flashDeals = uiState.products.filter { it.isFlashDeal }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                items(flashDeals) { prod ->
                    ProductCard(
                        product = prod,
                        isFavorite = uiState.wishlistIds.contains(prod.id),
                        onProductClick = { onProductClick(prod) },
                        onFavoriteToggle = { onFavoriteToggle(prod.id) },
                        onAddToCart = { onAddToCart(prod) },
                        modifier = Modifier.width(185.dp)
                    )
                }
            }
        }

        // 7. Smart Personalized Recommendation Feature
        // "Because you viewed: [Product Name]"
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Because you viewed: ${uiState.recentlyViewed.name}",
                                color = Primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Curated picks based on your style",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            val similarList = uiState.products.filter {
                it.id != uiState.recentlyViewed.id &&
                (it.category == uiState.recentlyViewed.category || it.isRecommended)
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                items(similarList) { prod ->
                    ProductCard(
                        product = prod,
                        isFavorite = uiState.wishlistIds.contains(prod.id),
                        onProductClick = { onProductClick(prod) },
                        onFavoriteToggle = { onFavoriteToggle(prod.id) },
                        onAddToCart = { onAddToCart(prod) },
                        modifier = Modifier.width(185.dp)
                    )
                }
            }
        }

        // 8. Recommended for You (Grid preview)
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(
                title = "Recommended for You",
                actionLabel = "Explore",
                onActionClick = onNavigateToExplore,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        val allRemaining = uiState.products.chunked(2)
        items(allRemaining) { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                rowItems.forEach { prod ->
                    ProductCard(
                        product = prod,
                        isFavorite = uiState.wishlistIds.contains(prod.id),
                        onProductClick = { onProductClick(prod) },
                        onFavoriteToggle = { onFavoriteToggle(prod.id) },
                        onAddToCart = { onAddToCart(prod) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
