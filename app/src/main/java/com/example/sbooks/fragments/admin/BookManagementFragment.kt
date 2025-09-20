package com.example.sbooks.fragments.admin

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.adapter.BookAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.BookDao
import com.example.sbooks.database.dao.CategoryDao
import com.example.sbooks.models.BookModel
import com.example.sbooks.models.SearchFilter
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.ValidationUtils

class BookManagementFragment : Fragment() {

    private companion object {
        private const val TAG = "BookManagementFragment"
    }

    // Views
    private lateinit var etSearchBook: EditText
    private lateinit var spinnerBookCategory: Spinner
    private lateinit var spinnerSortBy: Spinner
    private lateinit var cbLowStockOnly: CheckBox
    private lateinit var rvBooks: RecyclerView
    private lateinit var tvBookCount: TextView
    private lateinit var layoutEmptyState: LinearLayout

    // Data
    private lateinit var bookAdapter: BookAdapter
    private lateinit var bookDao: BookDao
    private lateinit var categoryDao: CategoryDao
    private var bookList = mutableListOf<BookModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_book_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initializeViews(view)
            setupDatabase()
            setupRecyclerView()
            setupSpinners()
            setupSearchListener()
            loadBooks()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
        }
    }

    private fun initializeViews(view: View) {
        etSearchBook = view.findViewById(R.id.et_search_book)
        spinnerBookCategory = view.findViewById(R.id.spinner_book_category)
        spinnerSortBy = view.findViewById(R.id.spinner_sort_by)
        cbLowStockOnly = view.findViewById(R.id.cb_low_stock_only)
        rvBooks = view.findViewById(R.id.rv_books)
        tvBookCount = view.findViewById(R.id.tv_book_count)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)
    }

    private fun setupDatabase() {
        val dbHelper = DatabaseHelper(requireContext())
        bookDao = BookDao(dbHelper.writableDatabase)
        categoryDao = CategoryDao(dbHelper.writableDatabase)
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(
            onEditClick = { book -> showEditBookDialog(book) },
            onDeleteClick = { book -> showDeleteBookDialog(book) },
            onItemClick = { book -> showBookDetailsDialog(book) }
        )

        rvBooks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookAdapter
        }
    }

    private fun setupSpinners() {
        // Category spinner
        try {
            val categories = listOf("Tất cả thể loại") + categoryDao.getActiveCategories().map { it.name }
            val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerBookCategory.adapter = categoryAdapter
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up category spinner", e)
        }

        // Sort spinner
        val sortOptions = arrayOf("Tên A-Z", "Tên Z-A", "Giá thấp đến cao", "Giá cao đến thấp", "Tồn kho thấp", "Mới nhất")
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSortBy.adapter = sortAdapter

        // Setup listeners
        spinnerBookCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterBooks()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerSortBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterBooks()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        cbLowStockOnly.setOnCheckedChangeListener { _, _ -> filterBooks() }
    }

    private fun setupSearchListener() {
        etSearchBook.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterBooks()
            }
        })
    }

    private fun loadBooks() {
        try {
            bookList.clear()
            bookList.addAll(bookDao.getAllBooks())
            updateUI()
            Log.d(TAG, "Loaded ${bookList.size} books")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading books", e)
        }
    }

    private fun filterBooks() {
        val query = etSearchBook.text.toString().trim()
        val selectedCategory = spinnerBookCategory.selectedItemPosition
        val selectedSort = spinnerSortBy.selectedItemPosition
        val lowStockOnly = cbLowStockOnly.isChecked

        try {
            val filter = SearchFilter(
                query = query,
                categoryId = if (selectedCategory > 0) selectedCategory else null,
                sortBy = when (selectedSort) {
                    0 -> SearchFilter.SortOption.NAME_ASC
                    1 -> SearchFilter.SortOption.NAME_DESC
                    2 -> SearchFilter.SortOption.PRICE_ASC
                    3 -> SearchFilter.SortOption.PRICE_DESC
                    4 -> SearchFilter.SortOption.STOCK_ASC
                    5 -> SearchFilter.SortOption.DATE_DESC
                    else -> SearchFilter.SortOption.NAME_ASC
                },
                showLowStockOnly = lowStockOnly
            )

            val filteredList = bookDao.searchBooks(filter)
            bookAdapter.submitList(filteredList)
            updateBookCount(filteredList.size)
            toggleEmptyState(filteredList.isEmpty())
        } catch (e: Exception) {
            Log.e(TAG, "Error filtering books", e)
        }
    }

    private fun updateUI() {
        bookAdapter.submitList(bookList)
        updateBookCount(bookList.size)
        toggleEmptyState(bookList.isEmpty())
    }

    private fun updateBookCount(count: Int) {
        tvBookCount.text = "$count cuốn sách"
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        rvBooks.visibility = if (isEmpty) View.GONE else View.VISIBLE
        layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun showEditBookDialog(book: BookModel) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_book)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etBookTitle = dialog.findViewById<EditText>(R.id.et_book_title)
        val etBookAuthor = dialog.findViewById<EditText>(R.id.et_book_author)
        val etBookPublisher = dialog.findViewById<EditText>(R.id.et_book_publisher)
        val etBookPrice = dialog.findViewById<EditText>(R.id.et_book_price)
        val etBookStock = dialog.findViewById<EditText>(R.id.et_book_stock)
        val etBookDescription = dialog.findViewById<EditText>(R.id.et_book_description)
        val spinnerCategory = dialog.findViewById<Spinner>(R.id.spinner_book_category_dialog)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialog.findViewById<Button>(R.id.btn_save)

        // Setup category spinner
        val categories = categoryDao.getActiveCategories()
        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Fill existing data
        tvDialogTitle.text = "Sửa sách"
        etBookTitle.setText(book.title)
        etBookAuthor.setText(book.author)
        etBookPublisher.setText(book.publisher)
        etBookPrice.setText(book.price.toString())
        etBookStock.setText(book.stock.toString())
        etBookDescription.setText(book.description)

        // Set selected category
        val categoryIndex = categories.indexOfFirst { it.id == book.categoryId }
        if (categoryIndex >= 0) {
            spinnerCategory.setSelection(categoryIndex)
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val title = etBookTitle.text.toString().trim()
            val author = etBookAuthor.text.toString().trim()
            val publisher = etBookPublisher.text.toString().trim()
            val priceStr = etBookPrice.text.toString().trim()
            val stockStr = etBookStock.text.toString().trim()
            val description = etBookDescription.text.toString().trim()

            val validation = ValidationUtils.validateBookInput(title, author, priceStr, stockStr, 1)

            if (!validation.isValid) {
                DialogUtils.showErrorDialog(requireContext(), validation.errors.joinToString("\n")) {}
                return@setOnClickListener
            }

            val selectedCategory = if (categories.isNotEmpty() && spinnerCategory.selectedItemPosition >= 0) {
                categories[spinnerCategory.selectedItemPosition]
            } else null

            val updatedBook = book.copy(
                title = title,
                author = author,
                publisher = publisher,
                categoryId = selectedCategory?.id ?: book.categoryId,
                categoryName = selectedCategory?.name ?: book.categoryName,
                price = priceStr.toDouble(),
                stock = stockStr.toInt(),
                description = description
            )

            try {
                val result = bookDao.updateBook(updatedBook)
                if (result > 0) {
                    DialogUtils.showToast(requireContext(), "Cập nhật sách thành công")
                    loadBooks()
                    dialog.dismiss()
                } else {
                    DialogUtils.showErrorDialog(requireContext(), "Không thể cập nhật sách") {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating book", e)
                DialogUtils.showErrorDialog(requireContext(), "Lỗi khi cập nhật: ${e.message}") {}
            }
        }

        dialog.show()
    }

    private fun showDeleteBookDialog(book: BookModel) {
        DialogUtils.showConfirmDialog(
            requireContext(),
            "Xác nhận xóa",
            "Bạn có chắc chắn muốn xóa sách ${book.title}?",
            positiveAction = {
                try {
                    val result = bookDao.deleteBook(book.id)
                    if (result > 0) {
                        DialogUtils.showToast(requireContext(), "Xóa sách thành công")
                        loadBooks()
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), "Không thể xóa sách") {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting book", e)
                    DialogUtils.showErrorDialog(requireContext(), "Lỗi khi xóa: ${e.message}") {}
                }
            }
        )
    }

    private fun showBookDetailsDialog(book: BookModel) {
        val message = buildString {
            appendLine("Tên sách: ${book.title}")
            appendLine("Tác giả: ${book.author}")
            appendLine("Nhà xuất bản: ${book.publisher}")
            appendLine("Thể loại: ${book.categoryName}")
            appendLine("Giá: ${book.getFormattedPrice()}")
            appendLine("Tồn kho: ${book.stock}")
            appendLine("Đã bán: ${book.soldCount}")
            appendLine("Đánh giá: ${book.rating}/5 (${book.reviewCount} đánh giá)")
            if (book.description.isNotEmpty()) {
                appendLine("Mô tả: ${book.description}")
            }
        }

        DialogUtils.showInfoDialog(requireContext(), "Chi tiết sách", message)
    }

    override fun onResume() {
        super.onResume()
        loadBooks()
    }
}