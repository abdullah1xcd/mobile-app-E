package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val brand: String,
    val category: String,
    val price: Double,
    val originalPrice: Double?,
    val rating: Double,
    val reviewCount: Int,
    val description: String,
    val emojiIcon: String,
    val inStock: Boolean,
    val isPopular: Boolean,
    val isFlashDeal: Boolean,
    val isRecommended: Boolean,
    val stockQuantity: Int
)

@Dao
interface ProductDao {
    @Query("SELECT * FROM cached_products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM cached_products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM cached_products WHERE category = :category")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Query("DELETE FROM cached_products")
    suspend fun clearAll()
}
