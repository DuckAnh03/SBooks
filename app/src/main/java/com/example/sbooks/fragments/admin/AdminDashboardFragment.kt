package com.example.sbooks.fragments.admin

import android.app.Dialog
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
import com.example.sbooks.adapter.OrderAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.BookDao
import com.example.sbooks.database.dao.OrderDao
import com.example.sbooks.database.dao.UserDao
import com.example.sbooks.database.dao.CategoryDao
import com.example.sbooks.models.*
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.ValidationUtils

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
    private lateinit var tvViewAllOrders: TextView

    // Database
    private lateinit var userDao: UserDao
    private lateinit var bookDao: BookDao
    private lateinit var orderDao: OrderDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var orderAdapter: OrderAdapter

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
            setupRecyclerView()
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
            tvViewAllOrders = view.findViewById(R.id.tv_view_all_orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
        }
    }

    private fun setupDatabase() {
        try {
            val dbHelper = DatabaseHelper(requireContext())
            userDao = UserDao(dbHelper.writableDatabase)
            bookDao = BookDao(dbHelper.writableDatabase)
            orderDao = OrderDao(dbHelper.writableDatabase)
            categoryDao = CategoryDao(dbHelper.writableDatabase)
        } catch (e: Exception) {
            Log.e(TAG, "Database setup failed", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            orderAdapter = OrderAdapter(
                onViewDetailsClick = { order -> showOrderDetailsDialog(order) },
                onUpdateStatusClick = { order -> showUpdateOrderStatusDialog(order) },
                onItemClick = { order -> showOrderDetailsDialog(order) }
            )

            rvRecentOrders.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = orderAdapter
            }
        } catch (e: Exception) {
            Log.e(TAG, "RecyclerView setup failed", e)
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

            tvViewAllOrders.setOnClickListener {
                // Navigate to orders fragment
                DialogUtils.showToast(requireContext(), "Chuyển đến quản lý đơn hàng")
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
            val allOrders = orderDao.getAllOrders()
            val totalOrders = allOrders.size
            val totalRevenue = allOrders
                .filter { it.status == OrderModel.OrderStatus.DELIVERED }
                .sumOf { it.finalAmount }

            // Update UI
            tvTotalUsers.text = totalUsers.toString()
            tvTotalBooks.text = totalBooks.toString()
            tvTotalOrders.text = totalOrders.toString()
            tvTotalRevenue.text = String.format("%,.0f VNĐ", totalRevenue)

            // Load recent orders (last 5)
            val recentOrders = allOrders.take(5)
            orderAdapter.submitList(recentOrders)

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

            val etBookTitle = dialogView.findViewById<EditText>(R.id.et_book_title)
            val etBookAuthor = dialogView.findViewById<EditText>(R.id.et_book_author)
            val etBookPublisher = dialogView.findViewById<EditText>(R.id.et_book_publisher)
            val etBookPrice = dialogView.findViewById<EditText>(R.id.et_book_price)
            val etBookStock = dialogView.findViewById<EditText>(R.id.et_book_stock)
            val etBookDescription = dialogView.findViewById<EditText>(R.id.et_book_description)
            val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_book_category_dialog)
            val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
            val btnSave = dialogView.findViewById<Button>(R.id.btn_save)

            // Setup category spinner
            val categories = categoryDao.getActiveCategories()
            val categoryNames = categories.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

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
                } else {
                    null
                }

                val newBook = BookModel(
                    title = title,
                    author = author,
                    publisher = publisher,
                    categoryId = selectedCategory?.id ?: 1,
                    categoryName = selectedCategory?.name ?: "",
                    price = priceStr.toDouble(),
                    stock = stockStr.toInt(),
                    description = description,
                    status = BookModel.BookStatus.ACTIVE
                )

                try {
                    val result = bookDao.insertBook(newBook)
                    if (result > 0) {
                        DialogUtils.showToast(requireContext(), "Thêm sách thành công")
                        loadDashboardData()
                        dialog.dismiss()
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), "Không thể thêm sách") {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving book", e)
                    DialogUtils.showErrorDialog(requireContext(), "Lỗi khi thêm: ${e.message}") {}
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

            val etUserName = dialogView.findViewById<EditText>(R.id.et_user_name)
            val etUserEmail = dialogView.findViewById<EditText>(R.id.et_user_email)
            val etUserPhone = dialogView.findViewById<EditText>(R.id.et_user_phone)
            val etUserPassword = dialogView.findViewById<EditText>(R.id.et_user_password)
            val spinnerRole = dialogView.findViewById<Spinner>(R.id.spinner_user_role_dialog)
            val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
            val btnSave = dialogView.findViewById<Button>(R.id.btn_save)

            // Setup role spinner
            val roles = arrayOf("Quản trị viên", "Nhân viên", "Khách hàng")
            val roleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
            roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRole.adapter = roleAdapter

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            btnCancel.setOnClickListener { dialog.dismiss() }
            btnSave.setOnClickListener {
                val fullName = etUserName.text.toString().trim()
                val email = etUserEmail.text.toString().trim()
                val phone = etUserPhone.text.toString().trim()
                val password = etUserPassword.text.toString().trim()
                val username = email.substringBefore("@")
                val selectedRoleIndex = spinnerRole.selectedItemPosition

                val validation = ValidationUtils.validateUserInput(
                    username = username,
                    email = email,
                    password = password,
                    fullName = fullName,
                    phone = phone
                )

                if (!validation.isValid) {
                    DialogUtils.showErrorDialog(requireContext(), validation.errors.joinToString("\n")) {}
                    return@setOnClickListener
                }

                // Check if email already exists
                val existingUser = userDao.getUserByEmail(email)
                if (existingUser != null) {
                    DialogUtils.showErrorDialog(requireContext(), "Email đã được sử dụng") {}
                    return@setOnClickListener
                }

                val role = when (selectedRoleIndex) {
                    0 -> UserModel.UserRole.ADMIN
                    1 -> UserModel.UserRole.STAFF
                    2 -> UserModel.UserRole.CUSTOMER
                    else -> UserModel.UserRole.CUSTOMER
                }

                val newUser = UserModel(
                    username = username,
                    email = email,
                    phone = phone,
                    fullName = fullName,
                    password = password,
                    role = role,
                    status = UserModel.UserStatus.ACTIVE
                )

                try {
                    val result = userDao.insertUser(newUser)
                    if (result > 0) {
                        DialogUtils.showToast(requireContext(), "Thêm người dùng thành công")
                        loadDashboardData()
                        dialog.dismiss()
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), "Không thể thêm người dùng") {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving user", e)
                    DialogUtils.showErrorDialog(requireContext(), "Lỗi khi thêm: ${e.message}") {}
                }
            }

            dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing add user dialog", e)
            showError("Không thể mở dialog thêm người dùng")
        }
    }

    private fun showOrderDetailsDialog(order: OrderModel) {
        val message = buildString {
            appendLine("Mã đơn hàng: ${order.orderCode}")
            appendLine("Khách hàng: ${order.customerName}")
            appendLine("Email: ${order.customerEmail}")
            appendLine("Số điện thoại: ${order.customerPhone}")
            appendLine("Địa chỉ: ${order.customerAddress}")
            appendLine("Tổng tiền: ${order.getFormattedTotal()}")
            appendLine("Trạng thái: ${order.getDisplayStatus()}")
            appendLine("Ngày đặt: ${order.orderDate}")
            appendLine("Số lượng sách: ${order.getItemCount()}")
        }

        DialogUtils.showInfoDialog(requireContext(), "Chi tiết đơn hàng", message)
    }

    private fun showUpdateOrderStatusDialog(order: OrderModel) {
        val statuses = arrayOf("Chờ xử lý", "Đang xử lý", "Đang giao", "Đã giao", "Đã hủy")
        val currentIndex = when (order.status) {
            OrderModel.OrderStatus.PENDING -> 0
            OrderModel.OrderStatus.PROCESSING -> 1
            OrderModel.OrderStatus.SHIPPING -> 2
            OrderModel.OrderStatus.DELIVERED -> 3
            OrderModel.OrderStatus.CANCELLED -> 4
        }

        DialogUtils.showSingleChoiceDialog(
            requireContext(),
            "Cập nhật trạng thái đơn hàng",
            statuses,
            currentIndex
        ) { selectedIndex ->
            val newStatus = when (selectedIndex) {
                0 -> OrderModel.OrderStatus.PENDING
                1 -> OrderModel.OrderStatus.PROCESSING
                2 -> OrderModel.OrderStatus.SHIPPING
                3 -> OrderModel.OrderStatus.DELIVERED
                4 -> OrderModel.OrderStatus.CANCELLED
                else -> return@showSingleChoiceDialog
            }

            try {
                val result = orderDao.updateOrderStatus(order.id, newStatus)
                if (result > 0) {
                    DialogUtils.showToast(requireContext(), "Cập nhật trạng thái thành công")
                    loadDashboardData()
                } else {
                    DialogUtils.showErrorDialog(requireContext(), "Không thể cập nhật trạng thái") {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating order status", e)
                DialogUtils.showErrorDialog(requireContext(), "Lỗi khi cập nhật: ${e.message}") {}
            }
        }
    }

    private fun showError(message: String) {
        try {
            if (isAdded && context != null) {
                DialogUtils.showErrorDialog(requireContext(), message) {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing error dialog", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            loadDashboardData()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }
}