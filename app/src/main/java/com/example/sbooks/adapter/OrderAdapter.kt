package com.example.sbooks.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.models.OrderModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter(
    private val onViewDetailsClick: (OrderModel) -> Unit,
    private val onUpdateStatusClick: (OrderModel) -> Unit,
    private val onItemClick: (OrderModel) -> Unit
) : ListAdapter<OrderModel, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    // Date formatter should be initialized once for efficiency
    private val inputDateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val outputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tv_order_date)
        private val tvCustomerName: TextView = itemView.findViewById(R.id.tv_customer_name)
        private val tvOrderTotal: TextView = itemView.findViewById(R.id.tv_order_total)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_status)
        private val tvOrderItems: TextView = itemView.findViewById(R.id.tv_order_items)
        private val btnViewDetails: TextView = itemView.findViewById(R.id.btn_view_details) // Assuming this is a TextView styled as a button
        private val btnUpdateStatus: TextView = itemView.findViewById(R.id.btn_update_status) // Assuming this is a TextView styled as a button

        // Removed the nested bind function. All logic is now in this single bind function.
        fun bind(order: OrderModel) {
            tvOrderId.text = order.orderCode
            tvOrderDate.text = formatDateTime(order.orderDate) // Use the outer adapter's formatDateTime
            tvCustomerName.text = order.customerName
            tvOrderTotal.text = order.getFormattedTotal()
            tvOrderStatus.text = order.getDisplayStatus()
            tvOrderItems.text = "${order.getItemCount()} cuốn sách • ${order.getItemSummary()}"

            // Set status background color
            when (order.status) {
                OrderModel.OrderStatus.PENDING -> {
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_status_pending)
                }
                OrderModel.OrderStatus.PROCESSING -> {
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_status_processing)
                }
                OrderModel.OrderStatus.SHIPPING -> { // Corrected from PROCESSING if SHIPPING has a distinct style
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_status_processing) // Assuming you have bg_status_shipping
                }
                OrderModel.OrderStatus.DELIVERED -> {
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_status_delivered)
                }
                OrderModel.OrderStatus.CANCELLED -> {
                    tvOrderStatus.setBackgroundResource(R.drawable.bg_status_cancelled)
                }
            }

            // Click listeners
            itemView.setOnClickListener { onItemClick(order) }
            btnViewDetails.setOnClickListener { onViewDetailsClick(order) }
            btnUpdateStatus.setOnClickListener { onUpdateStatusClick(order) }
        }
    } // End of OrderViewHolder

    // Moved formatDateTime out of OrderViewHolder to be part of the OrderAdapter
    // It's generally better for utility functions like this to be at the adapter level
    // or in a separate utility class if used elsewhere.
    private fun formatDateTime(dateTimeString: String): String {
        return try {
            val date = inputDateTimeFormat.parse(dateTimeString)
            if (date != null) {
                outputDateFormat.format(date)
            } else {
                dateTimeString.split(" ").firstOrNull() ?: dateTimeString // Fallback to original simple split
            }
        } catch (e: ParseException) {
            // Handle parsing error, e.g., log it or return the original string or a part of it
            e.printStackTrace() // Log the error
            dateTimeString.split(" ").firstOrNull() ?: dateTimeString // Fallback to original simple split
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
