package com.example.sbooks.fragments.admin

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.adapter.UserAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.UserDao
import com.example.sbooks.models.UserModel
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.ImageUtils
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

    // Image handling
    private var selectedImageUri: Uri? = null
    private var currentDialog: Dialog? = null
    private var currentUserForEdit: UserModel? = null

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            currentDialog?.findViewById<ImageView>(R.id.iv_user_avatar_preview)?.let { imageView ->
                ImageUtils.loadSelectedImageToView(requireContext(), uri, imageView)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_user_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

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
        Log.d(TAG, "initializeViews called")
        etSearchUser = view.findViewById(R.id.et_search_user)
        spinnerUserRole = view.findViewById(R.id.spinner_user_role)
        rvUsers = view.findViewById(R.id.rv_users)
        tvUserCount = view.findViewById(R.id.tv_user_count)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)

        Log.d(TAG, "All views initialized successfully")
    }

    private fun setupDatabase() {
        Log.d(TAG, "setupDatabase called")
        val dbHelper = DatabaseHelper(requireContext())
        userDao = UserDao(dbHelper.writableDatabase)
        Log.d(TAG, "Database setup complete")
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView called")
        userAdapter = UserAdapter(
            onEditClick = { user ->
                Log.d(TAG, "Edit click for user: ${user.username}")
                showEditUserDialog(user)
            },
            onDeleteClick = { user ->
                Log.d(TAG, "Delete click for user: ${user.username}")
                showDeleteUserDialog(user)
            },
            onItemClick = { user ->
                Log.d(TAG, "Item click for user: ${user.username}")
                showUserDetailsDialog(user)
            }
        )

        rvUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }

        Log.d(TAG, "RecyclerView setup complete")
    }

    private fun setupSpinner() {
        Log.d(TAG, "setupSpinner called")
        val roles = arrayOf("Tất cả vai trò", "Quản trị viên", "Nhân viên", "Khách hàng")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUserRole.adapter = adapter

        spinnerUserRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.d(TAG, "Spinner item selected: $position")
                filterUsers()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        Log.d(TAG, "Spinner setup complete")
    }

    private fun setupSearchListener() {
        Log.d(TAG, "setupSearchListener called")
        etSearchUser.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                Log.d(TAG, "Search text changed: ${s.toString()}")
                filterUsers()
            }
        })
    }

    private fun loadUsers() {
        Log.d(TAG, "loadUsers called")
        try {
            userList.clear()
            val allUsers = userDao.getAllUsers()
            userList.addAll(allUsers)

            Log.d(TAG, "Loaded ${userList.size} users from database")

            // Debug: Print all users
            userList.forEachIndexed { index, user ->
                Log.d(TAG, "User $index: id=${user.id}, username=${user.username}, fullName=${user.fullName}, email=${user.email}, role=${user.role}")
            }

            // Update UI directly after loading
            updateUI()

        } catch (e: Exception) {
            Log.e(TAG, "Error loading users", e)
        }
    }

    private fun filterUsers() {
        Log.d(TAG, "filterUsers called")
        val query = etSearchUser.text.toString().trim()
        val selectedRole = when (spinnerUserRole.selectedItemPosition) {
            0 -> null // All roles
            1 -> "admin"
            2 -> "staff"
            3 -> "customer"
            else -> null
        }

        Log.d(TAG, "Filter parameters - query: '$query', role: $selectedRole")

        try {
            val filteredList = if (query.isEmpty() && selectedRole == null) {
                // Show all users from our loaded list
                Log.d(TAG, "Showing all users")
                userList.toList()
            } else {
                // Use database search for filtered results
                Log.d(TAG, "Searching with filters")
                userDao.searchUsers(query, selectedRole)
            }

            Log.d(TAG, "Filtered list size: ${filteredList.size}")

            // Debug: Print filtered users
            filteredList.forEachIndexed { index, user ->
                Log.d(TAG, "Filtered User $index: ${user.username} - ${user.fullName}")
            }

            // Submit the filtered list to adapter
            userAdapter.submitList(null) // Clear first
            userAdapter.submitList(filteredList.toList()) // Then set new list
            updateUserCount(filteredList.size)
            toggleEmptyState(filteredList.isEmpty())
        } catch (e: Exception) {
            Log.e(TAG, "Error filtering users", e)
            // Fallback to showing all users if filtering fails
            userAdapter.submitList(null)
            userAdapter.submitList(userList.toList())
            updateUserCount(userList.size)
            toggleEmptyState(userList.isEmpty())
        }
    }

    private fun updateUI() {
        Log.d(TAG, "updateUI called with ${userList.size} users")
        userAdapter.submitList(null) // Clear first
        userAdapter.submitList(userList.toList()) // Then set new list
        updateUserCount(userList.size)
        toggleEmptyState(userList.isEmpty())
    }

    private fun updateUserCount(count: Int) {
        Log.d(TAG, "updateUserCount: $count")
        tvUserCount.text = "$count người dùng"
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        Log.d(TAG, "toggleEmptyState: $isEmpty")
        rvUsers.visibility = if (isEmpty) View.GONE else View.VISIBLE
        layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    // FAB function to show add user dialog
    fun showAddUserDialog() {
        Log.d(TAG, "showAddUserDialog called")
        currentUserForEdit = null
        selectedImageUri = null
        showUserDialog(null)
    }

    private fun showUserDialog(user: UserModel?) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_user)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        currentDialog = dialog

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etUserName = dialog.findViewById<EditText>(R.id.et_user_name)
        val etUsername = dialog.findViewById<EditText>(R.id.et_username)
        val etUserEmail = dialog.findViewById<EditText>(R.id.et_user_email)
        val etUserPhone = dialog.findViewById<EditText>(R.id.et_user_phone)
        val etUserPassword = dialog.findViewById<EditText>(R.id.et_user_password)
        val etUserAddress = dialog.findViewById<EditText>(R.id.et_user_address)
        val spinnerRole = dialog.findViewById<Spinner>(R.id.spinner_user_role_dialog)
        val spinnerStatus = dialog.findViewById<Spinner>(R.id.spinner_user_status_dialog)
        val ivUserAvatar = dialog.findViewById<ImageView>(R.id.iv_user_avatar_preview)
        val btnSelectAvatar = dialog.findViewById<Button>(R.id.btn_select_avatar)
        val btnChangePassword = dialog.findViewById<Button>(R.id.btn_change_password)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialog.findViewById<Button>(R.id.btn_save)

        // Setup role spinner
        val roles = arrayOf("Quản trị viên", "Nhân viên", "Khách hàng")
        val roleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = roleAdapter

        // Setup status spinner
        val statuses = arrayOf("Hoạt động", "Không hoạt động", "Tạm khóa")
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        // Configure dialog for add or edit
        if (user == null) {
            // Add mode
            tvDialogTitle.text = "Thêm người dùng"
            btnChangePassword.visibility = View.GONE
            etUserPassword.visibility = View.VISIBLE
        } else {
            // Edit mode
            tvDialogTitle.text = "Sửa người dùng"
            currentUserForEdit = user

            etUserName.setText(user.fullName)
            etUsername.setText(user.username)
            etUsername.isEnabled = false // Don't allow username changes
            etUserEmail.setText(user.email)
            etUserPhone.setText(user.phone)
            etUserAddress.setText(user.address)
            etUserPassword.visibility = View.GONE
            btnChangePassword.visibility = View.VISIBLE

            // Set role selection
            val rolePosition = when (user.role) {
                UserModel.UserRole.ADMIN -> 0
                UserModel.UserRole.STAFF -> 1
                UserModel.UserRole.CUSTOMER -> 2
            }
            spinnerRole.setSelection(rolePosition)

            // Set status selection
            val statusPosition = when (user.status) {
                UserModel.UserStatus.ACTIVE -> 0
                UserModel.UserStatus.INACTIVE -> 1
                UserModel.UserStatus.SUSPENDED -> 2
            }
            spinnerStatus.setSelection(statusPosition)

            // Load existing avatar if available
            if (user.avatar.isNotEmpty()) {
                val bitmap = ImageUtils.loadImageFromInternalStorage(user.avatar)
                if (bitmap != null) {
                    ivUserAvatar.setImageBitmap(bitmap)
                }
            }
        }

        // Avatar selection
        btnSelectAvatar.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Change password button
        btnChangePassword.setOnClickListener {
            user?.let { showChangePasswordDialog(it) }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            if (user == null) {
                handleAddUser(dialog)
            } else {
                handleEditUser(dialog, user)
            }
        }

        dialog.show()
    }

    private fun showEditUserDialog(user: UserModel) {
        showUserDialog(user)
    }

    private fun showChangePasswordDialog(user: UserModel) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_change_password)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val etCurrentPassword = dialog.findViewById<EditText>(R.id.et_current_password)
        val etNewPassword = dialog.findViewById<EditText>(R.id.et_new_password)
        val etConfirmPassword = dialog.findViewById<EditText>(R.id.et_confirm_password)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialog.findViewById<Button>(R.id.btn_save)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            when {
                currentPassword != user.password -> {
                    DialogUtils.showErrorDialog(requireContext(), "Mật khẩu hiện tại không đúng") {}
                }
                newPassword.length < 6 -> {
                    DialogUtils.showErrorDialog(requireContext(), "Mật khẩu mới phải có ít nhất 6 ký tự") {}
                }
                newPassword != confirmPassword -> {
                    DialogUtils.showErrorDialog(requireContext(), "Xác nhận mật khẩu không khớp") {}
                }
                else -> {
                    val updatedUser = user.copy(password = newPassword)
                    try {
                        val result = userDao.updateUser(updatedUser)
                        if (result > 0) {
                            DialogUtils.showToast(requireContext(), "Đổi mật khẩu thành công")
                            dialog.dismiss()
                        } else {
                            DialogUtils.showErrorDialog(requireContext(), "Không thể đổi mật khẩu") {}
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error changing password", e)
                        DialogUtils.showErrorDialog(requireContext(), "Lỗi khi đổi mật khẩu: ${e.message}") {}
                    }
                }
            }
        }

        dialog.show()
    }

    private fun handleAddUser(dialog: Dialog) {
        val etUserName = dialog.findViewById<EditText>(R.id.et_user_name)
        val etUsername = dialog.findViewById<EditText>(R.id.et_username)
        val etUserEmail = dialog.findViewById<EditText>(R.id.et_user_email)
        val etUserPhone = dialog.findViewById<EditText>(R.id.et_user_phone)
        val etUserPassword = dialog.findViewById<EditText>(R.id.et_user_password)
        val etUserAddress = dialog.findViewById<EditText>(R.id.et_user_address)
        val spinnerRole = dialog.findViewById<Spinner>(R.id.spinner_user_role_dialog)
        val spinnerStatus = dialog.findViewById<Spinner>(R.id.spinner_user_status_dialog)

        val fullName = etUserName.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val email = etUserEmail.text.toString().trim()
        val phone = etUserPhone.text.toString().trim()
        val password = etUserPassword.text.toString().trim()
        val address = etUserAddress.text.toString().trim()
        val selectedRoleIndex = spinnerRole.selectedItemPosition
        val selectedStatusIndex = spinnerStatus.selectedItemPosition

        val validation = ValidationUtils.validateUserInput(
            username = username,
            email = email,
            password = password,
            fullName = fullName,
            phone = phone
        )

        if (!validation.isValid) {
            DialogUtils.showErrorDialog(requireContext(), validation.errors.joinToString("\n")) {}
            return
        }

        val role = when (selectedRoleIndex) {
            0 -> UserModel.UserRole.ADMIN
            1 -> UserModel.UserRole.STAFF
            2 -> UserModel.UserRole.CUSTOMER
            else -> UserModel.UserRole.CUSTOMER
        }

        val status = when (selectedStatusIndex) {
            0 -> UserModel.UserStatus.ACTIVE
            1 -> UserModel.UserStatus.INACTIVE
            2 -> UserModel.UserStatus.SUSPENDED
            else -> UserModel.UserStatus.ACTIVE
        }

        // Handle avatar image
        var avatarPath = ""
        selectedImageUri?.let { uri ->
            val bitmap = ImageUtils.compressImage(requireContext(), uri)
            bitmap?.let {
                val fileName = ImageUtils.generateUniqueFileName()
                ImageUtils.saveImageToInternalStorage(requireContext(), it, fileName)?.let { path ->
                    avatarPath = path
                }
            }
        }

        val newUser = UserModel(
            username = username,
            email = email,
            phone = phone,
            fullName = fullName,
            address = address,
            password = password,
            role = role,
            status = status,
            avatar = avatarPath
        )

        try {
            val result = userDao.insertUser(newUser)
            if (result > 0) {
                DialogUtils.showToast(requireContext(), "Thêm người dùng thành công")
                loadUsers()
                dialog.dismiss()
            } else {
                DialogUtils.showErrorDialog(requireContext(), "Không thể thêm người dùng") {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding user", e)
            DialogUtils.showErrorDialog(requireContext(), "Lỗi khi thêm người dùng: ${e.message}") {}
        }
    }

    private fun handleEditUser(dialog: Dialog, user: UserModel) {
        val etUserName = dialog.findViewById<EditText>(R.id.et_user_name)
        val etUserEmail = dialog.findViewById<EditText>(R.id.et_user_email)
        val etUserPhone = dialog.findViewById<EditText>(R.id.et_user_phone)
        val etUserAddress = dialog.findViewById<EditText>(R.id.et_user_address)
        val spinnerRole = dialog.findViewById<Spinner>(R.id.spinner_user_role_dialog)
        val spinnerStatus = dialog.findViewById<Spinner>(R.id.spinner_user_status_dialog)

        val fullName = etUserName.text.toString().trim()
        val email = etUserEmail.text.toString().trim()
        val phone = etUserPhone.text.toString().trim()
        val address = etUserAddress.text.toString().trim()
        val selectedRoleIndex = spinnerRole.selectedItemPosition
        val selectedStatusIndex = spinnerStatus.selectedItemPosition

        val validation = ValidationUtils.validateUserInput(
            username = user.username,
            email = email,
            password = user.password,
            fullName = fullName,
            phone = phone
        )

        if (!validation.isValid) {
            DialogUtils.showErrorDialog(requireContext(), validation.errors.joinToString("\n")) {}
            return
        }

        val role = when (selectedRoleIndex) {
            0 -> UserModel.UserRole.ADMIN
            1 -> UserModel.UserRole.STAFF
            2 -> UserModel.UserRole.CUSTOMER
            else -> UserModel.UserRole.CUSTOMER
        }

        val status = when (selectedStatusIndex) {
            0 -> UserModel.UserStatus.ACTIVE
            1 -> UserModel.UserStatus.INACTIVE
            2 -> UserModel.UserStatus.SUSPENDED
            else -> UserModel.UserStatus.ACTIVE
        }

        // Handle avatar image update
        var avatarPath = user.avatar
        selectedImageUri?.let { uri ->
            val bitmap = ImageUtils.compressImage(requireContext(), uri)
            bitmap?.let {
                // Delete old avatar if exists
                if (avatarPath.isNotEmpty()) {
                    ImageUtils.deleteImageFile(avatarPath)
                }
                // Save new avatar
                val fileName = ImageUtils.generateUniqueFileName()
                ImageUtils.saveImageToInternalStorage(requireContext(), it, fileName)?.let { path ->
                    avatarPath = path
                }
            }
        }

        val updatedUser = user.copy(
            fullName = fullName,
            email = email,
            phone = phone,
            address = address,
            role = role,
            status = status,
            avatar = avatarPath
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

    private fun showDeleteUserDialog(user: UserModel) {
        DialogUtils.showConfirmDialog(
            requireContext(),
            "Xác nhận xóa",
            "Bạn có chắc chắn muốn xóa người dùng ${user.fullName.ifEmpty { user.username }}?",
            positiveAction = {
                try {
                    // Delete avatar file if exists
                    if (user.avatar.isNotEmpty()) {
                        ImageUtils.deleteImageFile(user.avatar)
                    }

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
            appendLine("Địa chỉ: ${user.address}")
            appendLine("Vai trò: ${user.getDisplayRole()}")
            appendLine("Trạng thái: ${user.getDisplayStatus()}")
            appendLine("Ngày tạo: ${user.createdAt}")
        }

        DialogUtils.showInfoDialog(requireContext(), "Chi tiết người dùng", message)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        loadUsers()
    }
}