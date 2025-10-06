package com.example.sbooks.activities.staff
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.adapter.InventoryAdapter
import com.example.sbooks.database.dao.BookDao
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.models.BookModel
import com.example.sbooks.models.SearchFilter
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.ImageUtils
import com.example.sbooks.utils.ValidationUtils

class StaffInventoryActivity : AppCompatActivity() {

    private lateinit var etSearchInventory: EditText
    private lateinit var spinnerStockFilter: Spinner
    private lateinit var tvTotalItems: TextView
    private lateinit var tvLowStockCount: TextView
    private lateinit var tvOutOfStockCount: TextView
    private lateinit var tvInventoryCount: TextView
    private lateinit var rvInventory: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout

    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var bookDao: BookDao

    private var inventoryList = mutableListOf<BookModel>()
    private var currentStockFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_staff_inventory)

        initializeViews()
        setupDatabase()
        setupRecyclerView()
        setupSpinner()
        setupSearchListener()
        loadInventory()
        loadStatistics()
    }

    private fun initializeViews() {
        etSearchInventory = findViewById(R.id.et_search_inventory)
        spinnerStockFilter = findViewById(R.id.spinner_stock_filter)
        tvTotalItems = findViewById(R.id.tv_total_items)
        tvLowStockCount = findViewById(R.id.tv_low_stock_count)
        tvOutOfStockCount = findViewById(R.id.tv_out_of_stock_count)
        tvInventoryCount = findViewById(R.id.tv_inventory_count)
        rvInventory = findViewById(R.id.rv_inventory)
        layoutEmptyState = findViewById(R.id.layout_empty_state)
    }

    private fun setupDatabase() {
        val dbHelper = DatabaseHelper(this)
        bookDao = BookDao(dbHelper.writableDatabase)
    }

    private fun setupRecyclerView() {
        inventoryAdapter = InventoryAdapter(
            onUpdateStockClick = { book -> showUpdateStockDialog(book) },
            onItemClick = { book -> showBookDetailsDialog(book) }
        )

        rvInventory.apply {
            layoutManager = LinearLayoutManager(this@StaffInventoryActivity)
            adapter = inventoryAdapter
        }
    }

    private fun setupSpinner() {
        val stockLevels = resources.getStringArray(R.array.stock_filter_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stockLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStockFilter.adapter = adapter

        spinnerStockFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentStockFilter = when (position) {
                    0 -> "all"
                    1 -> "available"
                    2 -> "low_stock"
                    3 -> "out_of_stock"
                    else -> "all"
                }
                filterInventory()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearchListener() {
        etSearchInventory.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterInventory()
            }
        })
    }

    private fun loadInventory() {
        try {
            inventoryList.clear()
            inventoryList.addAll(bookDao.getAllBooks())
            updateUI()
        } catch (e: Exception) {
            DialogUtils.showErrorDialog(this, "Lỗi khi tải danh sách kho: ${e.message}") {
                finish()
            }
        }
    }

    private fun loadStatistics() {
        try {
            val allBooks = bookDao.getAllBooks()
            val totalItems = allBooks.size
            val lowStockItems = allBooks.count { it.isLowStock() && !it.isOutOfStock() }
            val outOfStockItems = allBooks.count { it.isOutOfStock() }

            tvTotalItems.text = totalItems.toString()
            tvLowStockCount.text = lowStockItems.toString()
            tvOutOfStockCount.text = outOfStockItems.toString()
        } catch (e: Exception) {
            DialogUtils.showErrorDialog(this, "Lỗi khi tải thống kê: ${e.message}") {
                finish()
            }
        }
    }

    private fun filterInventory() {
        val query = etSearchInventory.text.toString().trim()

        try {
            var filteredList = if (query.isEmpty()) {
                inventoryList
            } else {
                val filter = SearchFilter(
                    query = query,
                    sortBy = SearchFilter.SortOption.NAME_ASC
                )
                bookDao.searchBooks(filter)
            }

            // Apply stock filter
            filteredList = when (currentStockFilter) {
                "available" -> filteredList.filter { it.stock > 10 }
                "low_stock" -> filteredList.filter { it.isLowStock() && !it.isOutOfStock() }
                "out_of_stock" -> filteredList.filter { it.isOutOfStock() }
                else -> filteredList
            }

            inventoryAdapter.submitList(filteredList)
            updateInventoryCount(filteredList.size)
            toggleEmptyState(filteredList.isEmpty())
        } catch (e: Exception) {
            DialogUtils.showErrorDialog(this, "Lỗi khi lọc kho: ${e.message}") {
                finish()
            }
        }
    }

    private fun updateUI() {
        inventoryAdapter.submitList(inventoryList)
        updateInventoryCount(inventoryList.size)
        toggleEmptyState(inventoryList.isEmpty())
    }

    private fun updateInventoryCount(count: Int) {
        tvInventoryCount.text = "$count mặt hàng"
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        rvInventory.visibility = if (isEmpty) View.GONE else View.VISIBLE
        layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    // Dialog methods
    private fun showUpdateStockDialog(book: BookModel) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_update_stock)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val ivBookPreview = dialog.findViewById<ImageView>(R.id.iv_book_preview)
        val tvBookTitleDialog = dialog.findViewById<TextView>(R.id.tv_book_title_dialog)
        val tvBookAuthorDialog = dialog.findViewById<TextView>(R.id.tv_book_author_dialog)
        val tvCurrentStock = dialog.findViewById<TextView>(R.id.tv_current_stock)
        val etNewStock = dialog.findViewById<EditText>(R.id.et_new_stock)
        val rgUpdateType = dialog.findViewById<RadioGroup>(R.id.rg_update_type)
        val rbSetQuantity = dialog.findViewById<RadioButton>(R.id.rb_set_quantity)
        val rbAddQuantity = dialog.findViewById<RadioButton>(R.id.rb_add_quantity)
        val rbSubtractQuantity = dialog.findViewById<RadioButton>(R.id.rb_subtract_quantity)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnUpdate = dialog.findViewById<Button>(R.id.btn_update)

        // Populate book info
        tvBookTitleDialog.text = book.title
        tvBookAuthorDialog.text = book.author
        tvCurrentStock.text = "Số lượng hiện tại: ${book.stock}"

        if (book.image.isNotEmpty()) {
            val bitmap = ImageUtils.loadImageFromInternalStorage(book.image)
            if (bitmap != null) {
                ivBookPreview.setImageBitmap(bitmap)
            } else {
                ivBookPreview.setImageResource(R.drawable.ic_book)
            }
        } else {
            ivBookPreview.setImageResource(R.drawable.ic_book)
        }

        // Set default values
        rbSetQuantity.isChecked = true
        etNewStock.setText(book.stock.toString())

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnUpdate.setOnClickListener {
            val newStockText = etNewStock.text.toString().trim()

            if (!ValidationUtils.isValidStock(newStockText)) {
                DialogUtils.showErrorDialog(this, "Số lượng không hợp lệ") {
                    finish()
                }
                return@setOnClickListener
            }

            val inputQuantity = newStockText.toInt()
            val finalStock = when (rgUpdateType.checkedRadioButtonId) {
                R.id.rb_set_quantity -> inputQuantity
                R.id.rb_add_quantity -> book.stock + inputQuantity
                R.id.rb_subtract_quantity -> maxOf(0, book.stock - inputQuantity)
                else -> inputQuantity
            }

            if (finalStock < 0) {
                DialogUtils.showErrorDialog(this, "Số lượng tồn kho không thể âm") {
                    finish()
                }
                return@setOnClickListener
            }

            updateBookStock(book, finalStock, dialog)
        }

        dialog.show()
    }

    private fun updateBookStock(book: BookModel, newStock: Int, dialog: Dialog) {
        try {
            val result = bookDao.updateBookStock(book.id, newStock)
            if (result > 0) {
                DialogUtils.showSuccessDialog(this, "Cập nhật tồn kho thành công")
                loadInventory()
                loadStatistics()
                dialog.dismiss()
            } else {
                DialogUtils.showErrorDialog(this, "Không thể cập nhật tồn kho") {
                    finish()
                }
            }
        } catch (e: Exception) {
            DialogUtils.showErrorDialog(this, "Lỗi khi cập nhật: ${e.message}") {
                finish()
            }
        }
    }

    private fun showBookDetailsDialog(book: BookModel) {
        val message = buildString {
            appendLine("Tên sách: ${book.title}")
            appendLine("Tác giả: ${book.author}")
            appendLine("Thể loại: ${book.categoryName}")
            appendLine("Giá: ${book.getFormattedPrice()}")
            appendLine("Tồn kho: ${book.stock}")
            appendLine("Trạng thái: ${book.getStockStatus().displayName}")
            appendLine("Đã bán: ${book.soldCount}")
            if (book.description.isNotEmpty()) {
                appendLine("Mô tả: ${book.description}")
            }
        }

        DialogUtils.showInfoDialog(this, "Chi tiết sách", message)
    }

    // Public methods for external access
    fun refreshInventory() {
        loadInventory()
        loadStatistics()
    }

    fun showLowStockItems() {
        spinnerStockFilter.setSelection(2) // Low stock filter
    }

    fun showOutOfStockItems() {
        spinnerStockFilter.setSelection(3) // Out of stock filter
    }
}