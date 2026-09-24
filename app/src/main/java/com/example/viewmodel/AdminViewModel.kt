package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetCurrentUserUseCase
import com.example.domain.usecase.GetOrdersUseCase
import com.example.domain.usecase.UpdateOrderStatusUseCase
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val currentUser: User? = null,
    val orders: List<Order> = emptyList(),
    val isAuthorized: Boolean = false,
    val selectedStatusFilter: String = "ALL",
    val isLoading: Boolean = false
) {
    val totalRevenue: Double
        get() = orders.sumOf { it.total }

    val pendingCount: Int
        get() = orders.count { it.status == OrderStatus.PENDING || it.status == OrderStatus.CONFIRMED }

    val outForDeliveryCount: Int
        get() = orders.count { it.status == OrderStatus.OUT_FOR_DELIVERY }
}

class AdminViewModel(
    private val getOrdersUseCase: GetOrdersUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
    getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase.currentUserFlow.collect { user ->
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isAuthorized = user?.isAdmin == true
                    )
                }
            }
        }

        viewModelScope.launch {
            getOrdersUseCase().collect { orderList ->
                _uiState.update { it.copy(orders = orderList) }
            }
        }
    }

    fun updateStatus(orderId: String, newStatus: OrderStatus) {
        if (!_uiState.value.isAuthorized) {
            viewModelScope.launch {
                _events.emit("Unauthorized: Admin role required to modify order status")
            }
            return
        }

        viewModelScope.launch {
            val result = updateOrderStatusUseCase(orderId, newStatus)
            result.fold(
                onSuccess = { order ->
                    _events.emit("Order #${order.orderId} moved to ${newStatus.label}")
                },
                onFailure = { error ->
                    _events.emit(error.message ?: "Failed to update order status")
                }
            )
        }
    }
}
