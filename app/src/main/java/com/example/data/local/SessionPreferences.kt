package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.model.User
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class SessionPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lumina_session_prefs", Context.MODE_PRIVATE)

    private val _currentUserFlow = MutableStateFlow<User?>(loadUserFromPrefs())
    val currentUserFlow: StateFlow<User?> = _currentUserFlow.asStateFlow()

    private val _pushNotificationsEnabledFlow = MutableStateFlow(
        prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true)
    )
    val pushNotificationsEnabledFlow: StateFlow<Boolean> = _pushNotificationsEnabledFlow.asStateFlow()

    private val _fastCheckoutEnabledFlow = MutableStateFlow(
        prefs.getBoolean(KEY_FAST_CHECKOUT, true)
    )
    val fastCheckoutEnabledFlow: StateFlow<Boolean> = _fastCheckoutEnabledFlow.asStateFlow()

    fun saveAuthSession(accessToken: String, refreshToken: String, user: User) {
        val userJson = JSONObject().apply {
            put("id", user.id)
            put("name", user.name)
            put("email", user.email)
            put("role", user.role.name)
            put("phone", user.phone ?: "")
            put("avatarUrl", user.avatarUrl ?: "")
        }.toString()

        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER_DATA, userJson)
            .apply()

        _currentUserFlow.value = user
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getCurrentUser(): User? = _currentUserFlow.value

    fun clearSession() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_DATA)
            .apply()

        _currentUserFlow.value = null
    }

    fun setPushNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, enabled).apply()
        _pushNotificationsEnabledFlow.value = enabled
    }

    fun setFastCheckoutEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FAST_CHECKOUT, enabled).apply()
        _fastCheckoutEnabledFlow.value = enabled
    }

    private fun loadUserFromPrefs(): User? {
        val jsonStr = prefs.getString(KEY_USER_DATA, null) ?: return null
        return try {
            val json = JSONObject(jsonStr)
            val roleStr = json.optString("role", UserRole.CUSTOMER.name)
            val role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.CUSTOMER }
            User(
                id = json.optString("id", "usr-1"),
                name = json.optString("name", "User"),
                email = json.optString("email", ""),
                role = role,
                phone = json.optString("phone").ifBlank { null },
                avatarUrl = json.optString("avatarUrl").ifBlank { null }
            )
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_PUSH_NOTIFICATIONS = "push_notifications"
        private const val KEY_FAST_CHECKOUT = "fast_checkout"
    }
}
