package com.example.sbooks.activities.staff

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.adapter.StaffOrderAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.OrderDao
import com.example.sbooks.models.OrderModel
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.PermissionUtils
import com.example.sbooks.utils.SharedPrefsHelper

class StaffOrderActivity : AppCompatActivity() {

    private lateinit var etSearchOrder: EditText
    private lateinit var spinnerOrderStatus: Spinner
    private lateinit var btnPendingOrders: Button
    private lateinit var btnProcessingOrders: Button
    private lateinit var btnCompletedOrders: Button
    private lateinit var tvSectionTitle: TextView
    private lateinit var tvOrderCount: TextView
    private lateinit var rvOrders: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout

    private lateinit var orderAdapter: StaffOrderAdapter
    private lateinit var orderDao: OrderDao
    private lateinit var sharedPrefs: SharedPrefsHelper

    private var orderList = mutableListOf<OrderModel>()
    private var currentFilter = OrderModel.OrderStatus.PENDING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_staff_order)

        initializeViews()
        setupDatabase()
        setupRecyclerView()
        setupSpinner()
        setupButtons()
        setupSearchListener()
        loadOrders()
    }

    private fun initializeViews() {
        etSearchOrder = findViewById(R.id.et_search_order)
        spinnerOrderStatus = findViewById(R.id.spinner_order_status)
        btnPendingOrders = findViewById(R.id.btn_pending_orders)
        btnProcessingOrders = findViewById(R.id.btn_processing_orders)
        btnCompletedOrders = findViewById(R.id.btn_completed_orders)
        tvSectionTitle = findViewById(R.id.tv_section_title)
        tvOrderCount = findViewById(R.id.tv_order_count)
        rvOrders = findViewById(R.id.rv_orders)
        layoutEmptyState = findViewById(R.id.layout_empty_state)
    }

    private fun setupDatabase() {
        val dbHelper = DatabaseHelper(this)
        orderDao = OrderDao(dbHelper.writableDatabase)
        sharedPrefs = SharedPrefsHelper(this)
    }

    private fun setupRecyclerView() {
        orderAdapter = StaffOrderAdapter(
            onViewDetailsClick = { order -> showOrderDetailsDialog(order) },
            onProcessOrderClick = { order -> showProcessOrderDialog(order) },
            onContactCustomerClick = { order -> contactCustomer(order) },
            onItemClick = { order -> showOrderDetailsDialog(order) }
        )

        rvOrders.apply {
            layoutManager = LinearLayoutManager(this@StaffOrderActivity)
            adapter = orderAdapter
        }
    }

    private fun setupSpinner() {
        val statuses = resources.getStringArray(R.array.staff_order_status)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOrderStatus.adapter = adapter

        spinnerOrderStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
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
            orderList.clear()
            val staffId = sharedPrefs.getUserId()

            // Load orders by status for staff
            val orders = when (currentFilter) {
                OrderModel.OrderStatus.PENDING -> orderDao.getOrdersByStatus(OrderModel.OrderStatus.PENDING)
                OrderModel.OrderStatus.PROCESSING -> orderDao.getOrdersByStaff(staffId)
                OrderModel.OrderStatus.DELIVERED -> orderDao.getOrdersByStatus(OrderModel.OrderStatus.DELIVERED)
                else -> orderDao.getAllOrders()
            }

            orderList.addAll(orders)
            updateUI()
        } catch (e: Exception) {
            DialogUtils.showErrorDialog(this, "Lỗi khi tải danh sách đơn hàng: ${e.message}") {
                finish()
            }
        }
    }

    private fun filterOrders() {
        val query = etSearchOrder.text.toString().trim()

        try {
            val filteredList = if (query.isEmpty()) {
                orderList
            } else {
                orderDao.searchOrders(query, currentFilter.value)
            }

            orderAdapter.submitList(filteredList)
            updateOrderCount(filteredList.size)
            toggleEmptyState(filteredList.isEmpty())
        } catch (e: Exception) {
            DialogUtils.showErrorDialog(this, "Lỗi khi lọc đơn hàng: ${e.message}") {
                finish()
            }
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

    // Dialog and action methods
    private fun showOrderDetailsDialog(order: OrderModel) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_order_details)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Populate order details
        // TODO: Implement order details dialog layout and population

        dialog.show()
    }

    private fun showProcessOrderDialog(order: OrderModel) {
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
            this,
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
                DialogUtils.showSuccessDialog(this, "Cập nhật trạng thái thành công")
                loadOrders()
            } else {
                DialogUtils.showErrorDialog(this, "Không thể cập nhật trạng thái") {
                    finish()
                }
            }
        } catch (e: Exception) {
            DialogUtils.showErrorDialog(this, "Lỗi khi cập nhật: ${e.message}") {
                finish()
            }
        }
    }

    private fun contactCustomer(order: OrderModel) {
        val options = arrayOf("Gọi điện", "Gửi SMS")
        DialogUtils.showChoiceDialog(
            this,
            "Liên hệ khách hàng",
            options
        ) { selectedIndex ->
            when (selectedIndex) {
                0 -> callCustomer(order.customerPhone)
                1 -> sendSMSToCustomer(order.customerPhone)
            }
        }
    }

    private fun callCustomer(phoneNumber: String) {
        if (phoneNumber.isEmpty()) {
            DialogUtils.showErrorDialog(this, "Không có số điện thoại khách hàng") {
                finish()
            }
            return
        }

        if (!PermissionUtils.hasCallPermission(this)) {
            DialogUtils.showErrorDialog(this, "Cần cấp quyền gọi điện để thực hiện chức năng này") {
                finish()
            }
            return
        }

        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        } catch (e: Exception) {
            DialogUtils.showErrorDialog(this, "Không thể thực hiện cuộc gọi: ${e.message}") {
                finish()
            }
        }
    }

    private fun sendSMSToCustomer(phoneNumber: String) {
        if (phoneNumber.isEmpty()) {
            DialogUtils.showErrorDialog(this, "Không có số điện thoại khách hàng") {
                finish()
            }
            return
        }

        try {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("smsto:$phoneNumber")
            intent.putExtra("sms_body", "Xin chào! Chúng tôi liên hệ về đơn hàng của bạn tại StarBooks.")
            startActivity(intent)
        } catch (e: Exception) {
            DialogUtils.showErrorDialog(this, "Không thể mở ứng dụng tin nhắn: ${e.message}") {
                finish()
            }
        }
    }
}