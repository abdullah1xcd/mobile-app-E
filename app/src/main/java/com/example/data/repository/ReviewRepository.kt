package com.example.data.repository

import com.example.data.datasource.DataSourceConfig
import com.example.data.mock.MockReviewDataSource
import com.example.data.remote.api.ReviewApi
import com.example.data.remote.dto.CreateReviewRequestDto
import com.example.domain.model.ProductReview
import com.example.domain.repository.IReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReviewRepository(
    private val reviewApi: ReviewApi? = null
) : IReviewRepository {

    override suspend fun getReviewsForProduct(productId: String): List<ProductReview> = withContext(Dispatchers.IO) {
        if (!DataSourceConfig.isMockMode && reviewApi != null) {
            try {
                val res = reviewApi.getReviews(productId)
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    return@withContext res.body()!!.map {
                        ProductReview(
                            id = it.id,
                            productId = it.productId,
                            authorName = it.authorName,
                            rating = it.rating,
                            comment = it.comment,
                            date = it.date,
                            isVerifiedPurchase = it.isVerifiedPurchase,
                            helpfulCount = it.helpfulCount
                        )
                    }
                }
            } catch (_: Exception) {}
        }
        MockReviewDataSource.getReviews(productId)
    }

    override suspend fun submitReview(
        productId: String,
        rating: Int,
        comment: String
    ): Result<ProductReview> = withContext(Dispatchers.IO) {
        if (!DataSourceConfig.isMockMode && reviewApi != null) {
            try {
                val res = reviewApi.submitReview(
                    productId,
                    CreateReviewRequestDto(productId, rating, comment)
                )
                if (res.isSuccessful && res.body() != null) {
                    val it = res.body()!!
                    return@withContext Result.success(
                        ProductReview(
                            id = it.id,
                            productId = it.productId,
                            authorName = it.authorName,
                            rating = it.rating,
                            comment = it.comment,
                            date = it.date,
                            isVerifiedPurchase = it.isVerifiedPurchase,
                            helpfulCount = it.helpfulCount
                        )
                    )
                }
            } catch (e: Exception) {
                if (!DataSourceConfig.fallbackToMockOnError) {
                    return@withContext Result.failure(e)
                }
            }
        }

        val review = MockReviewDataSource.addReview(productId, rating, comment)
        Result.success(review)
    }
}
