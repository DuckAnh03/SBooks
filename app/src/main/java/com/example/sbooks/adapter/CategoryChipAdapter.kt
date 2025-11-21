package com.example.sbooks.adapter
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.models.CategoryModel

class CategoryChipAdapter(
    private val categories: List<CategoryModel>,
    private val onItemClick: (CategoryModel) -> Unit
) : RecyclerView.Adapter<CategoryChipAdapter.ChipViewHolder>() {

    var selectedPosition = -1

    inner class ChipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_chip, parent, false)
        return ChipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        val category = categories[position]
        holder.tvCategoryName.text = category.name

        // Highlight nếu được chọn
        if (position == selectedPosition) {
            holder.tvCategoryName.setBackgroundColor(Color.parseColor("#FF5722"))
        } else {
            holder.tvCategoryName.setBackgroundColor(Color.GRAY)
        }

        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onItemClick(category)
        }
    }

    override fun getItemCount() = categories.size
}
