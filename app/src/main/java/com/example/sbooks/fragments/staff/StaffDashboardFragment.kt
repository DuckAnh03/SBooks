package com.example.sbooks.fragments.staff

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.adapter.StaffOrderAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.BookDao
import com.example.sbooks.database.dao.OrderDao
import com.example.sbooks.models.OrderModel
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.SharedPrefsHelper

class StaffDashboardFragment : Fragment() {

    private companion object {
        private const val TAG = "StaffDashboardFragment"
    }

    // Views
    private var tvAssignedOrders: TextView? = null
    private var tvLowStockItems: TextView? = null
    private var btnViewOrders: Button? = null
    private var btnUpdateInventory: Button? = null
    private var tvViewAllPending: TextView? = null
    private var rvPendingOrders: RecyclerView? = null

    // Data
    private var orderDao: OrderDao? = null
    private var bookDao: BookDao? = null
    private var sharedPrefs: SharedPrefsHelper? = null
    private var orderAdapter: StaffOrderAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView called")
        return try {
            inflater.inflate(R.layout.fragment_staff_dashboard, container, false)
        } catch (e: Exception) {
            Log.e(TAG, "Error inflating layout, creating fallback view", e)
            createFallbackView()
        }
    }

    private fun createFallbackView(): View {
        val textView = TextView(requireContext())
        textView.text = "Staff Dashboard - Loading..."
        textView.textSize = 18f
        textView.setPadding(32, 32, 32, 32)
        return textView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        try {
            initializeComponents()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            showError("Lỗi khởi tạo dashboard: ${e.message}")
        }
    }

    private fun initializeComponents() {
        try {
            initializeViews()
            setupDatabase()
            setupRecyclerView()
            setupClickListeners()
            loadDashboardData()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing components", e)
            throw e
        }
    }

    private fun initializeViews() {
        try {
            view?.let { v ->
                tvAssignedOrders = v.findViewById(R.id.tv_assigned_orders)
                tvLowStockItems = v.findViewById(R.id.tv_low_stock_items)
                btnViewOrders = v.findViewById(R.id.btn_view_orders)
                btnUpdateInventory = v.findViewById(R.id.btn_update_inventory)
                tvViewAllPending = v.findViewById(R.id.tv_view_all_pending)
                rvPendingOrders = v.findViewById(R.id.rv_pending_orders)

                Log.d(TAG, "Views initialized successfully")
            } ?: throw Exception("View is null")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            showError("Một số thành phần giao diện không tải được")
        }
    }

    private fun setupDatabase() {
        try {
            val dbHelper = DatabaseHelper(requireContext())
            orderDao = OrderDao(dbHelper.writableDatabase)
            bookDao = BookDao(dbHelper.writableDatabase)
            sharedPrefs = SharedPrefsHelper(requireContext())
            Log.d(TAG, "Database setup successful")
        } catch (e: Exception) {
            Log.e(TAG, "Database setup failed", e)
            showError("Không thể kết nối database")
        }
    }

    private fun setupRecyclerView() {
        try {
            rvPendingOrders?.let { recyclerView ->
                orderAdapter = StaffOrderAdapter(
                    onViewDetailsClick = { order -> showOrderDetails(order) },
                    onProcessOrderClick = { order -> processOrder(order) },
                    onContactCustomerClick = { order -> contactCustomer(order) },
                    onItemClick = { order -> showOrderDetails(order) }
                )

                recyclerView.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = orderAdapter
                }
                Log.d(TAG, "RecyclerView setup successful")
            }
        } catch (e: Exception) {
            Log.e(TAG, "RecyclerView setup failed", e)
        }
    }

    private fun setupClickListeners() {
        try {
            btnViewOrders?.setOnClickListener {
                Log.d(TAG, "Navigate to orders fragment")
                showInfo("Chuyển đến quản lý đơn hàng")
            }

            btnUpdateInventory?.setOnClickListener {
                Log.d(TAG, "Navigate to inventory fragment")
                showInfo("Chuyển đến quản lý kho")
            }

            tvViewAllPending?.setOnClickListener {
                Log.d(TAG, "Navigate to all pending orders")
                showInfo("Xem tất cả đơn hàng chờ xử lý")
            }

            Log.d(TAG, "Click listeners setup successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun loadDashboardData() {
        try {
            val staffId = sharedPrefs?.getUserId() ?: -1
            val orderDaoInstance = orderDao
            val bookDaoInstance = bookDao

            if (orderDaoInstance != null && bookDaoInstance != null && staffId != -1) {
                // Load assigned orders
                val assignedOrders = orderDaoInstance.getOrdersByStaff(staffId)
                tvAssignedOrders?.text = assignedOrders.size.toString()

                // Load low stock items
                val lowStockBooks = bookDaoInstance.getLowStockBooks(10)
                tvLowStockItems?.text = lowStockBooks.size.toString()

                // Load pending orders for display
                val pendingOrders = orderDaoInstance.getOrdersByStatus(OrderModel.OrderStatus.PENDING).take(5)
                orderAdapter?.submitList(pendingOrders)

                Log.d(TAG, "Dashboard data loaded: Assigned=${assignedOrders.size}, LowStock=${lowStockBooks.size}, Pending=${pendingOrders.size}")
            } else {
                Log.w(TAG, "Cannot load data: DAO or staffId is invalid")
                setDefaultValues()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading dashboard data", e)
            setDefaultValues()
            showError("Không thể tải dữ liệu dashboard")
        }
    }

    private fun setDefaultValues() {
        tvAssignedOrders?.text = "0"
        tvLowStockItems?.text = "0"
    }

    private fun showOrderDetails(order: OrderModel) {
        try {
            val message = buildString {
                appendLine("Mã đơn hàng: ${order.orderCode}")
                appendLine("Khách hàng: ${order.customerName}")
                appendLine("Số điện thoại: ${order.customerPhone}")
                appendLine("Địa chỉ: ${order.customerAddress}")
                appendLine("Tổng tiền: ${order.getFormattedTotal()}")
                appendLine("Trạng thái: ${order.getDisplayStatus()}")
                appendLine("Ngày đặt: ${order.orderDate}")
                appendLine("Số lượng sách: ${order.getItemCount()}")
            }

            DialogUtils.showInfoDialog(requireContext(), "Chi tiết đơn hàng", message)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing order details", e)
            showError("Không thể hiển thị chi tiết đơn hàng")
        }
    }

    private fun processOrder(order: OrderModel) {
        try {
            val nextStatus = when (order.status) {
                OrderModel.OrderStatus.PENDING -> OrderModel.OrderStatus.PROCESSING
                OrderModel.OrderStatus.PROCESSING -> OrderModel.OrderStatus.SHIPPING
                OrderModel.OrderStatus.SHIPPING -> OrderModel.OrderStatus.DELIVERED
                else -> return
            }

            val actionText = when (nextStatus) {
                OrderModel.OrderStatus.PROCESSING -> "bắt đầu xử lý"
                OrderModel.OrderStatus.SHIPPING -> "chuyển sang giao hàng"
                OrderModel.OrderStatus.DELIVERED -> "xác nhận đã giao"
                else -> "cập nhật"
            }

            DialogUtils.showConfirmDialog(
                requireContext(),
                "Xác nhận xử lý",
                "Bạn có muốn $actionText đơn hàng ${order.orderCode}?",
                positiveAction = {
                    updateOrderStatus(order, nextStatus)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing order", e)
            showError("Không thể xử lý đơn hàng")
        }
    }

    private fun contactCustomer(order: OrderModel) {
        try {
            val options = arrayOf("Gọi điện", "Gửi SMS")
            DialogUtils.showChoiceDialog(
                requireContext(),
                "Liên hệ khách hàng",
                options
            ) { selectedIndex ->
                when (selectedIndex) {
                    0 -> showInfo("Gọi ${order.customerPhone}")
                    1 -> showInfo("Gửi SMS đến ${order.customerPhone}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error contacting customer", e)
            showError("Không thể liên hệ khách hàng")
        }
    }

    private fun updateOrderStatus(order: OrderModel, newStatus: OrderModel.OrderStatus) {
        try {
            val orderDaoInstance = orderDao
            val staffId = sharedPrefs?.getUserId() ?: -1
            val staffName = sharedPrefs?.getString("full_name", "Nhân viên") ?: "Nhân viên"

            if (orderDaoInstance != null && staffId != -1) {
                val result = orderDaoInstance.updateOrderStatus(order.id, newStatus, staffId, staffName)
                if (result > 0) {
                    showInfo("Cập nhật trạng thái thành công")
                    loadDashboardData() // Refresh data
                } else {
                    showError("Không thể cập nhật trạng thái")
                }
            } else {
                showError("Lỗi hệ thống")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order status", e)
            showError("Lỗi khi cập nhật: ${e.message}")
        }
    }

    private fun showError(message: String) {
        try {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Lỗi: $message", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing error message", e)
        }
    }

    private fun showInfo(message: String) {
        try {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing info message", e)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - refreshing data")
        try {
            loadDashboardData()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        tvAssignedOrders = null
        tvLowStockItems = null
        btnViewOrders = null
        btnUpdateInventory = null
        tvViewAllPending = null
        rvPendingOrders = null
        orderAdapter = null
        orderDao = null
        bookDao = null
        sharedPrefs = null
    }
}