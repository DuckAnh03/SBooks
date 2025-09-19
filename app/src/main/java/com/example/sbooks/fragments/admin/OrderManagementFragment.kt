package com.example.sbooks.fragments.admin

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
import com.example.sbooks.adapter.OrderAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.OrderDao
import com.example.sbooks.models.OrderModel
import com.example.sbooks.utils.DialogUtils

class OrderManagementFragment : Fragment() {

    private companion object {
        private const val TAG = "OrderManagementFragment"
    }

    // Views
    private lateinit var etSearchOrder: EditText
    private lateinit var spinnerOrderStatus: Spinner
    private lateinit var btnDateFrom: Button
    private lateinit var btnDateTo: Button
    private lateinit var tvPendingCount: TextView
    private lateinit var tvProcessingCount: TextView
    private lateinit var tvDeliveredCount: TextView
    private lateinit var rvOrders: RecyclerView
    private lateinit var tvOrderCount: TextView
    private lateinit var layoutEmptyState: LinearLayout

    // Data
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var orderDao: OrderDao
    private var orderList = mutableListOf<OrderModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_order_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initializeViews(view)
            setupDatabase()
            setupRecyclerView()
            setupSpinner()
            setupDateButtons()
            setupSearchListener()
            loadOrders()
            loadStatistics()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
        }
    }

    private fun initializeViews(view: View) {
        etSearchOrder = view.findViewById(R.id.et_search_order)
        spinnerOrderStatus = view.findViewById(R.id.spinner_order_status)
        btnDateFrom = view.findViewById(R.id.btn_date_from)
        btnDateTo = view.findViewById(R.id.btn_date_to)
        tvPendingCount = view.findViewById(R.id.tv_pending_count)
        tvProcessingCount = view.findViewById(R.id.tv_processing_count)
        tvDeliveredCount = view.findViewById(R.id.tv_delivered_count)
        rvOrders = view.findViewById(R.id.rv_orders)
        tvOrderCount = view.findViewById(R.id.tv_order_count)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)
    }

    private fun setupDatabase() {
        val dbHelper = DatabaseHelper(requireContext())
        orderDao = OrderDao(dbHelper.writableDatabase)
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            onViewDetailsClick = { order -> showOrderDetailsDialog(order) },
            onUpdateStatusClick = { order -> showUpdateStatusDialog(order) },
            onItemClick = { order -> showOrderDetailsDialog(order) }
        )

        rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }

    private fun setupSpinner() {
        val statuses = resources.getStringArray(R.array.order_status)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOrderStatus.adapter = adapter

        spinnerOrderStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterOrders()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDateButtons() {
        btnDateFrom.setOnClickListener {
            DialogUtils.showToast(requireContext(), "Chọn ngày bắt đầu")
        }

        btnDateTo.setOnClickListener {
            DialogUtils.showToast(requireContext(), "Chọn ngày kết thúc")
        }
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
            orderList.clear()
            orderList.addAll(orderDao.getAllOrders())
            updateUI()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading orders", e)
        }
    }

    private fun loadStatistics() {
        try {
            val pendingOrders = orderDao.getOrdersByStatus(OrderModel.OrderStatus.PENDING)
            val processingOrders = orderDao.getOrdersByStatus(OrderModel.OrderStatus.PROCESSING)
            val deliveredOrders = orderDao.getOrdersByStatus(OrderModel.OrderStatus.DELIVERED)

            tvPendingCount.text = pendingOrders.size.toString()
            tvProcessingCount.text = processingOrders.size.toString()
            tvDeliveredCount.text = deliveredOrders.size.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading statistics", e)
        }
    }

    private fun filterOrders() {
        val query = etSearchOrder.text.toString().trim()
        val selectedStatus = when (spinnerOrderStatus.selectedItemPosition) {
            0 -> null // All statuses
            1 -> "pending"
            2 -> "processing"
            3 -> "shipping"
            4 -> "delivered"
            5 -> "cancelled"
            else -> null
        }

        try {
            val filteredList = orderDao.searchOrders(query, selectedStatus)
            orderAdapter.submitList(filteredList)
            updateOrderCount(filteredList.size)
            toggleEmptyState(filteredList.isEmpty())
        } catch (e: Exception) {
            Log.e(TAG, "Error filtering orders", e)
        }
    }

    private fun updateUI() {
        orderAdapter.submitList(orderList)
        updateOrderCount(orderList.size)
        toggleEmptyState(orderList.isEmpty())
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
            appendLine("Email: ${order.customerEmail}")
            appendLine("Số điện thoại: ${order.customerPhone}")
            appendLine("Địa chỉ: ${order.customerAddress}")
            appendLine("Tổng tiền: ${order.getFormattedTotal()}")
            appendLine("Phí ship: ${String.format("%,.0f VNĐ", order.shippingFee)}")
            appendLine("Giảm giá: ${String.format("%,.0f VNĐ", order.discountAmount)}")
            appendLine("Thành tiền: ${order.getFormattedTotal()}")
            appendLine("Trạng thái: ${order.getDisplayStatus()}")
            appendLine("Phương thức TT: ${order.getDisplayPaymentMethod()}")
            appendLine("Ngày đặt: ${order.orderDate}")
            appendLine("Số lượng sách: ${order.getItemCount()}")
            if (order.notes.isNotEmpty()) {
                appendLine("Ghi chú: ${order.notes}")
            }
        }

        DialogUtils.showInfoDialog(requireContext(), "Chi tiết đơn hàng", message)
    }

    private fun showUpdateStatusDialog(order: OrderModel) {
        val statuses = arrayOf(
            "Chờ xử lý",
            "Đang xử lý",
            "Đang giao",
            "Đã giao",
            "Đã hủy"
        )

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

            updateOrderStatus(order, newStatus)
        }
    }

    private fun updateOrderStatus(order: OrderModel, newStatus: OrderModel.OrderStatus) {
        try {
            val result = orderDao.updateOrderStatus(order.id, newStatus)
            if (result > 0) {
                DialogUtils.showToast(requireContext(), "Cập nhật trạng thái thành công")
                loadOrders()
                loadStatistics()
            } else {
                DialogUtils.showErrorDialog(requireContext(), "Không thể cập nhật trạng thái") {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order status", e)
            DialogUtils.showErrorDialog(requireContext(), "Lỗi khi cập nhật: ${e.message}") {}
        }
    }
}
