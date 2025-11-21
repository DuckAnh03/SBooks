package com.example.sbooks.fragments.customer

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sbooks.R
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.UserDao
import com.example.sbooks.models.UserModel
import com.google.android.material.textfield.TextInputEditText

class RegisterFragment : Fragment() {

    private companion object {
        private const val TAG = "RegisterFragment"
    }

    private lateinit var etFullname: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var cbTerms: CheckBox
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
    }

    private fun initViews(view: View) {
        etFullname = view.findViewById(R.id.et_fullname)
        etEmail = view.findViewById(R.id.et_email)
        etUsername = view.findViewById(R.id.et_username_register)
        etPassword = view.findViewById(R.id.et_password_register)
        etConfirmPassword = view.findViewById(R.id.et_confirm_password)
        etPhone = view.findViewById(R.id.et_phone)
        cbTerms = view.findViewById(R.id.cb_terms)
        btnRegister = view.findViewById(R.id.btn_register)
        tvLogin = view.findViewById(R.id.tv_login)
    }

    private fun setupListeners() {
        // Enable/Disable register button based on terms checkbox
        cbTerms.setOnCheckedChangeListener { _, isChecked ->
            validateForm()
        }

        // Add text watchers for validation
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etFullname.addTextChangedListener(textWatcher)
        etEmail.addTextChangedListener(textWatcher)
        etUsername.addTextChangedListener(textWatcher)
        etPassword.addTextChangedListener(textWatcher)
        etConfirmPassword.addTextChangedListener(textWatcher)
        etPhone.addTextChangedListener(textWatcher)

        // Register button click
        btnRegister.setOnClickListener {
            processRegister()
        }

        // Login link click
        tvLogin.setOnClickListener {
            // Navigate back to login fragment
            parentFragmentManager.popBackStack()
        }
    }

    private fun validateForm() {
        val fullname = etFullname.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        val phone = etPhone.text.toString().trim()

        // Enable button only if all fields are filled and terms accepted
        val allFieldsFilled = fullname.isNotEmpty() &&
                email.isNotEmpty() &&
                username.isNotEmpty() &&
                password.isNotEmpty() &&
                confirmPassword.isNotEmpty() &&
                phone.isNotEmpty()

        btnRegister.isEnabled = allFieldsFilled && cbTerms.isChecked
    }

    private fun processRegister() {
        val fullname = etFullname.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        val phone = etPhone.text.toString().trim()

        // Validate all fields
        if (!validateAllFields(fullname, email, username, password, confirmPassword, phone)) {
            return
        }

        // Show loading
        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Đang tạo tài khoản...")
            setCancelable(false)
            show()
        }

        // Save user in background thread
        Thread {
            try {
                val dbHelper = DatabaseHelper(requireContext())
                val userDao = UserDao(dbHelper.writableDatabase)

                // Check if username already exists
                if (userDao.getUserByUsername(username) != null) {
                    activity?.runOnUiThread {
                        progressDialog.dismiss()
                        Toast.makeText(requireContext(), "Tên đăng nhập đã tồn tại!", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                // Check if email already exists
                if (userDao.getUserByEmail(email) != null) {
                    activity?.runOnUiThread {
                        progressDialog.dismiss()
                        Toast.makeText(requireContext(), "Email đã được sử dụng!", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                // Create new user
                val newUser = UserModel(
                    id = 0, // Auto-generated
                    username = username,
                    email = email,
                    phone = phone,
                    fullName = fullname,
                    address = "", // Empty for now, user can update later
                    password = password, // Should hash in production!
                    role = UserModel.UserRole.CUSTOMER, // Default role
                    status = UserModel.UserStatus.ACTIVE,
                    avatar = "",
                    createdAt = getCurrentDateTime(),
                    updatedAt = getCurrentDateTime()
                )

                // Insert user
                val userId = userDao.insertUser(newUser)

                activity?.runOnUiThread {
                    progressDialog.dismiss()

                    if (userId > 0) {
                        Toast.makeText(
                            requireContext(),
                            "Đăng ký thành công! Vui lòng đăng nhập.",
                            Toast.LENGTH_LONG
                        ).show()

                        // Go back to login fragment
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Đăng ký thất bại. Vui lòng thử lại!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error during registration", e)
                activity?.runOnUiThread {
                    progressDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Lỗi: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    private fun validateAllFields(
        fullname: String,
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
        phone: String
    ): Boolean {

        // Validate fullname
        if (fullname.length < 2) {
            etFullname.error = "Họ tên phải có ít nhất 2 ký tự"
            etFullname.requestFocus()
            return false
        }

        // Validate email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Email không hợp lệ"
            etEmail.requestFocus()
            return false
        }

        // Validate username
        if (username.length < 3) {
            etUsername.error = "Tên đăng nhập phải có ít nhất 3 ký tự"
            etUsername.requestFocus()
            return false
        }

        if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            etUsername.error = "Tên đăng nhập chỉ được chứa chữ, số và dấu gạch dưới"
            etUsername.requestFocus()
            return false
        }

        // Validate password
        if (password.length < 6) {
            etPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
            etPassword.requestFocus()
            return false
        }

        // Validate confirm password
        if (password != confirmPassword) {
            etConfirmPassword.error = "Mật khẩu xác nhận không khớp"
            etConfirmPassword.requestFocus()
            return false
        }

        // Validate phone
        val phoneDigits = phone.replace(Regex("[^0-9]"), "")
        if (phoneDigits.length < 10 || phoneDigits.length > 11) {
            etPhone.error = "Số điện thoại không hợp lệ (10-11 chữ số)"
            etPhone.requestFocus()
            return false
        }

        if (!phoneDigits.startsWith("0")) {
            etPhone.error = "Số điện thoại phải bắt đầu bằng 0"
            etPhone.requestFocus()
            return false
        }

        // Validate terms
        if (!cbTerms.isChecked) {
            Toast.makeText(requireContext(), "Bạn phải đồng ý với điều khoản và điều kiện", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }
}