package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ShipmentTracking
import com.example.domain.usecase.GetOrderTrackingUseCase
import com.example.domain.usecase.GetShipmentTrackingUseCase
import com.example.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrackingUiState(
    val order: Order? = null,
    val shipmentTracking: ShipmentTracking? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TrackingViewModel(
    private val getOrderTrackingUseCase: GetOrderTrackingUseCase,
    private val getShipmentTrackingUseCase: GetShipmentTrackingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    fun loadOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val orderResult = getOrderTrackingUseCase(orderId)
            val trackingResult = getShipmentTrackingUseCase(orderId)

            val order = orderResult.getOrNull()
            val tracking = trackingResult.getOrNull()

            _uiState.update {
                it.copy(
                    order = order,
                    shipmentTracking = tracking,
                    isLoading = false
                )
            }
        }
    }
}
