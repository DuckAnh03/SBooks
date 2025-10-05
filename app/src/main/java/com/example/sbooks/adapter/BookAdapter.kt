package com.example.sbooks.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.models.BookModel
import com.example.sbooks.utils.ImageUtils

class BookAdapter(
    private val onEditClick: (BookModel) -> Unit,
    private val onDeleteClick: (BookModel) -> Unit,
    private val onItemClick: (BookModel) -> Unit
) : ListAdapter<BookModel, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivBookImage: ImageView = itemView.findViewById(R.id.iv_book_image)
        private val tvBookTitle: TextView = itemView.findViewById(R.id.tv_book_title)
        private val tvBookAuthor: TextView = itemView.findViewById(R.id.tv_book_author)
        private val tvBookCategory: TextView = itemView.findViewById(R.id.tv_book_category)
        private val tvBookPrice: TextView = itemView.findViewById(R.id.tv_book_price)
        private val tvBookStock: TextView = itemView.findViewById(R.id.tv_book_stock)
        private val btnEditBook: ImageButton = itemView.findViewById(R.id.btn_edit_book)
        private val btnDeleteBook: ImageButton = itemView.findViewById(R.id.btn_delete_book)

        fun bind(book: BookModel) {
            tvBookTitle.text = book.title
            tvBookAuthor.text = book.author
            tvBookCategory.text = book.categoryName
            tvBookPrice.text = book.getFormattedPrice()
            tvBookStock.text = "SL: ${book.stock}"

            // Set stock color based on quantity
            when {
                book.isOutOfStock() -> {
                    tvBookStock.setTextColor(itemView.context.getColor(R.color.colorError))
                }
                book.isLowStock() -> {
                    tvBookStock.setTextColor(itemView.context.getColor(R.color.colorWarning))
                }
                else -> {
                    tvBookStock.setTextColor(itemView.context.getColor(R.color.colorSuccess))
                }
            }

            // Load book image from internal storage or show default
            if (book.image.isNotEmpty()) {
                val bitmap = ImageUtils.loadImageFromInternalStorage(book.image)
                if (bitmap != null) {
                    ivBookImage.setImageBitmap(bitmap)
                } else {
                    ivBookImage.setImageResource(R.drawable.ic_book)
                }
            } else {
                ivBookImage.setImageResource(R.drawable.ic_book)
            }

            // Click listeners
            itemView.setOnClickListener { onItemClick(book) }
            btnEditBook.setOnClickListener { onEditClick(book) }
            btnDeleteBook.setOnClickListener { onDeleteClick(book) }
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