package com.example.sbooks.models
data class ReviewModel(
    val id: Int = 0,
    val bookId: Int = 0,
    val userId: Int = 0,
    val userName: String = "",
    val userAvatar: String = "",
    val rating: Float = 0.0f,
    val comment: String = "",
    val isVerifiedPurchase: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    fun getRatingStars(): String = "★".repeat(rating.toInt()) + "☆".repeat(5 - rating.toInt())
    fun getFormattedDate(): String {
        // Format date string to readable format
        return createdAt // Should be formatted properly in real implementation
    }
}