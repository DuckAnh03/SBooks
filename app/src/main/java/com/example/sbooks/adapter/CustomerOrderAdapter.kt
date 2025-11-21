package com.example.sbooks.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.models.OrderModel
import java.text.SimpleDateFormat
import java.util.*

class CustomerOrderAdapter(
    private val onOrderClick: (OrderModel) -> Unit
) : ListAdapter<OrderModel, CustomerOrderAdapter.OrderViewHolder>(OrderDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_list, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView as CardView
        private val tvOrderCode: TextView = itemView.findViewById(R.id.tv_order_code)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_status)
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tv_order_date)
        private val tvBookCount: TextView = itemView.findViewById(R.id.tv_book_count)
        private val tvTotalAmount: TextView = itemView.findViewById(R.id.tv_total_amount)
        private val btnAction: Button = itemView.findViewById(R.id.btn_action)

        fun bind(order: OrderModel) {
            tvOrderCode.text = "Mã đơn: ${order.orderCode}"
            tvOrderDate.text = "Ngày đặt: ${formatDate(order.orderDate)}"

            val totalBooks = order.items.sumOf { it.quantity }
            tvBookCount.text = "$totalBooks cuốn sách"
            tvTotalAmount.text = String.format("%,.0fđ", order.finalAmount)

            // Set status
            when (order.status) {
                OrderModel.OrderStatus.PENDING -> {
                    tvOrderStatus.text = "Chờ xác nhận"
                    tvOrderStatus.setBackgroundColor(Color.parseColor("#FFC107"))
                    btnAction.text = "Hủy đơn"
                    btnAction.setBackgroundColor(Color.parseColor("#F44336"))
                }
                OrderModel.OrderStatus.SHIPPING -> {
                    tvOrderStatus.text = "Đang giao"
                    tvOrderStatus.setBackgroundColor(Color.parseColor("#2196F3"))
                    btnAction.text = "Theo dõi"
                    btnAction.setBackgroundColor(Color.parseColor("#FF5722"))
                }
                OrderModel.OrderStatus.PROCESSING -> {
                    tvOrderStatus.text = "Đang xử lý"
                    tvOrderStatus.setBackgroundColor(Color.parseColor("#FF9800"))
                    btnAction.text = "Theo dõi"
                    btnAction.setBackgroundColor(Color.parseColor("#FF5722"))
                }
                OrderModel.OrderStatus.DELIVERED -> {
                    tvOrderStatus.text = "Hoàn tất"
                    tvOrderStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
                    btnAction.text = "Đánh giá"
                    btnAction.setBackgroundColor(Color.parseColor("#4CAF50"))
                }
                OrderModel.OrderStatus.CANCELLED -> {
                    tvOrderStatus.text = "Đã hủy"
                    tvOrderStatus.setBackgroundColor(Color.parseColor("#F44336"))
                    tvTotalAmount.setTextColor(Color.parseColor("#999999"))
                    btnAction.text = "Đặt lại"
                    btnAction.setBackgroundColor(Color.parseColor("#666666"))
                }
            }

            cardView.setOnClickListener { onOrderClick(order) }
            btnAction.setOnClickListener { onOrderClick(order) }
        }

        private fun formatDate(dateStr: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateStr)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateStr
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<OrderModel>() {
        override fun areItemsTheSame(oldItem: OrderModel, newItem: OrderModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OrderModel, newItem: OrderModel): Boolean {
            return oldItem == newItem
        }
    }
}