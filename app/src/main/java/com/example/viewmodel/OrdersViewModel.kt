package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ReturnReason
import com.example.domain.usecase.GetOrdersUseCase
import com.example.domain.usecase.RequestReturnUseCase
import com.example.model.Order
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val selectedStatusFilter: String = "ALL",
    val isLoading: Boolean = false
)

class OrdersViewModel(
    private val getOrdersUseCase: GetOrdersUseCase,
    private val requestReturnUseCase: RequestReturnUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            getOrdersUseCase().collect { orderList ->
                _uiState.update { it.copy(orders = orderList) }
            }
        }
    }

    fun requestReturn(orderId: String, reason: ReturnReason, notes: String, amount: Double) {
        viewModelScope.launch {
            val result = requestReturnUseCase(orderId, reason, notes, amount)
            result.fold(
                onSuccess = { req ->
                    _events.emit("Return request #${req.returnId} submitted for review")
                },
                onFailure = { error ->
                    _events.emit(error.message ?: "Failed to submit return request")
                }
            )
        }
    }
}
