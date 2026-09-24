package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductVariantDto(
    @Json(name = "variant_id") val variantId: String,
    @Json(name = "sku") val sku: String,
    @Json(name = "size") val size: Int? = null,
    @Json(name = "color") val color: String? = null,
    @Json(name = "price") val price: Double,
    @Json(name = "stock") val stock: Int
)

@JsonClass(generateAdapter = true)
data class ProductDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "brand") val brand: String,
    @Json(name = "category") val category: String,
    @Json(name = "price") val price: Double,
    @Json(name = "original_price") val originalPrice: Double? = null,
    @Json(name = "sku") val sku: String? = null,
    @Json(name = "rating") val rating: Double = 4.5,
    @Json(name = "review_count") val reviewCount: Int = 0,
    @Json(name = "description") val description: String = "",
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "images") val images: List<String> = emptyList(),
    @Json(name = "seller") val seller: String = "Lumina Official Store",
    @Json(name = "stock_quantity") val stockQuantity: Int = 25,
    @Json(name = "sizes") val sizes: List<Int> = emptyList(),
    @Json(name = "colors") val colors: List<String> = emptyList(),
    @Json(name = "in_stock") val inStock: Boolean = true,
    @Json(name = "is_popular") val isPopular: Boolean = false,
    @Json(name = "is_flash_deal") val isFlashDeal: Boolean = false,
    @Json(name = "is_recommended") val isRecommended: Boolean = false,
    @Json(name = "tags") val tags: List<String> = emptyList(),
    @Json(name = "variants") val variants: List<ProductVariantDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ProductPaginationResponse(
    @Json(name = "data") val data: List<ProductDto>,
    @Json(name = "page") val page: Int,
    @Json(name = "limit") val limit: Int,
    @Json(name = "total") val total: Int,
    @Json(name = "total_pages") val totalPages: Int
)
