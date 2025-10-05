package com.example.sbooks.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.models.CategoryModel
import com.example.sbooks.utils.ImageUtils

class CategoryAdapter(
    private val onEditClick: (CategoryModel) -> Unit,
    private val onDeleteClick: (CategoryModel) -> Unit,
    private val onItemClick: (CategoryModel) -> Unit
) : ListAdapter<CategoryModel, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCategoryIcon: ImageView = itemView.findViewById(R.id.iv_category_icon)
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tv_category_name)
        private val tvCategoryDescription: TextView = itemView.findViewById(R.id.tv_category_description)
        private val tvBookCount: TextView = itemView.findViewById(R.id.tv_book_count)
        private val tvCategoryStatus: TextView = itemView.findViewById(R.id.tv_category_status)
        private val btnEditCategory: ImageButton = itemView.findViewById(R.id.btn_edit_category)
        private val btnDeleteCategory: ImageButton = itemView.findViewById(R.id.btn_delete_category)

        fun bind(category: CategoryModel) {
            tvCategoryName.text = category.name
            tvCategoryDescription.text = category.description
            tvBookCount.text = category.getBookCountText()
            tvCategoryStatus.text = category.getDisplayStatus()

            // Set status background color
            when (category.status) {
                CategoryModel.CategoryStatus.ACTIVE -> {
                    tvCategoryStatus.setBackgroundResource(R.drawable.bg_status_active)
                }
                CategoryModel.CategoryStatus.INACTIVE -> {
                    tvCategoryStatus.setBackgroundResource(R.drawable.bg_status_inactive)
                }
            }

            // Load category icon - improved logic
            loadCategoryIcon(category)

            // Click listeners
            itemView.setOnClickListener { onItemClick(category) }
            btnEditCategory.setOnClickListener { onEditClick(category) }
            btnDeleteCategory.setOnClickListener { onDeleteClick(category) }
        }

        private fun loadCategoryIcon(category: CategoryModel) {
            if (category.icon.isNotEmpty()) {
                try {
                    // Try to parse as resource ID first
                    val resourceId = category.icon.toIntOrNull()
                    if (resourceId != null && resourceId != 0) {
                        // It's a resource ID
                        ivCategoryIcon.setImageResource(resourceId)
                        return
                    }

                    // Try to load as file path
                    val bitmap = ImageUtils.loadImageFromInternalStorage(category.icon)
                    if (bitmap != null) {
                        ivCategoryIcon.setImageBitmap(bitmap)
                        return
                    }
                } catch (e: Exception) {
                    // If parsing fails, fall through to default
                }
            }

            // Use default icon based on category name or generic icon
            val defaultIcon = getDefaultIconForCategory(category.name)
            ivCategoryIcon.setImageResource(defaultIcon)
        }

        private fun getDefaultIconForCategory(categoryName: String): Int {
            return when (categoryName.lowercase().trim()) {
                "văn học", "van hoc", "literature" -> R.drawable.ic_book
                "khoa học", "khoa hoc", "science" -> R.drawable.ic_category
                "nghệ thuật", "nghe thuat", "art" -> R.drawable.ic_category
                "kinh tế", "kinh te", "economics" -> R.drawable.ic_category
                "lịch sử", "lich su", "history" -> R.drawable.ic_book
                "tâm lý", "tam ly", "psychology" -> R.drawable.ic_category
                "giáo dục", "giao duc", "education" -> R.drawable.ic_book
                "y học", "y hoc", "medicine" -> R.drawable.ic_category
                else -> R.drawable.ic_category
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryModel>() {
        override fun areItemsTheSame(oldItem: CategoryModel, newItem: CategoryModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CategoryModel, newItem: CategoryModel): Boolean {
            return oldItem == newItem
        }
    }
}