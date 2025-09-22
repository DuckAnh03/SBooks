package com.example.sbooks.models
data class UserModel(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val fullName: String = "",
    val address: String = "",
    var password: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val status: UserStatus = UserStatus.ACTIVE,
    val avatar: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    enum class UserRole(val value: String, val displayName: String) {
        ADMIN("admin", "Quản trị viên"),
        STAFF("staff", "Nhân viên"),
        CUSTOMER("customer", "Khách hàng");

        companion object {
            fun fromValue(value: String): UserRole {
                return values().find { it.value == value } ?: CUSTOMER
            }
        }
    }

    enum class UserStatus(val value: String, val displayName: String) {
        ACTIVE("active", "Hoạt động"),
        INACTIVE("inactive", "Không hoạt động"),
        SUSPENDED("suspended", "Tạm khóa");

        companion object {
            fun fromValue(value: String): UserStatus {
                return values().find { it.value == value } ?: ACTIVE
            }
        }
    }

    fun getDisplayRole(): String = role.displayName
    fun getDisplayStatus(): String = status.displayName
    fun isAdmin(): Boolean = role == UserRole.ADMIN
    fun isStaff(): Boolean = role == UserRole.STAFF
    fun isCustomer(): Boolean = role == UserRole.CUSTOMER
    fun isActive(): Boolean = status == UserStatus.ACTIVE
}

