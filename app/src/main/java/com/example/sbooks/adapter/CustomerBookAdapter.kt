package com.example.sbooks.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.models.BookModel
import com.example.sbooks.utils.ImageUtils

class CustomerBookAdapter(
    private val onAddToCartClick: (BookModel) -> Unit,
    private val onBookClick: (BookModel) -> Unit
) : ListAdapter<BookModel, CustomerBookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book_inline, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivBookCover: ImageView = itemView.findViewById(R.id.iv_book_cover)
        private val tvBookTitle: TextView = itemView.findViewById(R.id.tv_book_title)
        private val tvBookAuthor: TextView = itemView.findViewById(R.id.tv_book_author)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
        private val tvReviewCount: TextView = itemView.findViewById(R.id.tv_review_count)
        private val tvOriginalPrice: TextView = itemView.findViewById(R.id.tv_original_price)
        private val tvCurrentPrice: TextView = itemView.findViewById(R.id.tv_current_price)
        private val btnAddToCart: Button = itemView.findViewById(R.id.btn_add_to_cart)

        fun bind(book: BookModel) {
            tvBookTitle.text = book.title
            tvBookAuthor.text = "Tác giả: ${book.author}"
            tvRating.text = String.format("%.1f", book.rating)
            tvReviewCount.text = "(${book.reviewCount} đánh giá)"

            // Price display with discount logic
            val originalPrice = book.price * 1.25 // Simulated original price (25% discount)
            if (book.price < originalPrice) {
                tvOriginalPrice.visibility = View.VISIBLE
                tvOriginalPrice.text = String.format("%,.0fđ", originalPrice)
                tvCurrentPrice.text = book.getFormattedPrice()
            } else {
                tvOriginalPrice.visibility = View.GONE
                tvCurrentPrice.text = book.getFormattedPrice()
            }

            // Load book image
            if (book.image.isNotEmpty()) {
                val bitmap = ImageUtils.loadImageFromInternalStorage(book.image)
                if (bitmap != null) {
                    ivBookCover.setImageBitmap(bitmap)
                } else {
                    ivBookCover.setImageResource(R.drawable.ic_book_24)
                }
            } else {
                ivBookCover.setImageResource(R.drawable.ic_book_24)
            }

            // Handle out of stock
            if (book.isOutOfStock()) {
                btnAddToCart.isEnabled = false
                btnAddToCart.text = "Hết hàng"
                btnAddToCart.alpha = 0.5f
            } else {
                btnAddToCart.isEnabled = true
                btnAddToCart.text = "Thêm vào giỏ"
                btnAddToCart.alpha = 1.0f
            }

            // Click listeners
            itemView.setOnClickListener { onBookClick(book) }
            btnAddToCart.setOnClickListener {
                if (!book.isOutOfStock()) {
                    onAddToCartClick(book)
                }
            }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<BookModel>() {
        override fun areItemsTheSame(oldItem: BookModel, newItem: BookModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BookModel, newItem: BookModel): Boolean {
            return oldItem == newItem
        }
    }
}