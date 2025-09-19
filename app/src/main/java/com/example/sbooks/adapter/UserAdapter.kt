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
import com.example.sbooks.models.UserModel

class UserAdapter(
    private val onEditClick: (UserModel) -> Unit,
    private val onDeleteClick: (UserModel) -> Unit,
    private val onItemClick: (UserModel) -> Unit
) : ListAdapter<UserModel, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivUserAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        private val tvUserName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val tvUserEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        private val tvUserRole: TextView = itemView.findViewById(R.id.tv_user_role)
        private val tvUserStatus: TextView = itemView.findViewById(R.id.tv_user_status)
        private val btnEditUser: ImageButton = itemView.findViewById(R.id.btn_edit_user)
        private val btnDeleteUser: ImageButton = itemView.findViewById(R.id.btn_delete_user)

        fun bind(user: UserModel) {
            tvUserName.text = user.fullName.ifEmpty { user.username }
            tvUserEmail.text = user.email
            tvUserRole.text = user.getDisplayRole()
            tvUserStatus.text = user.getDisplayStatus()

            // Set role background color
            when (user.role) {
                UserModel.UserRole.ADMIN -> tvUserRole.setBackgroundResource(R.drawable.bg_role_admin)
                UserModel.UserRole.STAFF -> tvUserRole.setBackgroundResource(R.drawable.bg_role_staff)
                UserModel.UserRole.CUSTOMER -> tvUserRole.setBackgroundResource(R.drawable.bg_role_customer)
            }

            // Set status background color
            when (user.status) {
                UserModel.UserStatus.ACTIVE -> tvUserStatus.setBackgroundResource(R.drawable.bg_status_active)
                UserModel.UserStatus.INACTIVE -> tvUserStatus.setBackgroundResource(R.drawable.bg_status_inactive)
                UserModel.UserStatus.SUSPENDED -> tvUserStatus.setBackgroundResource(R.drawable.bg_status_inactive)
            }

            // Set avatar (placeholder for now)
            ivUserAvatar.setImageResource(R.drawable.ic_users)

            // Click listeners
            itemView.setOnClickListener { onItemClick(user) }
            btnEditUser.setOnClickListener { onEditClick(user) }
            btnDeleteUser.setOnClickListener { onDeleteClick(user) }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UserModel>() {
        override fun areItemsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem == newItem
        }
    }
}