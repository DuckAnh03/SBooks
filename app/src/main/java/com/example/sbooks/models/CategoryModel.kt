package com.example.sbooks.models
data class CategoryModel(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val bookCount: Int = 0,
    val status: CategoryStatus = CategoryStatus.ACTIVE,
    val sortOrder: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    enum class CategoryStatus(val value: String, val displayName: String) {
        ACTIVE("active", "Đang hoạt động"),
        INACTIVE("inactive", "Không hoạt động");

        companion object {
            fun fromValue(value: String): CategoryStatus {
                return values().find { it.value == value } ?: ACTIVE
            }
        }
    }

    fun getDisplayStatus(): String = status.displayName
    fun isActive(): Boolean = status == CategoryStatus.ACTIVE
    fun getBookCountText(): String = "$bookCount cuốn sách"
}