package com.example.sbooks.fragments.admin

import android.app.AlertDialog
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
import com.example.sbooks.adapter.CategoryAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.CategoryDao
import com.example.sbooks.models.CategoryModel
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.ValidationUtils

class CategoryManagementFragment : Fragment() {

    private companion object {
        private const val TAG = "CategoryManagementFragment"
    }

    // Views
    private lateinit var etSearchCategory: EditText
    private lateinit var spinnerCategoryStatus: Spinner
    private lateinit var tvTotalCategories: TextView
    private lateinit var tvActiveCategories: TextView
    private lateinit var rvCategories: RecyclerView
    private lateinit var tvCategoryCount: TextView
    private lateinit var layoutEmptyState: LinearLayout

    // Data
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryDao: CategoryDao
    private var categoryList = mutableListOf<CategoryModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_category_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initializeViews(view)
            setupDatabase()
            setupRecyclerView()
            setupSpinner()
            setupSearchListener()
            loadCategories()
            loadStatistics()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
        }
    }

    private fun initializeViews(view: View) {
        etSearchCategory = view.findViewById(R.id.et_search_category)
        spinnerCategoryStatus = view.findViewById(R.id.spinner_category_status)
        tvTotalCategories = view.findViewById(R.id.tv_total_categories)
        tvActiveCategories = view.findViewById(R.id.tv_active_categories)
        rvCategories = view.findViewById(R.id.rv_categories)
        tvCategoryCount = view.findViewById(R.id.tv_category_count)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)
    }

    private fun setupDatabase() {
        val dbHelper = DatabaseHelper(requireContext())
        categoryDao = CategoryDao(dbHelper.writableDatabase)
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            onEditClick = { category -> showEditCategoryDialog(category) },
            onDeleteClick = { category -> showDeleteCategoryDialog(category) },
            onItemClick = { category -> showCategoryDetailsDialog(category) }
        )

        rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    private fun setupSpinner() {
        val statuses = arrayOf("Tất cả trạng thái", "Đang hoạt động", "Không hoạt động")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoryStatus.adapter = adapter

        spinnerCategoryStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterCategories()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearchListener() {
        etSearchCategory.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterCategories()
            }
        })
    }

    private fun loadCategories() {
        try {
            categoryList.clear()
            categoryList.addAll(categoryDao.getAllCategories())
            updateUI()
            Log.d(TAG, "Loaded ${categoryList.size} categories")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading categories", e)
        }
    }

    private fun loadStatistics() {
        try {
            val allCategories = categoryDao.getAllCategories()
            val activeCategories = categoryDao.getActiveCategories()

            tvTotalCategories.text = allCategories.size.toString()
            tvActiveCategories.text = activeCategories.size.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading statistics", e)
        }
    }

    private fun filterCategories() {
        val query = etSearchCategory.text.toString().trim()
        val selectedStatus = spinnerCategoryStatus.selectedItemPosition

        try {
            var filteredList = if (query.isEmpty()) {
                categoryList.toList() // Create a copy to avoid reference issues
            } else {
                categoryDao.searchCategories(query)
            }

            // Apply status filter
            if (selectedStatus != 0) {
                val status = if (selectedStatus == 1) CategoryModel.CategoryStatus.ACTIVE else CategoryModel.CategoryStatus.INACTIVE
                filteredList = filteredList.filter { it.status == status }
            }

            categoryAdapter.submitList(filteredList.toList()) // Pass a new list to trigger update
            updateCategoryCount(filteredList.size)
            toggleEmptyState(filteredList.isEmpty())
        } catch (e: Exception) {
            Log.e(TAG, "Error filtering categories", e)
        }
    }

    private fun updateUI() {
        categoryAdapter.submitList(categoryList.toList()) // Pass a copy to force update
        updateCategoryCount(categoryList.size)
        toggleEmptyState(categoryList.isEmpty())
        filterCategories() // Reapply current filters
    }

    private fun updateCategoryCount(count: Int) {
        tvCategoryCount.text = "$count danh mục"
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        rvCategories.visibility = if (isEmpty) View.GONE else View.VISIBLE
        layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    // PUBLIC METHOD - Called by AdminMainActivity FAB
    fun showAddCategoryDialog() {
        showCategoryDialog(null) // null means add new category
    }

    private fun showCategoryDialog(category: CategoryModel?) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_category)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etCategoryName = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_category_name)
        val etCategoryDescription = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_category_description)
        val ivCategoryIconPreview = dialog.findViewById<ImageView>(R.id.iv_category_icon_preview)
        val btnSelectIcon = dialog.findViewById<Button>(R.id.btn_select_icon)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialog.findViewById<Button>(R.id.btn_save)

        var selectedIconResource = R.drawable.ic_category

        // Configure for Add or Edit
        if (category == null) {
            // Add new category
            tvDialogTitle.text = "Thêm danh mục mới"
            btnSave.text = "Thêm"
        } else {
            // Edit existing category
            tvDialogTitle.text = "Sửa danh mục"
            btnSave.text = "Cập nhật"

            // Fill existing data
            etCategoryName.setText(category.name)
            etCategoryDescription.setText(category.description)

            // Set existing icon
            if (category.icon.isNotEmpty()) {
                try {
                    selectedIconResource = category.icon.toIntOrNull() ?: R.drawable.ic_category
                    ivCategoryIconPreview.setImageResource(selectedIconResource)
                } catch (e: Exception) {
                    selectedIconResource = R.drawable.ic_category
                    ivCategoryIconPreview.setImageResource(selectedIconResource)
                }
            }
        }

        // Predefined icons with visual representations
        val iconOptions = arrayOf(
            IconOption("Thể loại chung", R.drawable.ic_category),
            IconOption("Sách", R.drawable.ic_book),
            IconOption("Khoa học", R.drawable.ic_science),
            IconOption("Văn học", R.drawable.ic_book),
            IconOption("Nghệ thuật", R.drawable.ic_category),
            IconOption("Lịch sử", R.drawable.ic_history),
            IconOption("Kinh tế", R.drawable.ic_economy),
            IconOption("Manga", R.drawable.ic_manga),
            IconOption("Tâm lý", R.drawable.ic_psychology_book)
        )

        btnSelectIcon.setOnClickListener {
            showIconSelectionDialog(iconOptions) { selectedIcon ->
                selectedIconResource = selectedIcon.resourceId
                ivCategoryIconPreview.setImageResource(selectedIconResource)
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = etCategoryName.text.toString().trim()
            val description = etCategoryDescription.text.toString().trim()

            val validation = ValidationUtils.validateCategoryInput(name, description)

            if (!validation.isValid) {
                DialogUtils.showErrorDialog(requireContext(), validation.errors.joinToString("\n")) {}
                return@setOnClickListener
            }

            try {
                if (category == null) {
                    // Add new category
                    val newCategory = CategoryModel(
                        name = name,
                        description = description,
                        icon = selectedIconResource.toString(),
                        status = CategoryModel.CategoryStatus.ACTIVE,
                        sortOrder = categoryList.size
                    )

                    val result = categoryDao.insertCategory(newCategory)
                    if (result > 0) {
                        DialogUtils.showToast(requireContext(), "Thêm danh mục thành công")
                        // Force refresh data
                        refreshData()
                        dialog.dismiss()
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), "Không thể thêm danh mục") {}
                    }
                } else {
                    // Update existing category
                    val updatedCategory = category.copy(
                        name = name,
                        description = description,
                        icon = selectedIconResource.toString()
                    )

                    val result = categoryDao.updateCategory(updatedCategory)
                    if (result > 0) {
                        DialogUtils.showToast(requireContext(), "Cập nhật danh mục thành công")
                        // Force refresh data
                        refreshData()
                        dialog.dismiss()
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), "Không thể cập nhật danh mục") {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving category", e)
                DialogUtils.showErrorDialog(requireContext(), "Lỗi khi lưu: ${e.message}") {}
            }
        }

        dialog.show()
    }

    private data class IconOption(val name: String, val resourceId: Int)

    private fun showIconSelectionDialog(
        iconOptions: Array<IconOption>,
        onIconSelected: (IconOption) -> Unit
    ) {
        // Create a simple grid dialog with ImageViews
        val scrollView = ScrollView(requireContext())
        val linearLayout = LinearLayout(requireContext())
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setPadding(32, 32, 32, 32)

        // Title
        val titleView = TextView(requireContext())
        titleView.text = "Chọn biểu tượng cho danh mục"
        titleView.textSize = 16f
        titleView.setTypeface(null, android.graphics.Typeface.BOLD)
        titleView.gravity = android.view.Gravity.CENTER
        titleView.setPadding(0, 0, 0, 32)
        linearLayout.addView(titleView)

        // Create grid-like layout using horizontal LinearLayouts
        val iconsPerRow = 3
        var currentRow: LinearLayout? = null

        iconOptions.forEachIndexed { index, iconOption ->
            if (index % iconsPerRow == 0) {
                currentRow = LinearLayout(requireContext())
                currentRow!!.orientation = LinearLayout.HORIZONTAL
                currentRow!!.gravity = android.view.Gravity.CENTER
                linearLayout.addView(currentRow)
            }

            // Create icon container
            val iconContainer = LinearLayout(requireContext())
            iconContainer.orientation = LinearLayout.VERTICAL
            iconContainer.gravity = android.view.Gravity.CENTER
            iconContainer.setPadding(16, 16, 16, 16)
            iconContainer.isClickable = true
            iconContainer.isFocusable = true

            // Add click effect
            val attrs = intArrayOf(android.R.attr.selectableItemBackground)
            val typedArray = requireContext().obtainStyledAttributes(attrs)
            val selectableBackground = typedArray.getDrawable(0)
            typedArray.recycle()
            iconContainer.background = selectableBackground

            // Create ImageView
            val imageView = ImageView(requireContext())
            val imageSize = (48 * resources.displayMetrics.density).toInt()
            val imageParams = LinearLayout.LayoutParams(imageSize, imageSize)
            imageView.layoutParams = imageParams
            imageView.setImageResource(iconOption.resourceId)
            imageView.setPadding(8, 8, 8, 8)
            imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

            // Add circular background
            try {
                imageView.setBackgroundResource(R.drawable.bg_circle)
            } catch (e: Exception) {
                // If bg_circle doesn't exist, create a simple background
                val shape = android.graphics.drawable.GradientDrawable()
                shape.shape = android.graphics.drawable.GradientDrawable.OVAL
                shape.setColor(0xFFE0E0E0.toInt())
                imageView.background = shape
            }

            // Create TextView
            val textView = TextView(requireContext())
            textView.text = iconOption.name
            textView.textSize = 12f
            textView.gravity = android.view.Gravity.CENTER
            textView.maxLines = 2
            textView.setPadding(0, 8, 0, 0)
            val textParams = LinearLayout.LayoutParams(
                (100 * resources.displayMetrics.density).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textView.layoutParams = textParams

            iconContainer.addView(imageView)
            iconContainer.addView(textView)

            iconContainer.setOnClickListener {
                onIconSelected(iconOption)
            }

            currentRow!!.addView(iconContainer)
        }

        scrollView.addView(linearLayout)

        AlertDialog.Builder(requireContext())
            .setTitle("Chọn biểu tượng")
            .setView(scrollView)
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showEditCategoryDialog(category: CategoryModel) {
        showCategoryDialog(category)
    }

    private fun showDeleteCategoryDialog(category: CategoryModel) {
        DialogUtils.showConfirmDialog(
            requireContext(),
            "Xác nhận xóa",
            "Bạn có chắc chắn muốn xóa danh mục ${category.name}?\n\nLưu ý: Việc xóa danh mục có thể ảnh hưởng đến các sách thuộc danh mục này.",
            positiveAction = {
                try {
                    val result = categoryDao.deleteCategory(category.id)
                    if (result > 0) {
                        DialogUtils.showToast(requireContext(), "Xóa danh mục thành công")
                        // Force refresh data
                        refreshData()
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), "Không thể xóa danh mục") {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting category", e)
                    DialogUtils.showErrorDialog(requireContext(), "Lỗi khi xóa: ${e.message}") {}
                }
            }
        )
    }

    private fun showCategoryDetailsDialog(category: CategoryModel) {
        val message = buildString {
            appendLine("Tên danh mục: ${category.name}")
            appendLine("Mô tả: ${category.description}")
            appendLine("Số sách: ${category.getBookCountText()}")
            appendLine("Trạng thái: ${category.getDisplayStatus()}")
            appendLine("Ngày tạo: ${category.createdAt}")
        }

        DialogUtils.showInfoDialog(requireContext(), "Chi tiết danh mục", message)
    }

    // Force refresh all data
    private fun refreshData() {
        loadCategories()
        loadStatistics()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }
}