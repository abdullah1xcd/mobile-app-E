package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val productId: String,
    val productName: String,
    val productBrand: String,
    val productCategory: String,
    val productPrice: Double,
    val productEmoji: String,
    val selectedSize: Int?,
    val selectedColor: String?,
    val quantity: Int
)

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY localId ASC")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CartItemEntity): Long

    @Query("UPDATE cart_items SET quantity = :quantity WHERE localId = :localId")
    suspend fun updateQuantity(localId: Long, quantity: Int)

    @Query("DELETE FROM cart_items WHERE localId = :localId")
    suspend fun deleteById(localId: Long)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteByProductId(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}
