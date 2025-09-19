package com.example.sbooks.models
data class ReportModel(
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val totalCustomers: Int = 0,
    val totalBooks: Int = 0,
    val averageOrderValue: Double = 0.0,
    val periodStartDate: String = "",
    val periodEndDate: String = "",
    val bestSellingBooks: List<BestSellerBookModel> = emptyList(),
    val topRatedBooks: List<TopRatedBookModel> = emptyList(),
    val orderStatusBreakdown: Map<OrderModel.OrderStatus, Int> = emptyMap(),
    val dailyRevenue: List<DailyRevenueModel> = emptyList()
) {
    fun getFormattedRevenue(): String = String.format("%,.0f VNĐ", totalRevenue)
    fun getFormattedAverageOrder(): String = String.format("%,.0f VNĐ", averageOrderValue)
}