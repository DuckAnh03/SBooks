package com.example.sbooks.fragments.admin

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.adapter.UserAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.UserDao
import com.example.sbooks.models.UserModel
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.ValidationUtils

class UserManagementFragment : Fragment() {

    private companion object {
        private const val TAG = "UserManagementFragment"
    }

    // Views
    private lateinit var etSearchUser: EditText
    private lateinit var spinnerUserRole: Spinner
    private lateinit var rvUsers: RecyclerView
    private lateinit var tvUserCount: TextView
    private lateinit var layoutEmptyState: LinearLayout

    // Data
    private lateinit var userAdapter: UserAdapter
    private lateinit var userDao: UserDao
    private var userList = mutableListOf<UserModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initializeViews(view)
            setupDatabase()
            setupRecyclerView()
            setupSpinner()
            setupSearchListener()
            loadUsers()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
        }
    }

    private fun initializeViews(view: View) {
        etSearchUser = view.findViewById(R.id.et_search_user)
        spinnerUserRole = view.findViewById(R.id.spinner_user_role)
        rvUsers = view.findViewById(R.id.rv_users)
        tvUserCount = view.findViewById(R.id.tv_user_count)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)
    }

    private fun setupDatabase() {
        val dbHelper = DatabaseHelper(requireContext())
        userDao = UserDao(dbHelper.writableDatabase)
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            onEditClick = { user -> showEditUserDialog(user) },
            onDeleteClick = { user -> showDeleteUserDialog(user) },
            onItemClick = { user -> showUserDetailsDialog(user) }
        )

        rvUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }
    }

    private fun setupSpinner() {
        val roles = resources.getStringArray(R.array.user_roles)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUserRole.adapter = adapter

        spinnerUserRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterUsers()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearchListener() {
        etSearchUser.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterUsers()
            }
        })
    }

    private fun loadUsers() {
        try {
            userList.clear()
            userList.addAll(userDao.getAllUsers())
            updateUI()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading users", e)
        }
    }

    private fun filterUsers() {
        val query = etSearchUser.text.toString().trim()
        val selectedRole = when (spinnerUserRole.selectedItemPosition) {
            0 -> null // All roles
            1 -> "admin"
            2 -> "staff"
            3 -> "customer"
            else -> null
        }

        try {
            val filteredList = if (query.isEmpty() && selectedRole == null) {
                userDao.getAllUsers()
            } else {
                userDao.searchUsers(query, selectedRole)
            }

            userAdapter.submitList(filteredList)
            updateUserCount(filteredList.size)
            toggleEmptyState(filteredList.isEmpty())
        } catch (e: Exception) {
            Log.e(TAG, "Error filtering users", e)
        }
    }

    private fun updateUI() {
        userAdapter.submitList(userList)
        updateUserCount(userList.size)
        toggleEmptyState(userList.isEmpty())
    }

    private fun updateUserCount(count: Int) {
        tvUserCount.text = "$count người dùng"
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        rvUsers.visibility = if (isEmpty) View.GONE else View.VISIBLE
        layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun showEditUserDialog(user: UserModel) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_user)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etUserName = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_user_name)
        val etUserEmail = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_user_email)
        val etUserPhone = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_user_phone)
        val etUserPassword = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_user_password)
        val spinnerRole = dialog.findViewById<Spinner>(R.id.spinner_user_role_dialog)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialog.findViewById<Button>(R.id.btn_save)

        // Setup spinner
        val roles = resources.getStringArray(R.array.user_roles_dialog)
        val roleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = roleAdapter

        // Fill existing data
        tvDialogTitle.text = "Sửa người dùng"
        etUserName.setText(user.fullName)
        etUserEmail.setText(user.email)
        etUserPhone.setText(user.phone)
        etUserPassword.visibility = View.GONE // Don't show password for edit

        val rolePosition = when (user.role) {
            UserModel.UserRole.ADMIN -> 0
            UserModel.UserRole.STAFF -> 1
            UserModel.UserRole.CUSTOMER -> 2
        }
        spinnerRole.setSelection(rolePosition)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val fullName = etUserName.text.toString().trim()
            val email = etUserEmail.text.toString().trim()
            val phone = etUserPhone.text.toString().trim()
            val selectedRoleIndex = spinnerRole.selectedItemPosition

            val validation = ValidationUtils.validateUserInput(
                username = user.username,
                email = email,
                password = user.password,
                fullName = fullName,
                phone = phone
            )

            if (!validation.isValid) {
                DialogUtils.showErrorDialog(requireContext(), validation.errors.joinToString("\n")) {}
                return@setOnClickListener
            }

            val role = when (selectedRoleIndex) {
                0 -> UserModel.UserRole.ADMIN
                1 -> UserModel.UserRole.STAFF
                2 -> UserModel.UserRole.CUSTOMER
                else -> UserModel.UserRole.CUSTOMER
            }

            val updatedUser = user.copy(
                fullName = fullName,
                email = email,
                phone = phone,
                role = role
            )

            try {
                val result = userDao.updateUser(updatedUser)
                if (result > 0) {
                    DialogUtils.showToast(requireContext(), "Cập nhật người dùng thành công")
                    loadUsers()
                    dialog.dismiss()
                } else {
                    DialogUtils.showErrorDialog(requireContext(), "Không thể cập nhật người dùng") {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user", e)
                DialogUtils.showErrorDialog(requireContext(), "Lỗi khi cập nhật: ${e.message}") {}
            }
        }

        dialog.show()
    }

    private fun showDeleteUserDialog(user: UserModel) {
        DialogUtils.showConfirmDialog(
            requireContext(),
            "Xác nhận xóa",
            "Bạn có chắc chắn muốn xóa người dùng ${user.fullName.ifEmpty { user.username }}?",
            positiveAction = {
                try {
                    val result = userDao.deleteUser(user.id)
                    if (result > 0) {
                        DialogUtils.showToast(requireContext(), "Xóa người dùng thành công")
                        loadUsers()
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), "Không thể xóa người dùng") {}
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting user", e)
                    DialogUtils.showErrorDialog(requireContext(), "Lỗi khi xóa: ${e.message}") {}
                }
            }
        )
    }

    private fun showUserDetailsDialog(user: UserModel) {
        val message = buildString {
            appendLine("Tên đăng nhập: ${user.username}")
            appendLine("Họ tên: ${user.fullName}")
            appendLine("Email: ${user.email}")
            appendLine("Số điện thoại: ${user.phone}")
            appendLine("Vai trò: ${user.getDisplayRole()}")
            appendLine("Trạng thái: ${user.getDisplayStatus()}")
            appendLine("Ngày tạo: ${user.createdAt}")
        }

        DialogUtils.showInfoDialog(requireContext(), "Chi tiết người dùng", message)
    }
}