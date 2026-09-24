package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InitiatePaymentRequest(
    @Json(name = "order_id") val orderId: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "currency") val currency: String = "EGP",
    @Json(name = "payment_method") val paymentMethod: String,
    @Json(name = "card_last4") val cardLast4: String? = null
)

@JsonClass(generateAdapter = true)
data class PaymentResponseDto(
    @Json(name = "transaction_id") val transactionId: String,
    @Json(name = "order_id") val orderId: String,
    @Json(name = "status") val status: String, // PAID, PENDING, FAILED
    @Json(name = "amount") val amount: Double,
    @Json(name = "gateway_reference") val gatewayReference: String?,
    @Json(name = "message") val message: String
)
