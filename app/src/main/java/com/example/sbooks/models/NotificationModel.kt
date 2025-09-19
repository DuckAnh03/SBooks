package com.example.sbooks.models
data class NotificationModel(
    val id: Int = 0,
    val userId: Int = 0,
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.INFO,
    val isRead: Boolean = false,
    val relatedId: Int? = null,
    val createdAt: String = ""
) {
    enum class NotificationType(val value: String, val displayName: String) {
        INFO("info", "Thông tin"),
        ORDER("order", "Đơn hàng"),
        PROMOTION("promotion", "Khuyến mãi"),
        SYSTEM("system", "Hệ thống");

        companion object {
            fun fromValue(value: String): NotificationType {
                return values().find { it.value == value } ?: INFO
            }
        }
    }
}
