package com.example.sbooks.fragments.customer

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sbooks.activities.admin.AdminMainActivity
import com.example.sbooks.activities.customer.HomeActivity
import com.example.sbooks.activities.staff.StaffMainActivity
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.UserDao
import com.example.sbooks.databinding.FragmentLoginBinding
import com.example.sbooks.models.UserModel
import com.example.sbooks.activities.customer.LoginActivity
import com.example.sbooks.utils.SharedPrefsHelper
import com.example.sbooks.activities.customer.AccountActivity

class LoginFragment : Fragment() {

    private companion object {
        private const val TAG = "LoginFragment"
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var userDao: UserDao
    private lateinit var sharedPrefsHelper: SharedPrefsHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database
        val dbHelper = DatabaseHelper(requireContext())
        userDao = UserDao(dbHelper.readableDatabase)

        // KHỞI TẠO SharedPrefsHelper
        sharedPrefsHelper = SharedPrefsHelper(requireContext())
        // Check if already logged in (SỬ DỤNG SharedPrefsHelper)
        if (sharedPrefsHelper.isLoggedIn()) {
            // Lấy role từ SharedPrefsHelper
            navigateToRoleActivity(sharedPrefsHelper.getUserRole())
            return
        }

        setupViews()
    }

    private fun setupViews() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(username, password)) {
                performLogin(username, password)
            }
        }

        // Clear errors when user starts typing
        binding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etUsername.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etPassword.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validateInput(username: String, password: String): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            binding.etUsername.error = "Vui lòng nhập tên đăng nhập"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Vui lòng nhập mật khẩu"
            isValid = false
        } else if (password.length < 6) {
            binding.etPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
            isValid = false
        }

        return isValid
    }

    private fun performLogin(username: String, password: String) {
        // Show loading state
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Đang đăng nhập..."
        binding.etUsername.isEnabled = false
        binding.etPassword.isEnabled = false

        // Validate login from database
        val user = userDao.validateLogin(username, password)

        if (user != null) {
            Log.i(TAG, "Login successful for user: ${user.username}, role: ${user.role}")

            // Save session
            sharedPrefsHelper.saveUserSession(
                userId = user.id,
                username = user.username,
                userRole = user.role.value
            )

            // Show success message
            Toast.makeText(
                requireContext(),
                "Đăng nhập thành công! Xin chào ${user.fullName}",
                Toast.LENGTH_SHORT
            ).show()

            // Navigate based on role
            navigateToRoleActivity(user.role.value)
        } else {
            Log.w(TAG, "Login failed for username: $username")

            // Reset UI
            resetLoginUI()

            Toast.makeText(
                requireContext(),
                "Tên đăng nhập hoặc mật khẩu không đúng",
                Toast.LENGTH_SHORT
            ).show()

            // Clear password and focus
            binding.etPassword.text?.clear()
            binding.etPassword.requestFocus()
        }
    }

    private fun resetLoginUI() {
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = "Đăng nhập"
        binding.etUsername.isEnabled = true
        binding.etPassword.isEnabled = true
    }

    // Trong LoginFragment.kt, sửa hàm navigateToRoleActivity:

    private fun navigateToRoleActivity(role: String) {
        val activityClass = when (role) {
            "admin" -> AdminMainActivity::class.java
            "staff" -> StaffMainActivity::class.java
            "customer" -> AccountActivity::class.java
            else -> {
                Log.e(TAG, "Unknown user role: $role. Navigating to HomeActivity as default.")
                HomeActivity::class.java // Cần thêm ::class.java để khớp kiểu dữ liệu
            }
        }
        try {
            val intent = Intent(activity, activityClass)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        } catch (e: Exception) {
            Log.e(TAG, "Navigation failed: ${e.message}", e)
            Toast.makeText(context, "Lỗi: Không thể chuyển màn hình đến ${activityClass.simpleName}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}