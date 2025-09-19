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

                // Set category icon (placeholder for now)
                ivCategoryIcon.setImageResource(R.drawable.ic_category)

                // Click listeners
                itemView.setOnClickListener { onItemClick(category) }
                btnEditCategory.setOnClickListener { onEditClick(category) }
                btnDeleteCategory.setOnClickListener { onDeleteClick(category) }
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