package com.example.data.repository

import com.example.data.SampleData
import com.example.data.local.ProductDao
import com.example.data.local.ProductEntity
import com.example.data.remote.api.LuminaApiService
import com.example.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ProductRepository(
    private val productDao: ProductDao,
    private val apiService: LuminaApiService
) {
    // In-memory catalog populated from SampleData + cached entities
    private var cachedList: List<Product> = SampleData.products

    fun getProducts(): Flow<List<Product>> = flow {
        // Emit in-memory/cached first for instant zero-latency UI load
        emit(cachedList)

        // Sync with remote API if reachable
        try {
            val response = apiService.getProducts()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val remoteProducts = response.body()!!
                cachedList = remoteProducts
                productDao.insertAll(remoteProducts.map { it.toEntity() })
                emit(remoteProducts)
            }
        } catch (_: Exception) {
            // Keep using cached products
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getProductById(id: String): Product? = withContext(Dispatchers.IO) {
        cachedList.find { it.id == id } ?: try {
            val res = apiService.getProductById(id)
            if (res.isSuccessful) res.body() else null
        } catch (_: Exception) {
            null
        }
    }

    private fun Product.toEntity(): ProductEntity {
        return ProductEntity(
            id = id,
            name = name,
            brand = brand,
            category = category,
            price = price,
            originalPrice = originalPrice,
            rating = rating,
            reviewCount = reviewCount,
            description = description,
            emojiIcon = emojiIcon,
            inStock = inStock,
            isPopular = isPopular,
            isFlashDeal = isFlashDeal,
            isRecommended = isRecommended,
            stockQuantity = stockQuantity
        )
    }
}
