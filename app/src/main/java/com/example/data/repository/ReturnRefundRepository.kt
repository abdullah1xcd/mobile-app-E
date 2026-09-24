package com.example.data.repository

import com.example.data.datasource.DataSourceConfig
import com.example.data.remote.api.ReturnRefundApi
import com.example.data.remote.dto.CreateReturnRequestDto
import com.example.domain.model.ReturnReason
import com.example.domain.model.ReturnRequest
import com.example.domain.repository.IReturnRefundRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class ReturnRefundRepository(
    private val returnApi: ReturnRefundApi? = null
) : IReturnRefundRepository {

    private val returnRequests = mutableListOf<ReturnRequest>()

    override suspend fun submitReturnRequest(
        orderId: String,
        reason: ReturnReason,
        notes: String,
        amount: Double
    ): Result<ReturnRequest> = withContext(Dispatchers.IO) {
        if (!DataSourceConfig.isMockMode && returnApi != null) {
            try {
                val res = returnApi.createReturnRequest(
                    CreateReturnRequestDto(
                        orderId = orderId,
                        reason = reason.name,
                        notes = notes,
                        refundAmount = amount
                    )
                )
                if (res.isSuccessful && res.body() != null) {
                    val dto = res.body()!!
                    val req = ReturnRequest(
                        returnId = dto.returnId,
                        orderId = dto.orderId,
                        reason = reason,
                        notes = notes,
                        refundAmount = dto.refundAmount,
                        status = dto.status
                    )
                    returnRequests.add(0, req)
                    return@withContext Result.success(req)
                }
            } catch (e: Exception) {
                if (!DataSourceConfig.fallbackToMockOnError) {
                    return@withContext Result.failure(e)
                }
            }
        }

        val localReq = ReturnRequest(
            returnId = "RET-" + UUID.randomUUID().toString().take(6).uppercase(),
            orderId = orderId,
            reason = reason,
            notes = notes,
            refundAmount = amount,
            status = "PENDING_REVIEW"
        )
        returnRequests.add(0, localReq)
        Result.success(localReq)
    }

    override suspend fun getReturnRequests(): List<ReturnRequest> = withContext(Dispatchers.IO) {
        returnRequests
    }
}
