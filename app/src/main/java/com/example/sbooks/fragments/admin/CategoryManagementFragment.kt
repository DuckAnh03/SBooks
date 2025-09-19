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
                categoryList
            } else {
                categoryDao.searchCategories(query)
            }

            // Apply status filter
            if (selectedStatus != 0) {
                val status = if (selectedStatus == 1) CategoryModel.CategoryStatus.ACTIVE else CategoryModel.CategoryStatus.INACTIVE
                filteredList = filteredList.filter { it.status == status }
            }

            categoryAdapter.submitList(filteredList)
            updateCategoryCount(filteredList.size)
            toggleEmptyState(filteredList.isEmpty())
        } catch (e: Exception) {
            Log.e(TAG, "Error filtering categories", e)
        }
    }

    private fun updateUI() {
        categoryAdapter.submitList(categoryList)
        updateCategoryCount(categoryList.size)
        toggleEmptyState(categoryList.isEmpty())
    }

    private fun updateCategoryCount(count: Int) {
        tvCategoryCount.text = "$count danh mục"
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        rvCategories.visibility = if (isEmpty) View.GONE else View.VISIBLE
        layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun showEditCategoryDialog(category: CategoryModel) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_category)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etCategoryName = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_category_name)
        val etCategoryDescription = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_category_description)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialog.findViewById<Button>(R.id.btn_save)

        // Fill existing data
        tvDialogTitle.text = "Sửa danh mục"
        etCategoryName.setText(category.name)
        etCategoryDescription.setText(category.description)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val name = etCategoryName.text.toString().trim()
            val description = etCategoryDescription.text.toString().trim()

            val validation = ValidationUtils.validateCategoryInput(name, description)

            if (!validation.isValid) {
                DialogUtils.showErrorDialog(requireContext(), validation.errors.joinToString("\n")) {}
                return@setOnClickListener
            }

            val updatedCategory = category.copy(
                name = name,
                description = description
            )

            try {
                val result = categoryDao.updateCategory(updatedCategory)
                if (result > 0) {
                    DialogUtils.showToast(requireContext(), "Cập nhật danh mục thành công")
                    loadCategories()
                    loadStatistics()
                    dialog.dismiss()
                } else {
                    DialogUtils.showErrorDialog(requireContext(), "Không thể cập nhật danh mục") {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating category", e)
                DialogUtils.showErrorDialog(requireContext(), "Lỗi khi cập nhật: ${e.message}") {}
            }
        }

        dialog.show()
    }

    private fun showDeleteCategoryDialog(category: CategoryModel) {
        DialogUtils.showConfirmDialog(
            requireContext(),
            "Xác nhận xóa",
            "Bạn có chắc chắn muốn xóa danh mục ${category.name}?",
            positiveAction = {
                try {
                    val result = categoryDao.deleteCategory(category.id)
                    if (result > 0) {
                        DialogUtils.showToast(requireContext(), "Xóa danh mục thành công")
                        loadCategories()
                        loadStatistics()
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
}
