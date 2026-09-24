package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductReviewDto(
    @Json(name = "id") val id: String,
    @Json(name = "product_id") val productId: String,
    @Json(name = "author_name") val authorName: String,
    @Json(name = "rating") val rating: Int,
    @Json(name = "comment") val comment: String,
    @Json(name = "date") val date: String,
    @Json(name = "is_verified_purchase") val isVerifiedPurchase: Boolean = true,
    @Json(name = "helpful_count") val helpfulCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class CreateReviewRequestDto(
    @Json(name = "product_id") val productId: String,
    @Json(name = "rating") val rating: Int,
    @Json(name = "comment") val comment: String
)

@JsonClass(generateAdapter = true)
data class CreateReturnRequestDto(
    @Json(name = "order_id") val orderId: String,
    @Json(name = "reason") val reason: String,
    @Json(name = "notes") val notes: String,
    @Json(name = "refund_amount") val refundAmount: Double
)

@JsonClass(generateAdapter = true)
data class ReturnResponseDto(
    @Json(name = "return_id") val returnId: String,
    @Json(name = "order_id") val orderId: String,
    @Json(name = "status") val status: String,
    @Json(name = "refund_amount") val refundAmount: Double,
    @Json(name = "message") val message: String
)
