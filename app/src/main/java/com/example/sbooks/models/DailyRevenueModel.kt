package com.example.sbooks.models
data class DailyRevenueModel(
    val date: String = "",
    val revenue: Double = 0.0,
    val orderCount: Int = 0
) {
    fun getFormattedRevenue(): String = String.format("%,.0f", revenue)
}