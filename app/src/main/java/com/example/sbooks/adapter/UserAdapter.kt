package com.example.sbooks.adapter

import android.util.Log
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
import com.example.sbooks.models.UserModel
import com.example.sbooks.utils.ImageUtils

class UserAdapter(
    private val onEditClick: (UserModel) -> Unit,
    private val onDeleteClick: (UserModel) -> Unit,
    private val onItemClick: (UserModel) -> Unit
) : ListAdapter<UserModel, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    private companion object {
        private const val TAG = "UserAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        Log.d(TAG, "onCreateViewHolder called")
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        Log.d(TAG, "onBindViewHolder called for position: $position, user: ${user.username}, fullName: ${user.fullName}")
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        Log.d(TAG, "getItemCount: $count")
        return count
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivUserAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        private val tvUserName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val tvUserEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        private val tvUserRole: TextView = itemView.findViewById(R.id.tv_user_role)
        private val tvUserStatus: TextView = itemView.findViewById(R.id.tv_user_status)
        private val btnEditUser: ImageButton = itemView.findViewById(R.id.btn_edit_user)
        private val btnDeleteUser: ImageButton = itemView.findViewById(R.id.btn_delete_user)

        init {
            Log.d(TAG, "UserViewHolder initialized")
            // Check if all views are found
            Log.d(TAG, "Views found - Name: ${tvUserName != null}, Email: ${tvUserEmail != null}, Role: ${tvUserRole != null}")
        }

        fun bind(user: UserModel) {
            Log.d(TAG, "Binding user: ${user.username}")

            try {
                // Set user name - prefer fullName, fallback to username
                val displayName = if (user.fullName.isNotBlank()) user.fullName else user.username
                tvUserName.text = displayName
                Log.d(TAG, "Set name: $displayName")

                // Set email
                tvUserEmail.text = user.email
                Log.d(TAG, "Set email: ${user.email}")

                // Set role
                val roleText = user.getDisplayRole()
                tvUserRole.text = roleText
                Log.d(TAG, "Set role: $roleText")

                // Set status
                val statusText = user.getDisplayStatus()
                tvUserStatus.text = statusText
                Log.d(TAG, "Set status: $statusText")

                // Set role background color
                when (user.role) {
                    UserModel.UserRole.ADMIN -> {
                        tvUserRole.setBackgroundResource(R.drawable.bg_role_admin)
                        Log.d(TAG, "Set admin role background")
                    }
                    UserModel.UserRole.STAFF -> {
                        tvUserRole.setBackgroundResource(R.drawable.bg_role_staff)
                        Log.d(TAG, "Set staff role background")
                    }
                    UserModel.UserRole.CUSTOMER -> {
                        tvUserRole.setBackgroundResource(R.drawable.bg_role_customer)
                        Log.d(TAG, "Set customer role background")
                    }
                }

                // Set status background color
                when (user.status) {
                    UserModel.UserStatus.ACTIVE -> {
                        tvUserStatus.setBackgroundResource(R.drawable.bg_status_active)
                        Log.d(TAG, "Set active status background")
                    }
                    UserModel.UserStatus.INACTIVE -> {
                        tvUserStatus.setBackgroundResource(R.drawable.bg_status_inactive)
                        Log.d(TAG, "Set inactive status background")
                    }
                    UserModel.UserStatus.SUSPENDED -> {
                        tvUserStatus.setBackgroundResource(R.drawable.bg_status_inactive)
                        Log.d(TAG, "Set suspended status background")
                    }
                }

                // Set avatar - load from storage if available, otherwise use default
                if (user.avatar.isNotEmpty()) {
                    val bitmap = ImageUtils.loadImageFromInternalStorage(user.avatar)
                    if (bitmap != null) {
                        ivUserAvatar.setImageBitmap(bitmap)
                        Log.d(TAG, "Set user avatar from storage: ${user.avatar}")
                    } else {
                        // If loading fails, use default
                        ivUserAvatar.setImageResource(R.drawable.ic_users)
                        Log.w(TAG, "Failed to load avatar from: ${user.avatar}")
                    }
                } else {
                    // Use default avatar
                    ivUserAvatar.setImageResource(R.drawable.ic_users)
                    Log.d(TAG, "Using default avatar")
                }

                // Click listeners
                itemView.setOnClickListener {
                    Log.d(TAG, "Item clicked: ${user.username}")
                    onItemClick(user)
                }
                btnEditUser.setOnClickListener {
                    Log.d(TAG, "Edit clicked: ${user.username}")
                    onEditClick(user)
                }
                btnDeleteUser.setOnClickListener {
                    Log.d(TAG, "Delete clicked: ${user.username}")
                    onDeleteClick(user)
                }

                Log.d(TAG, "Successfully bound user: ${user.username}")

            } catch (e: Exception) {
                Log.e(TAG, "Error binding user: ${user.username}", e)
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UserModel>() {
        override fun areItemsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            val same = oldItem.id == newItem.id
            Log.d("UserDiffCallback", "areItemsTheSame: $same (${oldItem.id} == ${newItem.id})")
            return same
        }

        override fun areContentsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            val same = oldItem == newItem
            Log.d("UserDiffCallback", "areContentsTheSame: $same")
            return same
        }
    }
}