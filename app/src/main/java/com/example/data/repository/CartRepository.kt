package com.example.data.repository

import com.example.data.SampleData
import com.example.data.local.CartDao
import com.example.data.local.CartItemEntity
import com.example.model.CartItem
import com.example.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CartRepository(
    private val cartDao: CartDao
) {
    val cartItemsFlow: Flow<List<CartItem>> = cartDao.getAllCartItems().map { entities ->
        entities.map { entity ->
            val product = SampleData.products.find { it.id == entity.productId }
                ?: Product(
                    id = entity.productId,
                    name = entity.productName,
                    brand = entity.productBrand,
                    category = entity.productCategory,
                    price = entity.productPrice,
                    rating = 4.8,
                    reviewCount = 120,
                    description = "",
                    emojiIcon = entity.productEmoji
                )
            CartItem(
                product = product,
                selectedSize = entity.selectedSize,
                selectedColor = entity.selectedColor,
                quantity = entity.quantity
            )
        }
    }

    suspend fun addToCart(
        product: Product,
        size: Int? = null,
        color: String? = null,
        quantity: Int = 1
    ) = withContext(Dispatchers.IO) {
        val entity = CartItemEntity(
            productId = product.id,
            productName = product.name,
            productBrand = product.brand,
            productCategory = product.category,
            productPrice = product.price,
            productEmoji = product.emojiIcon,
            selectedSize = size,
            selectedColor = color,
            quantity = quantity
        )
        cartDao.insertItem(entity)
    }

    suspend fun updateQuantity(item: CartItem, delta: Int) = withContext(Dispatchers.IO) {
        val newQuantity = item.quantity + delta
        if (newQuantity <= 0) {
            cartDao.deleteByProductId(item.product.id)
        } else {
            val entity = CartItemEntity(
                productId = item.product.id,
                productName = item.product.name,
                productBrand = item.product.brand,
                productCategory = item.product.category,
                productPrice = item.product.price,
                productEmoji = item.product.emojiIcon,
                selectedSize = item.selectedSize,
                selectedColor = item.selectedColor,
                quantity = newQuantity
            )
            cartDao.insertItem(entity)
        }
    }

    suspend fun removeFromCart(item: CartItem) = withContext(Dispatchers.IO) {
        cartDao.deleteByProductId(item.product.id)
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clearCart()
    }
}
