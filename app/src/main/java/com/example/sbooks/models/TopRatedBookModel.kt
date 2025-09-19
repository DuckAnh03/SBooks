package com.example.sbooks.models
data class TopRatedBookModel(
    val bookId: Int = 0,
    val title: String = "",
    val author: String = "",
    val image: String = "",
    val price: Double = 0.0,
    val rating: Float = 0.0f,
    val reviewCount: Int = 0,
    val stock: Int = 0
) {
    fun getFormattedPrice(): String = String.format("%,.0f VNĐ", price)
    fun getRatingStars(): String = "★".repeat(rating.toInt()) + "☆".repeat(5 - rating.toInt())
    fun getReviewText(): String = "($reviewCount đánh giá)"
    fun getStockText(): String = if (stock > 0) "Còn $stock" else "Hết hàng"
}