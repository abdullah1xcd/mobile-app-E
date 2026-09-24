package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ProductReview
import com.example.domain.usecase.AddToCartUseCase
import com.example.domain.usecase.GetProductDetailUseCase
import com.example.domain.usecase.GetProductReviewsUseCase
import com.example.domain.usecase.GetProductsUseCase
import com.example.domain.usecase.SearchProductsUseCase
import com.example.domain.usecase.SubmitReviewUseCase
import com.example.model.Product
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductUiState(
    val allProducts: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val selectedProduct: Product? = null,
    val selectedProductReviews: List<ProductReview> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "all",
    val sortOption: SortOption = SortOption.POPULAR,
    val maxPrice: Double = 60000.0,
    val inStockOnly: Boolean = false,
    val minRating: Double = 0.0,
    val isSearching: Boolean = false
)

class ProductViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val getProductReviewsUseCase: GetProductReviewsUseCase,
    private val submitReviewUseCase: SubmitReviewUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            getProductsUseCase().collect { list ->
                _uiState.update { state ->
                    state.copy(
                        allProducts = list,
                        filteredProducts = applyFilters(list, state)
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearching = true) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // 300ms debounce
            val results = searchProductsUseCase(query, _uiState.value.selectedCategory)
            _uiState.update { state ->
                state.copy(
                    isSearching = false,
                    filteredProducts = applyFilters(results, state)
                )
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { state ->
            val updated = state.copy(selectedCategory = category)
            updated.copy(filteredProducts = applyFilters(state.allProducts, updated))
        }
    }

    fun setSortOption(sort: SortOption) {
        _uiState.update { state ->
            val updated = state.copy(sortOption = sort)
            updated.copy(filteredProducts = applyFilters(state.allProducts, updated))
        }
    }

    fun setPriceRange(maxPrice: Double) {
        _uiState.update { state ->
            val updated = state.copy(maxPrice = maxPrice)
            updated.copy(filteredProducts = applyFilters(state.allProducts, updated))
        }
    }

    fun selectProduct(product: Product) {
        _uiState.update { it.copy(selectedProduct = product) }
        loadProductReviews(product.id)
    }

    fun loadProductReviews(productId: String) {
        viewModelScope.launch {
            val reviews = getProductReviewsUseCase(productId)
            _uiState.update { it.copy(selectedProductReviews = reviews) }
        }
    }

    fun submitReview(productId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val result = submitReviewUseCase(productId, rating, comment)
            result.fold(
                onSuccess = { review ->
                    _uiState.update { state ->
                        state.copy(selectedProductReviews = listOf(review) + state.selectedProductReviews)
                    }
                    _events.emit("Review submitted! Thank you.")
                },
                onFailure = { error ->
                    _events.emit(error.message ?: "Failed to submit review")
                }
            )
        }
    }

    fun addToCart(product: Product, size: Int? = null, color: String? = null, qty: Int = 1) {
        viewModelScope.launch {
            addToCartUseCase(product, size, color, qty)
            _events.emit("Added ${product.name} to cart")
        }
    }

    private fun applyFilters(source: List<Product>, state: ProductUiState): List<Product> {
        var list = source
        if (state.selectedCategory != "all") {
            list = list.filter { it.category.equals(state.selectedCategory, ignoreCase = true) }
        }
        if (state.inStockOnly) {
            list = list.filter { it.inStock }
        }
        if (state.minRating > 0.0) {
            list = list.filter { it.rating >= state.minRating }
        }
        list = list.filter { it.price <= state.maxPrice }

        return when (state.sortOption) {
            SortOption.POPULAR -> list.sortedByDescending { it.reviewCount }
            SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.price }
            SortOption.RATING -> list.sortedByDescending { it.rating }
        }
    }
}
