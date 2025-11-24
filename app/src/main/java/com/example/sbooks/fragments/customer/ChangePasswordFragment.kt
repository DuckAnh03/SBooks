package com.example.sbooks.fragments.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sbooks.R
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.UserDao
import com.example.sbooks.utils.SharedPrefsHelper
import java.security.MessageDigest

class ChangePasswordFragment : Fragment() {

    private lateinit var userDao: UserDao
    private lateinit var sharedPrefsHelper: SharedPrefsHelper

    // Views
    private lateinit var btnBack: ImageButton
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_change_password, container, false)

        // Initialize
        sharedPrefsHelper = SharedPrefsHelper(requireContext())
        val dbHelper = DatabaseHelper(requireContext())
        userDao = UserDao(dbHelper.writableDatabase)

        // Initialize views
        initViews(root)

        // Setup listeners
        setupListeners()

        return root
    }

    private fun initViews(root: View) {
        btnBack = root.findViewById(R.id.btnBack)
        etCurrentPassword = root.findViewById(R.id.etCurrentPassword)
        etNewPassword = root.findViewById(R.id.etNewPassword)
        etConfirmPassword = root.findViewById(R.id.etConfirmPassword)
        btnChangePassword = root.findViewById(R.id.btnChangePassword)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnChangePassword.setOnClickListener {
            changePassword()
        }
    }

    private fun validateInput(): Boolean {
        val currentPassword = etCurrentPassword.text.toString()
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // Validate current password
        if (currentPassword.isEmpty()) {
            etCurrentPassword.error = "Vui lòng nhập mật khẩu hiện tại"
            etCurrentPassword.requestFocus()
            return false
        }

        // Validate new password
        if (newPassword.isEmpty()) {
            etNewPassword.error = "Vui lòng nhập mật khẩu mới"
            etNewPassword.requestFocus()
            return false
        }

        if (newPassword.length < 6) {
            etNewPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
            etNewPassword.requestFocus()
            return false
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Vui lòng xác nhận mật khẩu"
            etConfirmPassword.requestFocus()
            return false
        }

        if (newPassword != confirmPassword) {
            etConfirmPassword.error = "Mật khẩu xác nhận không khớp"
            etConfirmPassword.requestFocus()
            return false
        }

        // Check if new password is different from current
        if (currentPassword == newPassword) {
            etNewPassword.error = "Mật khẩu mới phải khác mật khẩu hiện tại"
            etNewPassword.requestFocus()
            return false
        }

        return true
    }

    private fun changePassword() {
        if (!validateInput()) {
            return
        }

        val userId = sharedPrefsHelper.getUserId()
        if (userId == -1) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        btnChangePassword.isEnabled = false
        btnChangePassword.text = "Đang xử lý..."

        Thread {
            try {
                val user = userDao.getUserById(userId)

                if (user == null) {
                    activity?.runOnUiThread {
                        btnChangePassword.isEnabled = true
                        btnChangePassword.text = "Đổi mật khẩu"
                        Toast.makeText(
                            requireContext(),
                            "Không tìm thấy người dùng",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@Thread
                }

                // Verify current password
                val currentPassword = etCurrentPassword.text.toString()
                if (user.password != currentPassword) {
                    activity?.runOnUiThread {
                        btnChangePassword.isEnabled = true
                        btnChangePassword.text = "Đổi mật khẩu"
                        etCurrentPassword.error = "Mật khẩu hiện tại không đúng"
                        etCurrentPassword.requestFocus()
                    }
                    return@Thread
                }

                // Update password
                val newPassword = etNewPassword.text.toString()
                val updatedUser = user.copy(password = newPassword)
                val result = userDao.updateUser(updatedUser)

                activity?.runOnUiThread {
                    btnChangePassword.isEnabled = true
                    btnChangePassword.text = "Đổi mật khẩu"

                    if (result > 0) {
                        Toast.makeText(
                            requireContext(),
                            "Đổi mật khẩu thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Không thể đổi mật khẩu",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    btnChangePassword.isEnabled = true
                    btnChangePassword.text = "Đổi mật khẩu"
                    Toast.makeText(
                        requireContext(),
                        "Lỗi: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}