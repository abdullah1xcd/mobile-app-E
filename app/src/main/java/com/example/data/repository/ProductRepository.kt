package com.example.data.repository

import com.example.data.SampleData
import com.example.data.datasource.DataSourceConfig
import com.example.data.local.ProductDao
import com.example.data.local.ProductEntity
import com.example.data.remote.api.LuminaApiService
import com.example.domain.repository.IProductRepository
import com.example.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ProductRepository(
    private val productDao: ProductDao,
    private val apiService: LuminaApiService
) : IProductRepository {

    private var cachedList: List<Product> = SampleData.products

    override fun getProducts(): Flow<List<Product>> = flow {
        // 1. Emit cached/local first for instant zero-latency UI load
        emit(cachedList)

        // 2. Fetch from remote if not in strict mock mode
        if (!DataSourceConfig.isMockMode) {
            try {
                val response = apiService.getProducts()
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val remoteProducts = response.body()!!
                    cachedList = remoteProducts
                    productDao.insertAll(remoteProducts.map { it.toEntity() })
                    emit(remoteProducts)
                }
            } catch (_: Exception) {
                // Keep displaying cached catalog
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getProductById(id: String): Result<Product?> = withContext(Dispatchers.IO) {
        val found = cachedList.find { it.id == id }
        if (found != null) {
            return@withContext Result.success(found)
        }
        if (!DataSourceConfig.isMockMode) {
            try {
                val res = apiService.getProductById(id)
                if (res.isSuccessful && res.body() != null) {
                    return@withContext Result.success(res.body())
                }
            } catch (e: Exception) {
                // fall through
            }
        }
        Result.success(null)
    }

    override suspend fun searchProducts(query: String, category: String?): List<Product> = withContext(Dispatchers.IO) {
        val q = query.trim().lowercase()
        cachedList.filter { product ->
            val matchesCategory = category.isNullOrBlank() || category == "all" || product.category.equals(category, ignoreCase = true)
            val matchesQuery = q.isEmpty() ||
                product.name.lowercase().contains(q) ||
                product.brand.lowercase().contains(q) ||
                product.description.lowercase().contains(q)
            matchesCategory && matchesQuery
        }
    }

    override suspend fun refreshProducts(): Result<Unit> = withContext(Dispatchers.IO) {
        if (!DataSourceConfig.isMockMode) {
            try {
                val response = apiService.getProducts()
                if (response.isSuccessful && response.body() != null) {
                    val remoteProducts = response.body()!!
                    cachedList = remoteProducts
                    productDao.insertAll(remoteProducts.map { it.toEntity() })
                    return@withContext Result.success(Unit)
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
        Result.success(Unit)
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
