package com.example.sbooks.models
data class OrderItemModel(
    val id: Int = 0,
    val orderId: Int = 0,
    val bookId: Int = 0,
    val bookTitle: String = "",
    val bookAuthor: String = "",
    val bookImage: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val totalPrice: Double = 0.0
) {
    fun getFormattedPrice(): String = String.format("%,.0f VNĐ", price)
    fun getFormattedTotal(): String = String.format("%,.0f VNĐ", totalPrice)
}