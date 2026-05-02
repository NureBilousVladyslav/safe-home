package com.example.safehome.presentation.main.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safehome.data.api.UserApi
import com.example.safehome.data.model.ErrorResponse
import com.example.safehome.data.model.GeneralNotification
import com.example.safehome.data.model.NotificationItem
import com.example.safehome.data.model.NotificationType
import com.example.safehome.data.model.SecurityNotification
import com.example.safehome.data.repo.TokenRepository
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val userApi: UserApi
) : ViewModel() {
    private val _notificationsGeneralState = MutableStateFlow<List<NotificationItem>>(emptyList())
    private val _notificationsSecurityState = MutableStateFlow<List<NotificationItem>>(emptyList())
    private val _selectedType = MutableStateFlow(NotificationType.GENERAL)

    val notificationsState: StateFlow<List<NotificationItem>> = combine(
        _notificationsSecurityState,
        _notificationsGeneralState,
        _selectedType
    ) { security, general, selectedType ->
        when (selectedType) {
            NotificationType.SECURITY -> security
            NotificationType.GENERAL -> general
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            loadGeneralNotifications()
            loadSecurityNotifications()
        }
    }

    fun setNotificationType(type: NotificationType) {
        _selectedType.value = type
    }

    private suspend fun loadGeneralNotifications() {
        try {
            val token = tokenRepository.getToken()
            val response = userApi.getGeneralNotifications(token)
            if (response.isSuccessful) {
                val notifications = response.body()?.notifications.orEmpty()
                _notificationsGeneralState.value = notifications.map { it.toNotificationItem() }
            } else {
                handleError(response.errorBody()?.string(), _notificationsGeneralState)
            }
        } catch (e: Exception) {
            Timber.tag("NotificationViewModel").e("Network error: ${e.message}")
            _notificationsGeneralState.value = emptyList()
        }
    }

    private suspend fun loadSecurityNotifications() {
        try {
            val token = tokenRepository.getToken()
            val response = userApi.getSecurityNotifications(token)
            if (response.isSuccessful) {
                val notifications = response.body()?.notifications.orEmpty()
                _notificationsSecurityState.value = notifications.map { it.toNotificationItem() }
            } else {
                handleError(response.errorBody()?.string(), _notificationsSecurityState)
            }
        } catch (e: Exception) {
            Timber.tag("NotificationViewModel").e("Network error: ${e.message}")
            _notificationsSecurityState.value = emptyList()
        }
    }

    private fun handleError(errorBody: String?, state: MutableStateFlow<List<NotificationItem>>) {
        val errorMessage = try {
            Gson().fromJson(errorBody, ErrorResponse::class.java)?.message ?: "Unknown error"
        } catch (e: JsonSyntaxException) {
            "Parse error: ${e.message}"
        }
        Timber.tag("NotificationViewModel").e(errorMessage)
        state.value = emptyList()
    }

    private fun GeneralNotification.toNotificationItem() = NotificationItem(
        id = id,
        title = title,
        body = body,
        created_at = created_at
    )

    private fun SecurityNotification.toNotificationItem() = NotificationItem(
        id = id,
        title = title,
        body = body,
        created_at = created_at
    )
}