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
import com.example.sbooks.models.BestSellerBookModel
class BestSellerAdapter(
    private val onItemClick: (BestSellerBookModel) -> Unit
) : ListAdapter<BestSellerBookModel, BestSellerAdapter.BestSellerViewHolder>(BestSellerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestSellerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bestseller_book, parent, false)
        return BestSellerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BestSellerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BestSellerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRank: TextView = itemView.findViewById(R.id.tv_rank)
        private val ivBookImage: ImageView = itemView.findViewById(R.id.iv_book_image)
        private val tvBookTitle: TextView = itemView.findViewById(R.id.tv_book_title)
        private val tvBookAuthor: TextView = itemView.findViewById(R.id.tv_book_author)
        private val tvSoldCount: TextView = itemView.findViewById(R.id.tv_sold_count)
        private val tvBookPrice: TextView = itemView.findViewById(R.id.tv_book_price)
        private val tvRevenue: TextView = itemView.findViewById(R.id.tv_revenue)

        fun bind(book: BestSellerBookModel) {
            tvRank.text = book.rank.toString()
            tvBookTitle.text = book.title
            tvBookAuthor.text = book.author
            tvSoldCount.text = book.soldQuantity.toString()
            tvBookPrice.text = book.getFormattedPrice()
            tvRevenue.text = "${book.getFormattedRevenue().replace(" VNĐ", "")}M VNĐ"

            // Set rank badge color based on position
            when (book.rank) {
                1 -> tvRank.setBackgroundResource(R.drawable.bg_rank_gold)
                2 -> tvRank.setBackgroundResource(R.drawable.bg_rank_silver)
                3 -> tvRank.setBackgroundResource(R.drawable.bg_rank_bronze)
                else -> tvRank.setBackgroundResource(R.drawable.bg_rank_badge)
            }

            // Set book image (placeholder for now)
            ivBookImage.setImageResource(R.drawable.ic_book)

            // Click listener
            itemView.setOnClickListener { onItemClick(book) }
        }
    }

    class BestSellerDiffCallback : DiffUtil.ItemCallback<BestSellerBookModel>() {
        override fun areItemsTheSame(oldItem: BestSellerBookModel, newItem: BestSellerBookModel): Boolean {
            return oldItem.bookId == newItem.bookId
        }

        override fun areContentsTheSame(oldItem: BestSellerBookModel, newItem: BestSellerBookModel): Boolean {
            return oldItem == newItem
        }
    }
}