package com.example.sbooks.models
data class BookModel(
    val id: Int = 0,
    val title: String = "",
    val author: String = "",
    val publisher: String = "",
    val categoryId: Int = 0,
    val categoryName: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val description: String = "",
    val image: String = "",
    val isbn: String = "",
    val pages: Int = 0,
    val language: String = "Tiếng Việt",
    val publicationYear: Int = 0,
    var rating: Float = 0.0f,
    var reviewCount: Int = 0,
    val soldCount: Int = 0,
    val status: BookStatus = BookStatus.ACTIVE,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    enum class BookStatus(val value: String, val displayName: String) {
        ACTIVE("active", "Đang bán"),
        INACTIVE("inactive", "Ngưng bán"),
        OUT_OF_STOCK("out_of_stock", "Hết hàng");

        companion object {
            fun fromValue(value: String): BookStatus {
                return values().find { it.value == value } ?: ACTIVE
            }
        }
    }

    fun getFormattedPrice(): String = String.format("%,.0f VNĐ", price)
    fun getStockStatus(): StockLevel {
        return when {
            stock <= 0 -> StockLevel.OUT_OF_STOCK
            stock <= 10 -> StockLevel.LOW_STOCK
            stock <= 50 -> StockLevel.MEDIUM_STOCK
            else -> StockLevel.HIGH_STOCK
        }
    }
    fun isLowStock(): Boolean = stock <= 10
    fun isOutOfStock(): Boolean = stock <= 0
    fun isAvailable(): Boolean = status == BookStatus.ACTIVE && stock > 0
    fun getRatingStars(): String = "★".repeat(rating.toInt()) + "☆".repeat(5 - rating.toInt())

    enum class StockLevel(val displayName: String) {
        OUT_OF_STOCK("Hết hàng"),
        LOW_STOCK("Sắp hết"),
        MEDIUM_STOCK("Còn ít"),
        HIGH_STOCK("Còn nhiều")
    }
}
