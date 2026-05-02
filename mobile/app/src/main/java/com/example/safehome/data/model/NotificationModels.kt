package com.example.safehome.data.model

enum class NotificationType {
    SECURITY, GENERAL
}

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val created_at: String
)

data class GeneralNotificationResponse(
    val notifications: List<GeneralNotification>
)

data class GeneralNotification(
    val id: String,
    val title: String,
    val body: String,
    val created_at: String,
    val importance: String,
    val type: String,
    val data: Map<String, Any>
)

data class SecurityNotificationResponse(
    val notifications: List<SecurityNotification>
)

data class SecurityNotification(
    val id: String,
    val title: String,
    val body: String,
    val created_at: String,
    val importance: String,
    val type: String,
    val data: Map<String, Any>,
    val home_id: String,
    val sensor_id: String
)
