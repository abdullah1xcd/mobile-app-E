package com.example.data.remote.api

import com.example.data.remote.dto.AuthResponseDto
import com.example.data.remote.dto.CouponValidateRequest
import com.example.data.remote.dto.CouponValidateResponseDto
import com.example.data.remote.dto.CreateOrderRequest
import com.example.data.remote.dto.LoginRequest
import com.example.data.remote.dto.OrderResponseDto
import com.example.data.remote.dto.RegisterRequest
import com.example.model.Product
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LuminaApiService {

    // Auth Endpoints
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponseDto>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponseDto>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Unit>

    // Product Endpoints
    @GET("api/v1/products")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<List<Product>>

    @GET("api/v1/products/{id}")
    suspend fun getProductById(@Path("id") id: String): Response<Product>

    // Order Endpoints
    @POST("api/v1/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<OrderResponseDto>

    @GET("api/v1/orders")
    suspend fun getOrders(): Response<List<OrderResponseDto>>

    @GET("api/v1/orders/{id}")
    suspend fun getOrderById(@Path("id") id: String): Response<OrderResponseDto>

    @PATCH("api/v1/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<OrderResponseDto>

    // Coupon Validation
    @POST("api/v1/coupons/validate")
    suspend fun validateCoupon(@Body request: CouponValidateRequest): Response<CouponValidateResponseDto>
}
