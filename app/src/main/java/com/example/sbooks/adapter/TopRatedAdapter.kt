package com.example.sbooks.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.models.TopRatedBookModel
import com.example.sbooks.utils.ImageUtils

class TopRatedAdapter(
    private val onItemClick: (TopRatedBookModel) -> Unit
) : ListAdapter<TopRatedBookModel, TopRatedAdapter.TopRatedViewHolder>(TopRatedDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopRatedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_top_rated_book, parent, false)
        return TopRatedViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopRatedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TopRatedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
        private val ivBookImage: ImageView = itemView.findViewById(R.id.iv_book_image)
        private val tvBookTitle: TextView = itemView.findViewById(R.id.tv_book_title)
        private val tvBookAuthor: TextView = itemView.findViewById(R.id.tv_book_author)
        private val tvRatingStars: TextView = itemView.findViewById(R.id.tv_rating_stars)
        private val tvReviewCount: TextView = itemView.findViewById(R.id.tv_review_count)
        private val tvBookPrice: TextView = itemView.findViewById(R.id.tv_book_price)
        private val tvStock: TextView = itemView.findViewById(R.id.tv_stock)

        fun bind(book: TopRatedBookModel) {
            tvRating.text = String.format("%.1f", book.rating)
            tvBookTitle.text = book.title
            tvBookAuthor.text = book.author
            tvRatingStars.text = book.getRatingStars()
            tvReviewCount.text = book.getReviewText()
            tvBookPrice.text = book.getFormattedPrice()
            tvStock.text = book.getStockText()

            // Set stock color
            if (book.stock > 0) {
                tvStock.setTextColor(itemView.context.getColor(R.color.colorSuccess))
            } else {
                tvStock.setTextColor(itemView.context.getColor(R.color.colorError))
            }

            // Set book image (placeholder for now)
            if (book.image.isNotEmpty()) {
                val bitmap = ImageUtils.loadImageFromInternalStorage(book.image)
                if (bitmap != null) {
                    ivBookImage.setImageBitmap(bitmap)
                }
            }

            // Click listener
            itemView.setOnClickListener { onItemClick(book) }
        }
    }

    class TopRatedDiffCallback : DiffUtil.ItemCallback<TopRatedBookModel>() {
        override fun areItemsTheSame(oldItem: TopRatedBookModel, newItem: TopRatedBookModel): Boolean {
            return oldItem.bookId == newItem.bookId
        }

        override fun areContentsTheSame(oldItem: TopRatedBookModel, newItem: TopRatedBookModel): Boolean {
            return oldItem == newItem
        }
    }
}