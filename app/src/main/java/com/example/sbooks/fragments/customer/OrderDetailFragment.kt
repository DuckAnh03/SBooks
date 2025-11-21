package com.example.sbooks.fragments.customer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.sbooks.R
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.OrderDao
import com.example.sbooks.models.OrderModel
import com.example.sbooks.models.OrderItemModel
import com.example.sbooks.utils.ImageUtils
import java.text.SimpleDateFormat
import java.util.Locale

class OrderDetailFragment : Fragment() {

    private lateinit var orderDao: OrderDao
    private var orderId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_order_detail, container, false)

        // Get order ID from arguments
        orderId = arguments?.getInt("order_id", -1) ?: -1

        // Initialize database
        val dbHelper = DatabaseHelper(requireContext())
        orderDao = OrderDao(dbHelper.writableDatabase)

        // Setup views
        setupBackButton(root)
        loadOrderDetails(root)

        return root
    }

    private fun setupBackButton(root: View) {
        val btnBack = root.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadOrderDetails(root: View) {
        if (orderId == -1) {
            return
        }

        Thread {
            val order = orderDao.getOrderById(orderId)

            activity?.runOnUiThread {
                if (order != null) {
                    displayOrderDetails(root, order)
                }
            }
        }.start()
    }

    private fun displayOrderDetails(root: View, order: OrderModel) {
        // Order status and code
        val tvOrderStatus = root.findViewById<TextView>(R.id.tvOrderStatus)
        val tvOrderCode = root.findViewById<TextView>(R.id.tvOrderCode)
        val tvOrderDate = root.findViewById<TextView>(R.id.tvOrderDate)
        val tvExpectedDate = root.findViewById<TextView>(R.id.tvExpectedDate)

        tvOrderCode.text = order.orderCode

        // Format dates
        val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        tvOrderDate.text = try {
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(order.orderDate)
            dateFormat.format(date!!)
        } catch (e: Exception) {
            order.orderDate
        }

        // Set status with color
        when (order.status) {
            OrderModel.OrderStatus.PENDING -> {
                tvOrderStatus.text = "Chờ xác nhận"
                tvOrderStatus.setBackgroundColor(Color.parseColor("#FFC107"))
            }
            OrderModel.OrderStatus.PROCESSING -> {
                tvOrderStatus.text = "Đã xác nhận"
                tvOrderStatus.setBackgroundColor(Color.parseColor("#2196F3"))
            }
            OrderModel.OrderStatus.SHIPPING -> {
                tvOrderStatus.text = "Đang giao"
                tvOrderStatus.setBackgroundColor(Color.parseColor("#FF9800"))
            }
            OrderModel.OrderStatus.DELIVERED -> {
                tvOrderStatus.text = "Đã giao"
                tvOrderStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
            }
            OrderModel.OrderStatus.CANCELLED -> {
                tvOrderStatus.text = "Đã hủy"
                tvOrderStatus.setBackgroundColor(Color.parseColor("#F44336"))
            }
        }

        // Expected delivery date (5 days from order date)
        tvExpectedDate.text = try {
            val calendar = java.util.Calendar.getInstance()
            calendar.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(order.orderDate)!!
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 5)
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        } catch (e: Exception) {
            "Đang cập nhật"
        }

        // Receiver info
        val tvReceiverName = root.findViewById<TextView>(R.id.tvReceiverName)
        val tvReceiverPhone = root.findViewById<TextView>(R.id.tvReceiverPhone)
        val tvDeliveryAddress = root.findViewById<TextView>(R.id.tvDeliveryAddress)

        tvReceiverName.text = order.customerName
        tvReceiverPhone.text = order.customerPhone
        tvDeliveryAddress.text = order.customerAddress

        // Display order items
        displayOrderItems(root, order.items)

        // Payment summary
        val tvSubtotal = root.findViewById<TextView>(R.id.tvSubtotal)
        val tvShippingFee = root.findViewById<TextView>(R.id.tvShippingFee)
        val tvTotalAmount = root.findViewById<TextView>(R.id.tvTotalAmount)

        tvSubtotal.text = String.format("%,.0fđ", order.totalAmount)

        if (order.shippingFee > 0) {
            tvShippingFee.text = String.format("%,.0fđ", order.shippingFee)
            tvShippingFee.setTextColor(Color.parseColor("#1A1A1A"))
        } else {
            tvShippingFee.text = "Miễn phí"
            tvShippingFee.setTextColor(Color.parseColor("#4CAF50"))
        }

        tvTotalAmount.text = String.format("%,.0fđ", order.finalAmount)
    }

    private fun displayOrderItems(root: View, items: List<OrderItemModel>) {
        val productsContainer = root.findViewById<LinearLayout>(R.id.productsContainer)
        productsContainer.removeAllViews()

        items.forEachIndexed { index, item ->
            val itemView = createOrderItemView(item)
            productsContainer.addView(itemView)

            // Add divider except for last item
            if (index < items.size - 1) {
                val divider = View(requireContext())
                divider.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                ).apply {
                    topMargin = 12
                    bottomMargin = 12
                }
                divider.setBackgroundColor(Color.parseColor("#F0F0F0"))
                productsContainer.addView(divider)
            }
        }
    }

    private fun createOrderItemView(item: OrderItemModel): View {
        val itemView = layoutInflater.inflate(
            R.layout.item_order_detail_product,
            null,
            false
        )

        val ivBookCover = itemView.findViewById<ImageView>(R.id.iv_item_product_image)
        val tvBookTitle = itemView.findViewById<TextView>(R.id.tv_item_product_name)
        val tvPrice = itemView.findViewById<TextView>(R.id.tv_item_product_price)
        val tvQuantity = itemView.findViewById<TextView>(R.id.tv_item_product_quantity)

        tvBookTitle.text = item.bookTitle
        tvPrice.text = String.format("%,.0fđ", item.price)
        tvQuantity.text = "x${item.quantity}"

        // Load book image
        if (item.bookImage.isNotEmpty()) {
            val bitmap = ImageUtils.loadImageFromInternalStorage(item.bookImage)
            if (bitmap != null) {
                ivBookCover.setImageBitmap(bitmap)
            } else {
                ivBookCover.setImageResource(R.drawable.ic_book_24)
            }
        } else {
            ivBookCover.setImageResource(R.drawable.ic_book_24)
        }

        return itemView
    }

    companion object {
        fun newInstance(orderId: Int): OrderDetailFragment {
            val fragment = OrderDetailFragment()
            val bundle = Bundle()
            bundle.putInt("order_id", orderId)
            fragment.arguments = bundle
            return fragment
        }
    }
}