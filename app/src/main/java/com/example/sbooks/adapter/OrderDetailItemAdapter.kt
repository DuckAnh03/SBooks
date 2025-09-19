package com.example.sbooks.adapter // Or your adapter package

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.models.OrderItemModel // Assuming you have this model
// If OrderItemModel doesn't have image URL, you might need BookModel or similar
// import com.bumptech.glide.Glide // For image loading

class OrderDetailItemAdapter(private val items: List<OrderItemModel>) :
    RecyclerView.Adapter<OrderDetailItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.iv_item_product_image)
        private val tvProductName: TextView = itemView.findViewById(R.id.tv_item_product_name)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tv_item_product_price)
        private val tvProductQuantity: TextView = itemView.findViewById(R.id.tv_item_product_quantity)

        fun bind(item: OrderItemModel) {
            tvProductName.text = item.bookTitle // Assuming bookTitle is in OrderItemModel
            tvProductPrice.text = item.getFormattedPrice() // Assuming a method to format price
            tvProductQuantity.text = "x ${item.quantity}"

            // TODO: Load image using Glide or Picasso if you have image URLs
            // Example with Glide:
            // if (item.bookImageUrl.isNotEmpty()) {
            //     Glide.with(itemView.context)
            //         .load(item.bookImageUrl)
            //         .placeholder(R.drawable.ic_book) // Placeholder
            //         .error(R.drawable.ic_book)       // Error placeholder
            //         .into(ivProductImage)
            // } else {
            //     ivProductImage.setImageResource(R.drawable.ic_book)
            // }
            ivProductImage.setImageResource(R.drawable.ic_book) // Placeholder
        }
    }
}
