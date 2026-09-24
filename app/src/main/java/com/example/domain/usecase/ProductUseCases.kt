package com.example.domain.usecase

import com.example.domain.repository.IProductRepository
import com.example.model.Product
import kotlinx.coroutines.flow.Flow

class GetProductsUseCase(private val productRepository: IProductRepository) {
    operator fun invoke(): Flow<List<Product>> {
        return productRepository.getProducts()
    }

    suspend fun refresh(): Result<Unit> = productRepository.refreshProducts()
}

class GetProductDetailUseCase(private val productRepository: IProductRepository) {
    suspend operator fun invoke(id: String): Result<Product?> {
        return productRepository.getProductById(id)
    }
}

class SearchProductsUseCase(private val productRepository: IProductRepository) {
    suspend operator fun invoke(query: String, category: String? = null): List<Product> {
        return productRepository.searchProducts(query, category)
    }
}
