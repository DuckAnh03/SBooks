package com.example.sbooks.activities.admin

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.adapter.UserAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.UserDao
import com.example.sbooks.models.UserModel
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.ValidationUtils

class UserManagementActivity : AppCompatActivity() {

    private lateinit var etSearchUser: EditText
    private lateinit var spinnerUserRole: Spinner
    private lateinit var rvUsers: RecyclerView
    private lateinit var tvUserCount: TextView
    private lateinit var layoutEmptyState: LinearLayout

    private lateinit var userAdapter: UserAdapter
    private lateinit var userDao: UserDao
    private var userList = mutableListOf<UserModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_user_management)

        initializeViews()
        setupDatabase()
        setupRecyclerView()
        setupSpinner()
        setupSearchListener()
        loadUsers()
    }

    private fun initializeViews() {
        etSearchUser = findViewById(R.id.et_search_user)
        spinnerUserRole = findViewById(R.id.spinner_user_role)
        rvUsers = findViewById(R.id.rv_users)
        tvUserCount = findViewById(R.id.tv_user_count)
        layoutEmptyState = findViewById(R.id.layout_empty_state)
    }

    private fun setupDatabase() {
        val dbHelper = DatabaseHelper(this)
        userDao = UserDao(dbHelper.writableDatabase)
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            onEditClick = { user -> showEditUserDialog(user) },
            onDeleteClick = { user -> showDeleteUserDialog(user) },
            onItemClick = { user -> showUserDetailsDialog(user) }
        )

        rvUsers.apply {
            layoutManager = LinearLayoutManager(this@UserManagementActivity)
            adapter = userAdapter
        }
    }

    private fun setupSpinner() {
        val roles = resources.getStringArray(R.array.user_roles)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
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
            DialogUtils.showErrorDialog(this, "Lỗi khi tải danh sách người dùng: ${e.message}") {
                finish()
            }
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
            DialogUtils.showErrorDialog(this, "Lỗi khi lọc người dùng: ${e.message}") {
                finish()
            }
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

    // Dialog methods
    private fun showEditUserDialog(user: UserModel) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_user)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etUserName = dialog.findViewById<EditText>(R.id.et_user_name)
        val etUserEmail = dialog.findViewById<EditText>(R.id.et_user_email)
        val etUserPhone = dialog.findViewById<EditText>(R.id.et_user_phone)
        val etUserPassword = dialog.findViewById<EditText>(R.id.et_user_password)
        val spinnerRole = dialog.findViewById<Spinner>(R.id.spinner_user_role_dialog)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialog.findViewById<Button>(R.id.btn_save)

        // Setup spinner
        val roles = resources.getStringArray(R.array.user_roles_dialog)
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = roleAdapter

        // Fill existing data
        tvDialogTitle.text = getString(R.string.edit_user)
        etUserName.setText(user.fullName)
        etUserEmail.setText(user.email)
        etUserPhone.setText(user.phone)
        etUserPassword.visibility = View.GONE // Don't show password for edit

        // Set role selection
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
                username = user.username, // Keep original username
                email = email,
                password = user.password, // Keep original password
                fullName = fullName,
                phone = phone
            )

            if (!validation.isValid) {
                DialogUtils.showErrorDialog(this, validation.errors.joinToString("\n")) {
                    finish()
                }
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
                    DialogUtils.showSuccessDialog(this, "Cập nhật người dùng thành công")
                    loadUsers()
                    dialog.dismiss()
                } else {
                    DialogUtils.showErrorDialog(this, "Không thể cập nhật người dùng") {
                        finish()
                    }
                }
            } catch (e: Exception) {
                DialogUtils.showErrorDialog(this, "Lỗi khi cập nhật: ${e.message}") {
                    finish()
                }
            }
        }

        dialog.show()
    }

    private fun showDeleteUserDialog(user: UserModel) {
        DialogUtils.showDeleteConfirmDialog(
            this,
            user.fullName.ifEmpty { user.username }
        ) {
            try {
                val result = userDao.deleteUser(user.id)
                if (result > 0) {
                    DialogUtils.showSuccessDialog(this, "Xóa người dùng thành công")
                    loadUsers()
                } else {
                    DialogUtils.showErrorDialog(this, "Không thể xóa người dùng") {
                        finish()
                    }
                }
            } catch (e: Exception) {
                DialogUtils.showErrorDialog(this, "Lỗi khi xóa: ${e.message}") {
                    finish()
                }
            }
        }
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

        DialogUtils.showInfoDialog(this, "Chi tiết người dùng", message)
    }

    fun addNewUser(view: View) {
        showAddUserDialog()
    }

    private fun showAddUserDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_user)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val etUserName = dialog.findViewById<EditText>(R.id.et_user_name)
        val etUserEmail = dialog.findViewById<EditText>(R.id.et_user_email)
        val etUserPhone = dialog.findViewById<EditText>(R.id.et_user_phone)
        val etUserPassword = dialog.findViewById<EditText>(R.id.et_user_password)
        val spinnerRole = dialog.findViewById<Spinner>(R.id.spinner_user_role_dialog)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialog.findViewById<Button>(R.id.btn_save)

        // Setup spinner
        val roles = resources.getStringArray(R.array.user_roles_dialog)
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = roleAdapter

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val fullName = etUserName.text.toString().trim()
            val email = etUserEmail.text.toString().trim()
            val phone = etUserPhone.text.toString().trim()
            val password = etUserPassword.text.toString().trim()
            val username = email.substringBefore("@") // Generate username from email
            val selectedRoleIndex = spinnerRole.selectedItemPosition

            val validation = ValidationUtils.validateUserInput(
                username = username,
                email = email,
                password = password,
                fullName = fullName,
                phone = phone
            )

            if (!validation.isValid) {
                DialogUtils.showErrorDialog(this, validation.errors.joinToString("\n")) {
                    finish()
                }
                return@setOnClickListener
            }

            // Check if email already exists
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                DialogUtils.showErrorDialog(this, "Email đã được sử dụng") {
                    finish()
                }
                return@setOnClickListener
            }

            val role = when (selectedRoleIndex) {
                0 -> UserModel.UserRole.ADMIN
                1 -> UserModel.UserRole.STAFF
                2 -> UserModel.UserRole.CUSTOMER
                else -> UserModel.UserRole.CUSTOMER
            }

            val newUser = UserModel(
                username = username,
                email = email,
                phone = phone,
                fullName = fullName,
                password = password,
                role = role,
                status = UserModel.UserStatus.ACTIVE
            )

            try {
                val result = userDao.insertUser(newUser)
                if (result > 0) {
                    DialogUtils.showSuccessDialog(this, "Thêm người dùng thành công")
                    loadUsers()
                    dialog.dismiss()
                } else {
                    DialogUtils.showErrorDialog(this, "Không thể thêm người dùng") {
                        finish()
                    }
                }
            } catch (e: Exception) {
                DialogUtils.showErrorDialog(this, "Lỗi khi thêm: ${e.message}") {
                    finish()
                }
            }
        }

        dialog.show()
    }
}