package com.example.sbooks.fragments.staff

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
import com.example.sbooks.adapter.StaffOrderAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.OrderDao
import com.example.sbooks.models.OrderModel
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.SharedPrefsHelper

class StaffOrderFragment : Fragment() {

    private companion object {
        private const val TAG = "StaffOrderFragment"
    }

    // Views
    private lateinit var etSearchOrder: EditText
    private lateinit var spinnerOrderStatus: Spinner
    private lateinit var btnPendingOrders: Button
    private lateinit var btnProcessingOrders: Button
    private lateinit var btnCompletedOrders: Button
    private lateinit var tvSectionTitle: TextView
    private lateinit var tvOrderCount: TextView
    private lateinit var rvOrders: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout

    // Data
    private lateinit var orderAdapter: StaffOrderAdapter
    private lateinit var orderDao: OrderDao
    private lateinit var sharedPrefs: SharedPrefsHelper
    private var allOrders = mutableListOf<OrderModel>() // Store all orders loaded from DB
    private var currentFilter = OrderModel.OrderStatus.PENDING

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_staff_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initializeViews(view)
            setupDatabase()
            setupRecyclerView()
            setupSpinner()
            setupButtons()
            setupSearchListener()
            loadOrders()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
        }
    }

    private fun initializeViews(view: View) {
        etSearchOrder = view.findViewById(R.id.et_search_order)
        spinnerOrderStatus = view.findViewById(R.id.spinner_order_status)
        btnPendingOrders = view.findViewById(R.id.btn_pending_orders)
        btnProcessingOrders = view.findViewById(R.id.btn_processing_orders)
        btnCompletedOrders = view.findViewById(R.id.btn_completed_orders)
        tvSectionTitle = view.findViewById(R.id.tv_section_title)
        tvOrderCount = view.findViewById(R.id.tv_order_count)
        rvOrders = view.findViewById(R.id.rv_orders)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)
    }

    private fun setupDatabase() {
        val dbHelper = DatabaseHelper(requireContext())
        orderDao = OrderDao(dbHelper.writableDatabase)
        sharedPrefs = SharedPrefsHelper(requireContext())
    }

    private fun setupRecyclerView() {
        orderAdapter = StaffOrderAdapter(
            onViewDetailsClick = { order -> showOrderDetailsDialog(order) },
            onProcessOrderClick = { order -> processOrder(order) },
            onContactCustomerClick = { order -> contactCustomer(order) },
            onItemClick = { order -> showOrderDetailsDialog(order) }
        )

        rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }

    private fun setupSpinner() {
        val statuses = resources.getStringArray(R.array.staff_order_status)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOrderStatus.adapter = adapter

        spinnerOrderStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Apply additional filtering based on spinner if needed
                filterOrders()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupButtons() {
        btnPendingOrders.setOnClickListener {
            selectStatusFilter(OrderModel.OrderStatus.PENDING, btnPendingOrders)
        }

        btnProcessingOrders.setOnClickListener {
            selectStatusFilter(OrderModel.OrderStatus.PROCESSING, btnProcessingOrders)
        }

        btnCompletedOrders.setOnClickListener {
            selectStatusFilter(OrderModel.OrderStatus.DELIVERED, btnCompletedOrders)
        }

        // Set initial selection
        selectStatusFilter(OrderModel.OrderStatus.PENDING, btnPendingOrders)
    }

    private fun selectStatusFilter(status: OrderModel.OrderStatus, selectedButton: Button) {
        currentFilter = status

        // Reset all button styles
        btnPendingOrders.setBackgroundResource(R.drawable.bg_button_secondary)
        btnProcessingOrders.setBackgroundResource(R.drawable.bg_button_secondary)
        btnCompletedOrders.setBackgroundResource(R.drawable.bg_button_secondary)

        // Set selected button style
        selectedButton.setBackgroundResource(R.drawable.bg_button_primary)

        // Update section title
        tvSectionTitle.text = when (status) {
            OrderModel.OrderStatus.PENDING -> "Đơn hàng chờ xử lý"
            OrderModel.OrderStatus.PROCESSING -> "Đơn hàng đang xử lý"
            OrderModel.OrderStatus.DELIVERED -> "Đơn hàng hoàn thành"
            else -> "Danh sách đơn hàng"
        }

        // Clear search box when changing filter
        etSearchOrder.setText("")

        loadOrders()
    }

    private fun setupSearchListener() {
        etSearchOrder.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterOrders()
            }
        })
    }

    private fun loadOrders() {
        try {
            allOrders.clear()

            // Load orders based on current filter - FIX: Load correct data from DB
            val orders = when (currentFilter) {
                OrderModel.OrderStatus.PENDING -> {
                    orderDao.getOrdersByStatus(OrderModel.OrderStatus.PENDING)
                }
                OrderModel.OrderStatus.PROCESSING -> {
                    // Include both PROCESSING and SHIPPING statuses for "Đang xử lý" tab
                    val processingOrders = orderDao.getOrdersByStatus(OrderModel.OrderStatus.PROCESSING)
                    val shippingOrders = orderDao.getOrdersByStatus(OrderModel.OrderStatus.SHIPPING)
                    processingOrders + shippingOrders
                }
                OrderModel.OrderStatus.DELIVERED -> {
                    orderDao.getOrdersByStatus(OrderModel.OrderStatus.DELIVERED)
                }
                else -> orderDao.getAllOrders()
            }

            allOrders.addAll(orders)
            filterOrders() // Apply search filter if any
        } catch (e: Exception) {
            Log.e(TAG, "Error loading orders", e)
            allOrders.clear()
            updateUI(emptyList())
        }
    }

    private fun filterOrders() {
        val query = etSearchOrder.text.toString().trim()

        try {
            // FIX: Filter from already loaded orders (allOrders) instead of querying DB again
            val filteredList = if (query.isEmpty()) {
                allOrders
            } else {
                // Filter in memory based on search query
                allOrders.filter { order ->
                    order.orderCode.contains(query, ignoreCase = true) ||
                            order.customerName.contains(query, ignoreCase = true) ||
                            order.customerPhone.contains(query, ignoreCase = true)
                }
            }

            updateUI(filteredList)
        } catch (e: Exception) {
            Log.e(TAG, "Error filtering orders", e)
            updateUI(emptyList())
        }
    }

    private fun updateUI(orders: List<OrderModel>) {
        orderAdapter.submitList(orders.toList()) // Create new list to trigger DiffUtil
        updateOrderCount(orders.size)
        toggleEmptyState(orders.isEmpty())
    }

    private fun updateOrderCount(count: Int) {
        tvOrderCount.text = "$count đơn hàng"
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        rvOrders.visibility = if (isEmpty) View.GONE else View.VISIBLE
        layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun showOrderDetailsDialog(order: OrderModel) {
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
    }

    private fun processOrder(order: OrderModel) {
        val nextStatus = when (order.status) {
            OrderModel.OrderStatus.PENDING -> OrderModel.OrderStatus.PROCESSING
            OrderModel.OrderStatus.PROCESSING -> OrderModel.OrderStatus.SHIPPING
            OrderModel.OrderStatus.SHIPPING -> OrderModel.OrderStatus.DELIVERED
            else -> return
        }

        val actionText = when (nextStatus) {
            OrderModel.OrderStatus.PROCESSING -> "Bắt đầu xử lý"
            OrderModel.OrderStatus.SHIPPING -> "Chuyển sang giao hàng"
            OrderModel.OrderStatus.DELIVERED -> "Xác nhận đã giao"
            else -> "Cập nhật"
        }

        DialogUtils.showConfirmDialog(
            requireContext(),
            "Cập nhật trạng thái đơn hàng",
            "Bạn có chắc chắn muốn $actionText đơn hàng ${order.orderCode}?",
            positiveAction = {
                updateOrderStatus(order, nextStatus)
            }
        )
    }

    private fun updateOrderStatus(order: OrderModel, newStatus: OrderModel.OrderStatus) {
        try {
            val staffId = sharedPrefs.getUserId()
            val staffName = sharedPrefs.getString("full_name", "Nhân viên")

            val result = orderDao.updateOrderStatus(order.id, newStatus, staffId, staffName)
            if (result > 0) {
                DialogUtils.showToast(requireContext(), "Cập nhật trạng thái thành công")
                loadOrders() // Reload to get fresh data
            } else {
                DialogUtils.showErrorDialog(requireContext(), "Không thể cập nhật trạng thái") {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order status", e)
            DialogUtils.showErrorDialog(requireContext(), "Lỗi khi cập nhật: ${e.message}") {}
        }
    }

    private fun contactCustomer(order: OrderModel) {
        val options = arrayOf("Gọi điện", "Gửi SMS")
        DialogUtils.showChoiceDialog(
            requireContext(),
            "Liên hệ khách hàng",
            options
        ) { selectedIndex ->
            when (selectedIndex) {
                0 -> DialogUtils.showToast(requireContext(), "Gọi ${order.customerPhone}")
                1 -> DialogUtils.showToast(requireContext(), "Gửi SMS đến ${order.customerPhone}")
            }
        }
    }
}