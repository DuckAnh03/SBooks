package com.example.sbooks.models
data class BestSellerBookModel(
    val bookId: Int = 0,
    val title: String = "",
    val author: String = "",
    val image: String = "",
    val price: Double = 0.0,
    val soldQuantity: Int = 0,
    val revenue: Double = 0.0,
    val rank: Int = 0
) {
    fun getFormattedPrice(): String = String.format("%,.0f VNĐ", price)
    fun getFormattedRevenue(): String = String.format("%,.0f VNĐ", revenue)
    fun getSoldText(): String = "$soldQuantity cuốn"
}