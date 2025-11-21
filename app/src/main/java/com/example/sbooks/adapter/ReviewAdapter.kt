package com.example.sbooks.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.models.ReviewModel
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(
    private var reviews: List<ReviewModel> = emptyList()
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserAvatar: TextView = itemView.findViewById(R.id.tvUserAvatar)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvReviewDate: TextView = itemView.findViewById(R.id.tvReviewDate)
        val layoutVerifiedBadge: LinearLayout = itemView.findViewById(R.id.layoutVerifiedBadge)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val tvComment: TextView = itemView.findViewById(R.id.tvComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        // User avatar (first letter of name)
        holder.tvUserAvatar.text = review.getUserInitials()

        // User name
        holder.tvUserName.text = review.userName

        // Review date
        holder.tvReviewDate.text = formatDate(review.createdAt)

        // Verified purchase badge
        holder.layoutVerifiedBadge.visibility = if (review.isVerifiedPurchase) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Rating - use floored value for display
        holder.ratingBar.rating = Math.floor(review.rating.toDouble()).toFloat()

        // Comment - Show only if exists
        if (review.hasComment()) {
            holder.tvComment.visibility = View.VISIBLE
            holder.tvComment.text = review.comment
        } else {
            holder.tvComment.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = reviews.size

    /**
     * Update reviews list efficiently using DiffUtil
     */
    fun updateReviews(newReviews: List<ReviewModel>) {
        val diffCallback = ReviewDiffCallback(reviews, newReviews)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        reviews = newReviews
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Simple update without DiffUtil (use for small lists)
     */
    fun updateReviewsSimple(newReviews: List<ReviewModel>) {
        reviews = newReviews
        notifyDataSetChanged()
    }

    /**
     * Format date string to display format
     */
    private fun formatDate(dateString: String): String {
        if (dateString.isEmpty()) {
            return "Vừa xong"
        }

        try {
            // Try with time
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(dateString)

            if (date != null) {
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                return outputFormat.format(date)
            }
        } catch (e: Exception) {
            // Try without time
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputFormat.parse(dateString)

                if (date != null) {
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    return outputFormat.format(date)
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }

        return dateString
    }

    /**
     * DiffUtil Callback for efficient list updates
     */
    private class ReviewDiffCallback(
        private val oldList: List<ReviewModel>,
        private val newList: List<ReviewModel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldReview = oldList[oldItemPosition]
            val newReview = newList[newItemPosition]

            return oldReview.rating == newReview.rating &&
                    oldReview.comment == newReview.comment &&
                    oldReview.updatedAt == newReview.updatedAt
        }
    }
}