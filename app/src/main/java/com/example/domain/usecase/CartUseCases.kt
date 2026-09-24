package com.example.domain.usecase

import com.example.domain.repository.ICartRepository
import com.example.model.CartItem
import com.example.model.Product
import kotlinx.coroutines.flow.Flow

class GetCartUseCase(private val cartRepository: ICartRepository) {
    operator fun invoke(): Flow<List<CartItem>> = cartRepository.cartItemsFlow
}

class AddToCartUseCase(private val cartRepository: ICartRepository) {
    suspend operator fun invoke(
        product: Product,
        size: Int? = null,
        color: String? = null,
        quantity: Int = 1
    ) {
        val qty = quantity.coerceAtLeast(1)
        cartRepository.addToCart(product, size, color, qty)
    }
}

class UpdateCartQuantityUseCase(private val cartRepository: ICartRepository) {
    suspend operator fun invoke(item: CartItem, delta: Int) {
        cartRepository.updateQuantity(item, delta)
    }
}

class RemoveFromCartUseCase(private val cartRepository: ICartRepository) {
    suspend operator fun invoke(item: CartItem) {
        cartRepository.removeFromCart(item)
    }
}

class ClearCartUseCase(private val cartRepository: ICartRepository) {
    suspend operator fun invoke() {
        cartRepository.clearCart()
    }
}
