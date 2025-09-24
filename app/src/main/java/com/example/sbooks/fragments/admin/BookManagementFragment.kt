package com.example.sbooks.fragments.admin

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.adapter.BookAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.BookDao
import com.example.sbooks.database.dao.CategoryDao
import com.example.sbooks.models.BookModel
import com.example.sbooks.models.CategoryModel
import com.example.sbooks.models.SearchFilter
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.ImageUtils
import com.example.sbooks.utils.ValidationUtils
import java.util.*

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

    // Image handling
    private var selectedImageUri: Uri? = null
    private var currentDialog: Dialog? = null
    private var currentBookImageView: ImageView? = null

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                currentBookImageView?.let { imageView ->
                    ImageUtils.loadSelectedImageToView(requireContext(), uri, imageView)
                }
            }
        }
    }

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

    // Add this property to store category data
    private var categoryList = listOf<CategoryModel>()

    private fun setupSpinners() {
        // Category spinner
        try {
            categoryList = categoryDao.getActiveCategories()
            val categoryNames = listOf("Tất cả thể loại") + categoryList.map { it.name }
            val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
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

            // Also reload category list to ensure spinner has latest data
            categoryList = categoryDao.getActiveCategories()
            setupCategorySpinner()

            Log.d(TAG, "Loaded ${bookList.size} books and ${categoryList.size} categories")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading books", e)
        }
    }

    private fun setupCategorySpinner() {
        try {
            val categoryNames = listOf("Tất cả thể loại") + categoryList.map { it.name }
            val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerBookCategory.adapter = categoryAdapter
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up category spinner", e)
        }
    }

    private fun filterBooks() {
        val query = etSearchBook.text.toString().trim()
        val selectedCategoryPosition = spinnerBookCategory.selectedItemPosition
        val selectedSort = spinnerSortBy.selectedItemPosition
        val lowStockOnly = cbLowStockOnly.isChecked

        try {
            // Get the actual category ID based on spinner position
            val selectedCategoryId = if (selectedCategoryPosition > 0 && selectedCategoryPosition <= categoryList.size) {
                categoryList[selectedCategoryPosition - 1].id // -1 because first item is "Tất cả thể loại"
            } else {
                null
            }

            val filter = SearchFilter(
                query = query,
                categoryId = selectedCategoryId, // Use actual category ID instead of position
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

            Log.d(TAG, "Filter applied - Position: $selectedCategoryPosition, CategoryID: $selectedCategoryId, Results: ${filteredList.size}")
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

    // PUBLIC METHOD - Called by AdminMainActivity FAB
    fun showAddBookDialog() {
        showBookDialog(null) // null means add new book
    }

    private fun showBookDialog(book: BookModel?) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_book)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        currentDialog = dialog

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val ivBookPreview = dialog.findViewById<ImageView>(R.id.iv_book_preview)
        val btnSelectImage = dialog.findViewById<Button>(R.id.btn_select_image)
        val etBookTitle = dialog.findViewById<EditText>(R.id.et_book_title)
        val etBookAuthor = dialog.findViewById<EditText>(R.id.et_book_author)
        val etBookPublisher = dialog.findViewById<EditText>(R.id.et_book_publisher)
        val etBookPrice = dialog.findViewById<EditText>(R.id.et_book_price)
        val etBookStock = dialog.findViewById<EditText>(R.id.et_book_stock)
        val etBookDescription = dialog.findViewById<EditText>(R.id.et_book_description)
        val spinnerCategory = dialog.findViewById<Spinner>(R.id.spinner_book_category_dialog)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialog.findViewById<Button>(R.id.btn_save)

        currentBookImageView = ivBookPreview
        selectedImageUri = null

        // Setup category spinner
        val categories = categoryDao.getActiveCategories()
        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Configure for Add or Edit
        if (book == null) {
            // Add new book
            tvDialogTitle.text = "Thêm sách mới"
            btnSave.text = "Thêm"
        } else {
            // Edit existing book
            tvDialogTitle.text = "Sửa sách"
            btnSave.text = "Cập nhật"

            // Fill existing data
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

            // Load existing image if available
            if (book.image.isNotEmpty()) {
                val bitmap = ImageUtils.loadImageFromInternalStorage(book.image)
                if (bitmap != null) {
                    ivBookPreview.setImageBitmap(bitmap)
                }
            }
        }

        // Image selection
        btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        btnCancel.setOnClickListener {
            currentDialog = null
            currentBookImageView = null
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val title = etBookTitle.text.toString().trim()
            val author = etBookAuthor.text.toString().trim()
            val publisher = etBookPublisher.text.toString().trim()
            val priceStr = etBookPrice.text.toString().trim()
            val stockStr = etBookStock.text.toString().trim()
            val description = etBookDescription.text.toString().trim()

            val validation = ValidationUtils.validateBookInput(title, author, priceStr, stockStr, 1) // Temporary validation

            if (!validation.isValid) {
                DialogUtils.showErrorDialog(requireContext(), validation.errors.joinToString("\n")) {}
                return@setOnClickListener
            }

            if (categories.isEmpty() || spinnerCategory.selectedItemPosition < 0) {
                DialogUtils.showErrorDialog(requireContext(), "Vui lòng chọn thể loại sách") {}
                return@setOnClickListener
            }

            val selectedCategory = categories[spinnerCategory.selectedItemPosition]

            // Handle image saving
            var imagePath = book?.image ?: ""
            selectedImageUri?.let { uri ->
                try {
                    val compressedBitmap = ImageUtils.compressImage(requireContext(), uri)
                    compressedBitmap?.let { bitmap ->
                        val filename = ImageUtils.generateUniqueFileName()
                        val savedPath = ImageUtils.saveImageToInternalStorage(requireContext(), bitmap, filename)
                        if (savedPath != null) {
                            imagePath = savedPath
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving image", e)
                }
            }

            try {
                if (book == null) {
                    // Add new book
                    val newBook = BookModel(
                        title = title,
                        author = author,
                        publisher = publisher,
                        categoryId = selectedCategory.id,
                        categoryName = selectedCategory.name,
                        price = priceStr.toDouble(),
                        stock = stockStr.toInt(),
                        description = description,
                        image = imagePath,
                        status = BookModel.BookStatus.ACTIVE
                    )

                    val result = bookDao.insertBook(newBook)
                    if (result > 0) {
                        DialogUtils.showToast(requireContext(), "Thêm sách thành công")
                        loadBooks()
                        dialog.dismiss()
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), "Không thể thêm sách") {}
                    }
                } else {
                    // Update existing book
                    val updatedBook = book.copy(
                        title = title,
                        author = author,
                        publisher = publisher,
                        categoryId = selectedCategory.id,
                        categoryName = selectedCategory.name,
                        price = priceStr.toDouble(),
                        stock = stockStr.toInt(),
                        description = description,
                        image = imagePath
                    )

                    val result = bookDao.updateBook(updatedBook)
                    if (result > 0) {
                        DialogUtils.showToast(requireContext(), "Cập nhật sách thành công")
                        loadBooks()
                        dialog.dismiss()
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), "Không thể cập nhật sách") {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving book", e)
                DialogUtils.showErrorDialog(requireContext(), "Lỗi khi lưu: ${e.message}") {}
            }
        }

        dialog.setOnDismissListener {
            currentDialog = null
            currentBookImageView = null
        }

        dialog.show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun showEditBookDialog(book: BookModel) {
        showBookDialog(book)
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