package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShippingQuoteRequest(
    @Json(name = "city") val city: String,
    @Json(name = "subtotal") val subtotal: Double
)

@JsonClass(generateAdapter = true)
data class ShippingRateDto(
    @Json(name = "carrier_id") val carrierId: String,
    @Json(name = "carrier_name") val carrierName: String,
    @Json(name = "service_level") val serviceLevel: String,
    @Json(name = "rate") val rate: Double,
    @Json(name = "estimated_days") val estimatedDays: String
)

@JsonClass(generateAdapter = true)
data class TrackingCheckpointDto(
    @Json(name = "status") val status: String,
    @Json(name = "location") val location: String,
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "description") val description: String,
    @Json(name = "completed") val completed: Boolean
)

@JsonClass(generateAdapter = true)
data class ShipmentTrackingDto(
    @Json(name = "tracking_number") val trackingNumber: String,
    @Json(name = "order_id") val orderId: String,
    @Json(name = "carrier_name") val carrierName: String,
    @Json(name = "current_status") val currentStatus: String,
    @Json(name = "courier_name") val courierName: String,
    @Json(name = "courier_phone") val courierPhone: String,
    @Json(name = "estimated_delivery") val estimatedDelivery: String,
    @Json(name = "checkpoints") val checkpoints: List<TrackingCheckpointDto> = emptyList()
)
