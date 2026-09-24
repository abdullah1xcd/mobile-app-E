package com.example.data.repository

import com.example.data.datasource.DataSourceConfig
import com.example.data.mock.MockShippingDataSource
import com.example.data.remote.api.ShippingApi
import com.example.data.remote.dto.ShippingQuoteRequest
import com.example.domain.model.ShipmentTracking
import com.example.domain.model.ShippingRate
import com.example.domain.model.TrackingCheckpoint
import com.example.domain.repository.IShippingRepository
import com.example.model.OrderStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShippingRepository(
    private val shippingApi: ShippingApi? = null
) : IShippingRepository {

    override suspend fun getAvailableShippingRates(city: String, subtotal: Double): List<ShippingRate> = withContext(Dispatchers.IO) {
        if (!DataSourceConfig.isMockMode && shippingApi != null) {
            try {
                val res = shippingApi.getShippingRates(ShippingQuoteRequest(city = city, subtotal = subtotal))
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    return@withContext res.body()!!.map {
                        ShippingRate(
                            carrierId = it.carrierId,
                            carrierName = it.carrierName,
                            serviceLevel = it.serviceLevel,
                            rate = it.rate,
                            estimatedDeliveryDays = it.estimatedDays
                        )
                    }
                }
            } catch (_: Exception) {}
        }
        MockShippingDataSource.shippingRates
    }

    override suspend fun getShipmentTracking(orderId: String): Result<ShipmentTracking> = withContext(Dispatchers.IO) {
        if (!DataSourceConfig.isMockMode && shippingApi != null) {
            try {
                val res = shippingApi.getShipmentTracking(orderId)
                if (res.isSuccessful && res.body() != null) {
                    val dto = res.body()!!
                    val status = try { OrderStatus.valueOf(dto.currentStatus) } catch (_: Exception) { OrderStatus.SHIPPED }
                    val tracking = ShipmentTracking(
                        trackingNumber = dto.trackingNumber,
                        orderId = dto.orderId,
                        carrierName = dto.carrierName,
                        currentStatus = status,
                        courierName = dto.courierName,
                        courierPhone = dto.courierPhone,
                        estimatedDeliveryTime = dto.estimatedDelivery,
                        checkpoints = dto.checkpoints.map { cp ->
                            TrackingCheckpoint(
                                status = try { OrderStatus.valueOf(cp.status) } catch (_: Exception) { OrderStatus.PENDING },
                                location = cp.location,
                                timestamp = cp.timestamp,
                                description = cp.description,
                                isCompleted = cp.completed
                            )
                        }
                    )
                    return@withContext Result.success(tracking)
                }
            } catch (e: Exception) {
                if (!DataSourceConfig.fallbackToMockOnError) {
                    return@withContext Result.failure(e)
                }
            }
        }
        Result.success(MockShippingDataSource.getTracking(orderId))
    }
}
