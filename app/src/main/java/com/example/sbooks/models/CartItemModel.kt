package com.example.sbooks.models
data class CartItemModel(
    val id: Int = 0,
    val userId: Int = 0,
    val bookId: Int = 0,
    val bookTitle: String = "",
    val bookAuthor: String = "",
    val bookImage: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val isSelected: Boolean = true
) {
    fun getTotalPrice(): Double = price * quantity
    fun getFormattedPrice(): String = String.format("%,.0f VNĐ", price)
    fun getFormattedTotal(): String = String.format("%,.0f VNĐ", getTotalPrice())
}