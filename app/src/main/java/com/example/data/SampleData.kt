package com.example.data

import com.example.R
import com.example.model.Address
import com.example.model.CartItem
import com.example.model.NotificationItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.Product

object SampleData {

    val categories = listOf(
        CategoryItem("all", "All", "✨"),
        CategoryItem("footwear", "Footwear", "👟"),
        CategoryItem("watches", "Watches", "⌚"),
        CategoryItem("audio", "Audio", "🎧"),
        CategoryItem("fashion", "Fashion", "👕"),
        CategoryItem("electronics", "Tech", "💻"),
        CategoryItem("home", "Home", "🏠")
    )

    data class CategoryItem(
        val id: String,
        val name: String,
        val icon: String
    )

    val products = listOf(
        Product(
            id = "prod-1",
            name = "Nike Air Max 270",
            brand = "Nike",
            category = "footwear",
            price = 3499.0,
            originalPrice = 4200.0,
            rating = 4.8,
            reviewCount = 243,
            description = "The Nike Air Max 270 delivers unrivaled, all-day comfort with the biggest heel Air unit yet. Featuring a super-breathable mesh upper, dual-density foam sole, and modern sleek curves engineered for city lifestyle.",
            drawableRes = R.drawable.img_nike_air_1790222474593,
            emojiIcon = "👟",
            sizes = listOf(40, 41, 42, 43, 44),
            colors = listOf("Navy / White", "Pure Platinum", "Triple Black"),
            inStock = true,
            isPopular = true,
            isFlashDeal = true,
            isRecommended = true,
            tags = listOf("Bestseller", "New Release")
        ),
        Product(
            id = "prod-2",
            name = "Minimalist Horizon Smartwatch",
            brand = "Aura Studio",
            category = "watches",
            price = 2199.0,
            originalPrice = 2899.0,
            rating = 4.6,
            reviewCount = 188,
            description = "Sleek aerospace aluminum casing with an ultra-vivid AMOLED edge-to-edge display. Features 7-day battery life, continuous heart rate & SpO2 tracking, and 50m water resistance.",
            emojiIcon = "⌚",
            sizes = listOf(40, 44),
            colors = listOf("Midnight Black", "Starlight Silver", "Deep Navy"),
            inStock = true,
            isPopular = true,
            isFlashDeal = true,
            isRecommended = true,
            tags = listOf("Trending", "Sale")
        ),
        Product(
            id = "prod-3",
            name = "Sony WH-1000XM5 ANC Headphones",
            brand = "Sony",
            category = "audio",
            price = 11499.0,
            originalPrice = 12999.0,
            rating = 4.9,
            reviewCount = 412,
            description = "Industry-leading noise cancellation powered by two processors and eight microphones. Ultra-comfortable lightweight design with soft fit leather, 30-hour battery life, and crystal-clear hands-free calling.",
            emojiIcon = "🎧",
            sizes = emptyList(),
            colors = listOf("Silver", "Black", "Midnight Blue"),
            inStock = true,
            isPopular = true,
            isFlashDeal = false,
            isRecommended = true,
            tags = listOf("Staff Pick", "Audiophile")
        ),
        Product(
            id = "prod-4",
            name = "Heavyweight Oversized Hoodie",
            brand = "Essentials",
            category = "fashion",
            price = 1299.0,
            originalPrice = 1699.0,
            rating = 4.7,
            reviewCount = 95,
            description = "Crafted from 480GSM luxury organic French terry cotton. Features dropped shoulders, a double-lined hood without drawstrings, and a relaxed minimalist silhouette designed to endure.",
            emojiIcon = "👕",
            sizes = listOf(38, 40, 42, 44),
            colors = listOf("Heather Oat", "Washed Black", "Forest Green"),
            inStock = true,
            isPopular = false,
            isFlashDeal = false,
            isRecommended = false,
            tags = listOf("Organic Cotton")
        ),
        Product(
            id = "prod-5",
            name = "Apple MacBook Air 15\" M3",
            brand = "Apple",
            category = "electronics",
            price = 54000.0,
            originalPrice = 58500.0,
            rating = 4.9,
            reviewCount = 89,
            description = "Strikingly thin design with the lightning-fast M3 chip. Delivers up to 18 hours of battery life, a stunning Liquid Retina display, and 1080p FaceTime HD camera.",
            emojiIcon = "💻",
            sizes = emptyList(),
            colors = listOf("Space Gray", "Midnight", "Silver", "Starlight"),
            inStock = true,
            isPopular = true,
            isFlashDeal = false,
            isRecommended = false,
            tags = listOf("Apple Silicon")
        ),
        Product(
            id = "prod-6",
            name = "Barista Smart Ceramic Brewer",
            brand = "Fellow",
            category = "home",
            price = 1650.0,
            originalPrice = 1950.0,
            rating = 4.8,
            reviewCount = 134,
            description = "Double-wall vacuum insulated matte ceramic pour-over dripper. Engineered steep interior angle and micro-pore drainage for an exceptionally clean, aromatic extraction every morning.",
            emojiIcon = "☕",
            sizes = emptyList(),
            colors = listOf("Matte Black", "Warm Sand"),
            inStock = true,
            isPopular = false,
            isFlashDeal = true,
            isRecommended = true,
            tags = listOf("Specialty Coffee")
        ),
        Product(
            id = "prod-7",
            name = "Ultra-Light Aerofly Trainers",
            brand = "Nike",
            category = "footwear",
            price = 2850.0,
            originalPrice = 3200.0,
            rating = 4.5,
            reviewCount = 76,
            description = "Weighing just 180 grams, the Aerofly features dynamic responsive ZoomX cushioning and high-grip rubber pods designed for speed sessions and long distance runs.",
            emojiIcon = "👟",
            sizes = listOf(41, 42, 43, 44),
            colors = listOf("Volt / White", "Phantom Gray"),
            inStock = true,
            isPopular = false,
            isFlashDeal = false,
            isRecommended = true,
            tags = listOf("Running")
        ),
        Product(
            id = "prod-8",
            name = "Minimalist Italian Leather Wallet",
            brand = "Bellroy",
            category = "fashion",
            price = 850.0,
            originalPrice = 1100.0,
            rating = 4.7,
            reviewCount = 162,
            description = "Full-grain vegetable tanned Italian leather with RFID protection. Holds up to 10 cards and flat banknotes while maintaining an ultra-slim 8mm profile.",
            emojiIcon = "👛",
            sizes = emptyList(),
            colors = listOf("Caramel", "Black", "Navy"),
            inStock = true,
            isPopular = true,
            isFlashDeal = false,
            isRecommended = false,
            tags = listOf("Leather")
        )
    )

    val defaultAddresses = listOf(
        Address(
            id = "addr-1",
            title = "Home",
            street = "Villa 221, South 90th St, New Cairo",
            city = "Cairo, Egypt",
            isDefault = true
        ),
        Address(
            id = "addr-2",
            title = "Work",
            street = "Building 4, Smart Village, Km 28 Alex Desert Rd",
            city = "Giza, Egypt",
            isDefault = false
        )
    )

    val initialNotifications = listOf(
        NotificationItem(
            id = "notif-1",
            title = "Your order is 15 minutes away",
            message = "Ahmed is on his way to Villa 221 with your package.",
            timeAgo = "10m ago",
            group = "Today",
            iconEmoji = "🚚",
            isUnread = true,
            orderId = "10452"
        ),
        NotificationItem(
            id = "notif-2",
            title = "20% off your favorite products",
            message = "Limited time offer on Nike footwear and audio gear. Use code SAVE20.",
            timeAgo = "2h ago",
            group = "Today",
            iconEmoji = "🛍",
            isUnread = true
        ),
        NotificationItem(
            id = "notif-3",
            title = "Order #10440 delivered",
            message = "Your package was received and signed for. We hope you enjoy it!",
            timeAgo = "Yesterday",
            group = "Yesterday",
            iconEmoji = "✓",
            isUnread = false,
            orderId = "10440"
        ),
        NotificationItem(
            id = "notif-4",
            title = "Flash Deals Live Now",
            message = "Up to 40% OFF across top electronics and watches.",
            timeAgo = "2 days ago",
            group = "Earlier",
            iconEmoji = "⚡",
            isUnread = false
        )
    )

    fun createInitialOrder(): Order {
        val nike = products[0]
        val watch = products[1]
        val items = listOf(
            CartItem(product = nike, selectedSize = 42, selectedColor = "Navy / White", quantity = 1),
            CartItem(product = watch, selectedSize = 44, selectedColor = "Midnight Black", quantity = 1)
        )
        val subtotal = items.sumOf { it.totalItemPrice }
        val deliveryFee = 100.0
        val discount = 200.0
        val total = subtotal + deliveryFee - discount

        return Order(
            orderId = "10452",
            date = "Today • Sep 23",
            items = items,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            discount = discount,
            total = total,
            status = OrderStatus.OUT_FOR_DELIVERY,
            estimatedDelivery = "Today • 2:45 PM",
            address = "Villa 221, South 90th St, New Cairo",
            courierName = "Ahmed",
            courierPhone = "+20 100 892 3411",
            paymentMethod = "Card (•••• 4821)",
            etaMinutes = 15
        )
    }
}
