package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.SampleData
import com.example.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NotificationsUiState(
    val notifications: List<NotificationItem> = SampleData.initialNotifications
) {
    val unreadCount: Int
        get() = notifications.count { it.isUnread }
}

class NotificationsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun markAllAsRead() {
        _uiState.update { state ->
            state.copy(notifications = state.notifications.map { it.copy(isUnread = false) })
        }
    }

    fun markAsRead(id: String) {
        _uiState.update { state ->
            state.copy(notifications = state.notifications.map {
                if (it.id == id) it.copy(isUnread = false) else it
            })
        }
    }
}
