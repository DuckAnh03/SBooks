package com.example.sbooks.fragments.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.BookDao
import com.example.sbooks.database.dao.OrderDao
import com.example.sbooks.database.dao.UserDao

class AdminDashboardFragment : Fragment() {

    private companion object {
        private const val TAG = "AdminDashboardFragment"
    }

    // Views
    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvTotalBooks: TextView
    private lateinit var tvTotalUsers: TextView
    private lateinit var btnAddBook: Button
    private lateinit var btnAddUser: Button
    private lateinit var rvRecentOrders: RecyclerView

    // Database
    private lateinit var userDao: UserDao
    private lateinit var bookDao: BookDao
    private lateinit var orderDao: OrderDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        try {
            initializeViews(view)
            setupDatabase()
            setupClickListeners()
            loadDashboardData()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            showError("Lỗi khởi tạo dashboard: ${e.message}")
        }
    }

    private fun initializeViews(view: View) {
        try {
            tvTotalRevenue = view.findViewById(R.id.tv_total_revenue)
            tvTotalOrders = view.findViewById(R.id.tv_total_orders)
            tvTotalBooks = view.findViewById(R.id.tv_total_books)
            tvTotalUsers = view.findViewById(R.id.tv_total_users)
            btnAddBook = view.findViewById(R.id.btn_add_book)
            btnAddUser = view.findViewById(R.id.btn_add_user)
            rvRecentOrders = view.findViewById(R.id.rv_recent_orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            // Set fallback behavior - create simple text view
            val textView = TextView(requireContext())
            textView.text = "Admin Dashboard - Views not found"
            textView.textSize = 18f
            textView.setPadding(32, 32, 32, 32)
        }
    }

    private fun setupDatabase() {
        try {
            val dbHelper = DatabaseHelper(requireContext())
            userDao = UserDao(dbHelper.writableDatabase)
            bookDao = BookDao(dbHelper.writableDatabase)
            orderDao = OrderDao(dbHelper.writableDatabase)
        } catch (e: Exception) {
            Log.e(TAG, "Database setup failed", e)
        }
    }

    private fun setupClickListeners() {
        try {
            btnAddBook.setOnClickListener {
                Log.d(TAG, "Add book button clicked")
                showAddBookDialog()
            }

            btnAddUser.setOnClickListener {
                Log.d(TAG, "Add user button clicked")
                showAddUserDialog()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun loadDashboardData() {
        try {
            // Load statistics
            val totalUsers = userDao.getAllUsers().size
            val totalBooks = bookDao.getAllBooks().size
            val totalOrders = orderDao.getAllOrders().size
            val totalRevenue = orderDao.getAllOrders().sumOf { it.finalAmount }

            // Update UI
            tvTotalUsers.text = totalUsers.toString()
            tvTotalBooks.text = totalBooks.toString()
            tvTotalOrders.text = totalOrders.toString()
            tvTotalRevenue.text = String.format("%,.0f VNĐ", totalRevenue)

            Log.d(TAG, "Dashboard data loaded: Users=$totalUsers, Books=$totalBooks, Orders=$totalOrders")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading dashboard data", e)
            // Set default values
            tvTotalUsers?.text = "0"
            tvTotalBooks?.text = "0"
            tvTotalOrders?.text = "0"
            tvTotalRevenue?.text = "0 VNĐ"
        }
    }

    private fun showAddBookDialog() {
        try {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_book, null)

            // Get views
            val etBookTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_book_title)
            val etBookAuthor = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_book_author)
            val etBookPrice = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_book_price)
            val etBookStock = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_book_stock)
            val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_book_category_dialog)
            val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
            val btnSave = dialogView.findViewById<Button>(R.id.btn_save)

            // Setup category spinner
            val categories = listOf("Văn học", "Khoa học", "Lịch sử", "Kinh tế", "Giáo dục")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            btnCancel.setOnClickListener { dialog.dismiss() }
            btnSave.setOnClickListener {
                val title = etBookTitle.text.toString().trim()
                val author = etBookAuthor.text.toString().trim()
                val priceStr = etBookPrice.text.toString().trim()
                val stockStr = etBookStock.text.toString().trim()

                if (validateBookInput(title, author, priceStr, stockStr)) {
                    // TODO: Save book to database
                    Toast.makeText(requireContext(), "Đã lưu sách: $title", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    loadDashboardData() // Refresh data
                }
            }

            dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing add book dialog", e)
            showError("Không thể mở dialog thêm sách")
        }
    }

    private fun showAddUserDialog() {
        try {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_user, null)

            // Get views
            val etUserName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_user_name)
            val etUserEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_user_email)
            val etUserPhone = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_user_phone)
            val etUserPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_user_password)
            val spinnerRole = dialogView.findViewById<Spinner>(R.id.spinner_user_role_dialog)
            val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
            val btnSave = dialogView.findViewById<Button>(R.id.btn_save)

            // Setup role spinner
            val roles = listOf("Khách hàng", "Nhân viên", "Quản trị viên")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRole.adapter = adapter

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            btnCancel.setOnClickListener { dialog.dismiss() }
            btnSave.setOnClickListener {
                val name = etUserName.text.toString().trim()
                val email = etUserEmail.text.toString().trim()
                val phone = etUserPhone.text.toString().trim()
                val password = etUserPassword.text.toString().trim()

                if (validateUserInput(name, email, password)) {
                    // TODO: Save user to database
                    Toast.makeText(requireContext(), "Đã tạo tài khoản: $name", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    loadDashboardData() // Refresh data
                }
            }

            dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing add user dialog", e)
            showError("Không thể mở dialog thêm người dùng")
        }
    }

    private fun validateBookInput(title: String, author: String, price: String, stock: String): Boolean {
        return when {
            title.isEmpty() -> {
                showError("Tên sách không được để trống")
                false
            }
            author.isEmpty() -> {
                showError("Tác giả không được để trống")
                false
            }
            price.isEmpty() || price.toDoubleOrNull() == null || price.toDouble() <= 0 -> {
                showError("Giá sách không hợp lệ")
                false
            }
            stock.isEmpty() || stock.toIntOrNull() == null || stock.toInt() < 0 -> {
                showError("Số lượng không hợp lệ")
                false
            }
            else -> true
        }
    }

    private fun validateUserInput(name: String, email: String, password: String): Boolean {
        return when {
            name.isEmpty() -> {
                showError("Tên không được để trống")
                false
            }
            email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Email không hợp lệ")
                false
            }
            password.isEmpty() || password.length < 6 -> {
                showError("Mật khẩu phải có ít nhất 6 ký tự")
                false
            }
            else -> true
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}