package com.example.sbooks.models

data class ReviewModel(
    val id: Int = 0,
    val bookId: Int,
    val userId: Int,
    val userName: String,
    val userAvatar: String = "",
    val rating: Float,
    val comment: String = "",
    val isVerifiedPurchase: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    /**
     * Check if review has a comment
     */
    fun hasComment(): Boolean {
        return comment.isNotEmpty() && comment.isNotBlank()
    }

    /**
     * Get short comment (first 100 characters)
     */
    fun getShortComment(maxLength: Int = 100): String {
        return if (comment.length > maxLength) {
            comment.substring(0, maxLength) + "..."
        } else {
            comment
        }
    }

    /**
     * Get rating as integer
     */
    fun getRatingInt(): Int {
        return rating.toInt()
    }

    /**
     * Get user initials for avatar
     */
    fun getUserInitials(): String {
        return userName.firstOrNull()?.toString()?.uppercase() ?: "?"
    }

    /**
     * Check if this is a high rating (4-5 stars)
     */
    fun isHighRating(): Boolean {
        return rating >= 4.0f
    }

    /**
     * Check if this is a low rating (1-2 stars)
     */
    fun isLowRating(): Boolean {
        return rating <= 2.0f
    }
}