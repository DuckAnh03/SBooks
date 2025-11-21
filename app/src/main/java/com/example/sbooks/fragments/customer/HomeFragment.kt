package com.example.sbooks.fragments.customer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.adapter.CategoryAdapter
import com.example.sbooks.adapter.CategoryChipAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.BookDao
import com.example.sbooks.database.dao.CategoryDao
import com.example.sbooks.models.BookModel
import com.example.sbooks.models.CategoryModel
import com.example.sbooks.models.SearchFilter
import com.example.sbooks.utils.CartManager
import com.example.sbooks.utils.ImageUtils

class HomeFragment : Fragment() {
    private lateinit var bookDao: BookDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var txtResultCount: TextView
    private lateinit var booksContainer: LinearLayout

    private var currentCategoryId: Int? = null
    private var currentSortOption = SearchFilter.SortOption.DATE_DESC
    private var currentSearchQuery: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize database
        val dbHelper = DatabaseHelper(requireContext())
        bookDao = BookDao(dbHelper.writableDatabase)
        categoryDao = CategoryDao(dbHelper.writableDatabase)

        setupCategory(root)
        // Initialize views
        txtResultCount = root.findViewById(R.id.txtResultCount)
        booksContainer = root.findViewById(R.id.booksContainer)

        // Get search query from arguments if exists
        currentSearchQuery = arguments?.getString("search_query")

        // Setup filter and sort buttons
        setupFilterAndSort(root)

        // Load initial books
        loadBooks()

        return root
    }

    private fun setupCategory(root: View){
        val categoriesFromDb = categoryDao.getAllCategories()
        val allCategory = CategoryModel(
            id = -1,             // dùng -1 để nhận biết là "Tất cả"
            name = "Tất cả"
        )
        val categories = listOf(allCategory) + categoriesFromDb
        val adapter = CategoryChipAdapter(categories) { category ->
            // Khi click vào category
            currentCategoryId = category.id
            loadBooks()
        }
        // Đồng bộ highlight
        adapter.selectedPosition = if (currentCategoryId == null) {
            0 // Tất cả
        } else {
            categories.indexOfFirst { it.id == currentCategoryId }.takeIf { it >= 0 } ?: 0
        }
        val recyclerView = root.findViewById<RecyclerView>(R.id.categoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
    }

    private fun setupFilterAndSort(root: View) {
        val btnFilter = root.findViewById<View>(R.id.btnFilter)
        val btnSort = root.findViewById<View>(R.id.btnSort)

        btnFilter.setOnClickListener {
            Toast.makeText(requireContext(), "Tính năng lọc đang phát triển", Toast.LENGTH_SHORT).show()
        }

        btnSort.setOnClickListener {
            showSortMenu(it)
        }
    }

    private fun showSortMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.apply {
            add(0, 1, 0, "Tên A-Z")
            add(0, 2, 0, "Tên Z-A")
            add(0, 3, 0, "Giá tăng dần")
            add(0, 4, 0, "Giá giảm dần")
            add(0, 5, 0, "Mới nhất")
        }

        popup.setOnMenuItemClickListener { item ->
            currentSortOption = when (item.itemId) {
                1 -> SearchFilter.SortOption.NAME_ASC
                2 -> SearchFilter.SortOption.NAME_DESC
                3 -> SearchFilter.SortOption.PRICE_ASC
                4 -> SearchFilter.SortOption.PRICE_DESC
                5 -> SearchFilter.SortOption.DATE_DESC
                else -> SearchFilter.SortOption.DATE_DESC
            }
            loadBooks()
            true
        }
        popup.show()
    }

    private fun loadBooks() {
        val filter = SearchFilter(
            query = currentSearchQuery ?: "",
            categoryId = currentCategoryId,
            sortBy = currentSortOption
        )

        val books = bookDao.searchBooks(filter)
        displayBooks(books)

        // Update result count with search info
        val countText = if (currentSearchQuery != null) {
            "Tìm thấy ${books.size} cuốn sách cho \"$currentSearchQuery\""
        } else {
            "Hiển thị ${books.size} cuốn sách"
        }
        txtResultCount.text = countText
    }

    private fun displayBooks(books: List<BookModel>) {
        // Clear existing views
        booksContainer.removeAllViews()

        // Add each book as a CardView
        books.forEach { book ->
            val bookCard = createBookCardFromLayout(book)
            booksContainer.addView(bookCard)
        }
    }

    private fun createBookCardFromLayout(book: BookModel): CardView {
        // Inflate the card layout
        val cardView = layoutInflater.inflate(
            R.layout.item_book_inline,
            booksContainer,
            false
        ) as CardView

        // Find views
        val ivBookCover = cardView.findViewById<ImageView>(R.id.iv_book_cover)
        val tvBookTitle = cardView.findViewById<TextView>(R.id.tv_book_title)
        val tvBookAuthor = cardView.findViewById<TextView>(R.id.tv_book_author)
        val tvBookCategory = cardView.findViewById<TextView>(R.id.tv_category)
        val tvRating = cardView.findViewById<TextView>(R.id.tv_rating)
        val tvReviewCount = cardView.findViewById<TextView>(R.id.tv_review_count)
        val tvOriginalPrice = cardView.findViewById<TextView>(R.id.tv_original_price)
        val tvCurrentPrice = cardView.findViewById<TextView>(R.id.tv_current_price)
        val btnAddToCart = cardView.findViewById<Button>(R.id.btn_add_to_cart)

        // Bind data
        tvBookTitle.text = book.title
        tvBookAuthor.text = "Tác giả: ${book.author}"
        tvBookCategory.text = book.categoryName
        tvRating.text = String.format("%.1f", book.rating)
        tvReviewCount.text = "(${book.reviewCount} đánh giá)"

        // Price display with discount logic
        val originalPrice = book.price * 1.25
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
        cardView.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("book_id", book.id)
            }
            val detailFragment = BookDetailFragment().apply {
                arguments = bundle
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        btnAddToCart.setOnClickListener {
            if (!book.isOutOfStock()) {
                CartManager.addToCart(book)
                Toast.makeText(requireContext(), "Đã thêm '${book.title}' vào giỏ hàng", Toast.LENGTH_SHORT).show()
            }
        }

        return cardView
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
    companion object {
        private const val ARG_SEARCH_QUERY = "search_query"

        /**
         * Factory method để tạo HomeFragment và truyền từ khóa tìm kiếm.
         */
        @JvmStatic
        fun newInstance(searchQuery: String): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString(ARG_SEARCH_QUERY, searchQuery)
            fragment.arguments = args
            return fragment
        }
    }
}