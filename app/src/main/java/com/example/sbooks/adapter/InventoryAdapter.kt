package com.example.sbooks.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.models.BookModel
class InventoryAdapter(
    private val onUpdateStockClick: (BookModel) -> Unit,
    private val onItemClick: (BookModel) -> Unit
) : ListAdapter<BookModel, InventoryAdapter.InventoryViewHolder>(BookAdapter.BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivBookImage: ImageView = itemView.findViewById(R.id.iv_book_image)
        private val tvBookTitle: TextView = itemView.findViewById(R.id.tv_book_title)
        private val tvBookAuthor: TextView = itemView.findViewById(R.id.tv_book_author)
        private val tvBookPrice: TextView = itemView.findViewById(R.id.tv_book_price)
        private val tvStockStatus: TextView = itemView.findViewById(R.id.tv_stock_status)
        private val tvStockQuantity: TextView = itemView.findViewById(R.id.tv_stock_quantity)
        private val btnUpdateStock: TextView = itemView.findViewById(R.id.btn_update_stock)

        fun bind(book: BookModel) {
            tvBookTitle.text = book.title
            tvBookAuthor.text = book.author
            tvBookPrice.text = book.getFormattedPrice()
            tvStockQuantity.text = book.stock.toString()

            // Set stock status
            val stockLevel = book.getStockStatus()
            tvStockStatus.text = stockLevel.displayName

            when (stockLevel) {
                BookModel.StockLevel.OUT_OF_STOCK -> {
                    tvStockStatus.setBackgroundResource(R.drawable.bg_stock_out)
                    tvStockQuantity.setTextColor(itemView.context.getColor(R.color.colorError))
                }
                BookModel.StockLevel.LOW_STOCK -> {
                    tvStockStatus.setBackgroundResource(R.drawable.bg_stock_low)
                    tvStockQuantity.setTextColor(itemView.context.getColor(R.color.colorWarning))
                }
                BookModel.StockLevel.MEDIUM_STOCK -> {
                    tvStockStatus.setBackgroundResource(R.drawable.bg_stock_available)
                    tvStockQuantity.setTextColor(itemView.context.getColor(R.color.colorSuccess))
                }
                BookModel.StockLevel.HIGH_STOCK -> {
                    tvStockStatus.setBackgroundResource(R.drawable.bg_stock_available)
                    tvStockQuantity.setTextColor(itemView.context.getColor(R.color.colorSuccess))
                }
            }

            // Set book image (placeholder for now)
            ivBookImage.setImageResource(R.drawable.ic_book)

            // Click listeners
            itemView.setOnClickListener { onItemClick(book) }
            btnUpdateStock.setOnClickListener { onUpdateStockClick(book) }
        }
    }
}