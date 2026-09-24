package com.example.data.remote.api

import com.example.data.remote.dto.CreateReturnRequestDto
import com.example.data.remote.dto.CreateReviewRequestDto
import com.example.data.remote.dto.InitiatePaymentRequest
import com.example.data.remote.dto.PaymentResponseDto
import com.example.data.remote.dto.ProductReviewDto
import com.example.data.remote.dto.ReturnResponseDto
import com.example.data.remote.dto.ShipmentTrackingDto
import com.example.data.remote.dto.ShippingQuoteRequest
import com.example.data.remote.dto.ShippingRateDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentApi {
    @POST("api/v1/payments/initiate")
    suspend fun initiatePayment(@Body request: InitiatePaymentRequest): Response<PaymentResponseDto>

    @GET("api/v1/payments/{orderId}/status")
    suspend fun getPaymentStatus(@Path("orderId") orderId: String): Response<PaymentResponseDto>
}

interface ShippingApi {
    @POST("api/v1/shipping/rates")
    suspend fun getShippingRates(@Body request: ShippingQuoteRequest): Response<List<ShippingRateDto>>

    @GET("api/v1/shipping/tracking/{orderId}")
    suspend fun getShipmentTracking(@Path("orderId") orderId: String): Response<ShipmentTrackingDto>
}

interface ReviewApi {
    @GET("api/v1/products/{productId}/reviews")
    suspend fun getReviews(@Path("productId") productId: String): Response<List<ProductReviewDto>>

    @POST("api/v1/products/{productId}/reviews")
    suspend fun submitReview(
        @Path("productId") productId: String,
        @Body request: CreateReviewRequestDto
    ): Response<ProductReviewDto>
}

interface ReturnRefundApi {
    @POST("api/v1/returns")
    suspend fun createReturnRequest(@Body request: CreateReturnRequestDto): Response<ReturnResponseDto>

    @GET("api/v1/returns")
    suspend fun getReturnRequests(): Response<List<ReturnResponseDto>>
}

interface NotificationApi {
    @POST("api/v1/notifications/fcm-token")
    suspend fun registerFcmToken(@Body body: Map<String, String>): Response<Unit>
}
