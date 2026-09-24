package com.example.data.repository

import com.example.data.datasource.DataSourceConfig
import com.example.data.mock.MockPaymentDataSource
import com.example.data.remote.api.PaymentApi
import com.example.data.remote.dto.InitiatePaymentRequest
import com.example.domain.model.PaymentResult
import com.example.domain.repository.IPaymentRepository
import com.example.model.PaymentStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository(
    private val paymentApi: PaymentApi? = null
) : IPaymentRepository {

    override suspend fun processPayment(
        orderId: String,
        amount: Double,
        paymentMethod: String,
        cardLast4: String?
    ): Result<PaymentResult> = withContext(Dispatchers.IO) {
        if (!DataSourceConfig.isMockMode && paymentApi != null) {
            try {
                val response = paymentApi.initiatePayment(
                    InitiatePaymentRequest(
                        orderId = orderId,
                        amount = amount,
                        paymentMethod = paymentMethod,
                        cardLast4 = cardLast4
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val status = when (body.status.uppercase()) {
                        "PAID" -> PaymentStatus.PAID
                        "PENDING" -> PaymentStatus.PENDING
                        else -> PaymentStatus.FAILED
                    }
                    return@withContext Result.success(
                        PaymentResult(
                            isSuccess = status == PaymentStatus.PAID || status == PaymentStatus.PENDING,
                            transactionId = body.transactionId,
                            message = body.message,
                            paymentStatus = status
                        )
                    )
                }
            } catch (e: Exception) {
                if (!DataSourceConfig.fallbackToMockOnError) {
                    return@withContext Result.failure(e)
                }
            }
        }

        // Mock payment processing with proper transaction handling
        val result = MockPaymentDataSource.processPayment(orderId, amount, paymentMethod, cardLast4)
        Result.success(result)
    }
}
