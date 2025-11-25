package com.example.sbooks.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil // Import DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.models.OrderModel
import com.example.sbooks.utils.ImageUtils

class StaffOrderAdapter(
    private val onViewDetailsClick: (OrderModel) -> Unit,
    private val onProcessOrderClick: (OrderModel) -> Unit,
    private val onContactCustomerClick: (OrderModel) -> Unit,
    private val onItemClick: (OrderModel) -> Unit
) : ListAdapter<OrderModel, StaffOrderAdapter.StaffOrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffOrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_staff_order, parent, false)
        return StaffOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: StaffOrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StaffOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ... your ViewHolder code ...
        private val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tv_order_date)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_status)
        private val tvOrderTotal: TextView = itemView.findViewById(R.id.tv_order_total)
        private val tvCustomerName: TextView = itemView.findViewById(R.id.tv_customer_name)
        private val tvCustomerAddress: TextView = itemView.findViewById(R.id.tv_customer_address)
        private val tvCustomerPhone: TextView = itemView.findViewById(R.id.tv_customer_phone)
        private val tvOrderItems: TextView = itemView.findViewById(R.id.tv_order_items)
        private val tvPriorityBadge: TextView = itemView.findViewById(R.id.tv_priority_badge)
        private val btnViewDetails: TextView = itemView.findViewById(R.id.btn_view_details)
        private val btnProcessOrder: TextView = itemView.findViewById(R.id.btn_processing_orders)
        private val btnContactCustomer: TextView = itemView.findViewById(R.id.btn_contact_customer)
        private val iv_cusomter_avatar: ImageView = itemView.findViewById(R.id.iv_customer_avatar)


        fun bind(order: OrderModel) {
            tvOrderId.text = order.orderCode
            tvOrderDate.text = formatDateTime(order.orderDate)
            tvOrderStatus.text = order.getDisplayStatus()
            tvOrderTotal.text = order.getFormattedTotal()
            tvCustomerName.text = order.customerName
            tvCustomerAddress.text = order.customerAddress
            tvCustomerPhone.text = order.customerPhone
            tvOrderItems.text = "${order.getItemCount()} cuốn sách • ${order.getItemSummary()}"

            if (order.customerAvatar.isNotEmpty()) {
                val bitmap = ImageUtils.loadImageFromInternalStorage(order.customerAvatar)
                if (bitmap != null) {
                    iv_cusomter_avatar.setImageBitmap(bitmap)
                }
            }

            val isUrgent = order.isPending() && isOrderUrgent(order.orderDate)
            tvPriorityBadge.visibility = if (isUrgent) View.VISIBLE else View.GONE

            when (order.status) {
                OrderModel.OrderStatus.PENDING -> {
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_status_pending)
                    btnProcessOrder.text = "Xử lý"
                    btnProcessOrder.visibility = View.VISIBLE
                }
                OrderModel.OrderStatus.PROCESSING -> {
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_status_processing)
                    btnProcessOrder.text = "Hoàn thành"
                    btnProcessOrder.visibility = View.VISIBLE
                }
                OrderModel.OrderStatus.SHIPPING -> {
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_status_processing) // Consider bg_status_shipping
                    btnProcessOrder.text = "Đã giao"
                    btnProcessOrder.visibility = View.VISIBLE
                }
                OrderModel.OrderStatus.DELIVERED, OrderModel.OrderStatus.CANCELLED -> {
                    tvOrderStatus.setBackgroundResource(
                        if (order.status == OrderModel.OrderStatus.DELIVERED) R.drawable.bg_status_delivered
                        else R.drawable.bg_status_cancelled
                    )
                    btnProcessOrder.visibility = View.GONE
                }
            }

            itemView.setOnClickListener { onItemClick(order) }
            btnViewDetails.setOnClickListener { onViewDetailsClick(order) }
            btnProcessOrder.setOnClickListener { onProcessOrderClick(order) }
            btnContactCustomer.setOnClickListener { onContactCustomerClick(order) }
        }

        private fun formatDateTime(dateTime: String): String {
            // Consider using SimpleDateFormat for more robust date parsing/formatting
            return dateTime.split(" ").firstOrNull()?.replace("-", "/") ?: dateTime
        }

        private fun isOrderUrgent(orderDate: String): Boolean {
            // Implement proper date comparison logic here
            // This is placeholder logic
            // Example: Parse orderDate and compare with current date
            // For now, returning true to show the badge for testing if isPending is also true
            return true
        }
    }

    // --- ADD THIS CLASS ---
    private class OrderDiffCallback : DiffUtil.ItemCallback<OrderModel>() {
        override fun areItemsTheSame(oldItem: OrderModel, newItem: OrderModel): Boolean {
            return oldItem.id == newItem.id // Assuming OrderModel has an 'id' property
        }

        override fun areContentsTheSame(oldItem: OrderModel, newItem: OrderModel): Boolean {
            return oldItem == newItem // Relies on OrderModel being a data class or having a proper equals()
        }
    }
    // --- END OF ADDED CLASS ---
}
