package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.domain.usecase.AddToCartUseCase
import com.example.domain.usecase.GetCartUseCase
import com.example.domain.usecase.GetCurrentUserUseCase
import com.example.domain.usecase.GetProductsUseCase
import com.example.model.Product
import com.example.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val currentUser: User? = null,
    val products: List<Product> = emptyList(),
    val flashDeals: List<Product> = emptyList(),
    val popularProducts: List<Product> = emptyList(),
    val recommendedProducts: List<Product> = emptyList(),
    val cartItemCount: Int = 0,
    val wishlistIds: Set<String> = setOf("prod-1", "prod-2"),
    val selectedCategory: String = "all",
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val getCartUseCase: GetCartUseCase,
    getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(products = SampleData.products))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        // Collect current user
        viewModelScope.launch {
            getCurrentUserUseCase.currentUserFlow.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }

        // Collect products
        viewModelScope.launch {
            getProductsUseCase().collect { productList ->
                _uiState.update { state ->
                    state.copy(
                        products = productList,
                        flashDeals = productList.filter { it.isFlashDeal },
                        popularProducts = productList.filter { it.isPopular },
                        recommendedProducts = productList.filter { it.isRecommended }
                    )
                }
            }
        }

        // Collect cart count
        viewModelScope.launch {
            getCartUseCase().collect { items ->
                val totalCount = items.sumOf { it.quantity }
                _uiState.update { it.copy(cartItemCount = totalCount) }
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            addToCartUseCase(product)
            _events.emit("Added ${product.name} to cart")
        }
    }

    fun toggleWishlist(productId: String) {
        _uiState.update { state ->
            val set = state.wishlistIds.toMutableSet()
            if (set.contains(productId)) set.remove(productId) else set.add(productId)
            state.copy(wishlistIds = set)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getProductsUseCase.refresh()
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
