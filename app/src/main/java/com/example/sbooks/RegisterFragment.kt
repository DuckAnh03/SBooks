package com.example.sbooks

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sbooks.databinding.FragmentRegisterBinding

// RegisterFragment.kt - Demo version
class RegisterFragment : Fragment() {

    private companion object {
        private const val TAG = "RegisterFragment"
    }

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "RegisterFragment created")
        setupViews()
    }

    private fun setupViews() {
        // Enable/disable register button based on terms checkbox
        binding.cbTerms.setOnCheckedChangeListener { _, isChecked ->
            binding.btnRegister.isEnabled = isChecked
            binding.btnRegister.alpha = if (isChecked) 1.0f else 0.5f
        }

        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullname.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val username = binding.etUsernameRegister.text.toString().trim()
            val password = binding.etPasswordRegister.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            Log.d(TAG, "Register attempt for username: $username, email: $email")

            if (validateInput(fullName, email, username, password, confirmPassword, phone)) {
                performRegister(fullName, email, username, password, phone)
            }
        }

        // Real-time password confirmation validation
        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = binding.etPasswordRegister.text.toString()
                val confirmPassword = s.toString()

                if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                    binding.etConfirmPassword.error = "Mật khẩu xác nhận không khớp"
                } else {
                    binding.etConfirmPassword.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Clear errors when user types
        setupErrorClearingListeners()
    }

    private fun setupErrorClearingListeners() {
        binding.etFullname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etFullname.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etEmail.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etUsernameRegister.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etUsernameRegister.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etPasswordRegister.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etPasswordRegister.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etPhone.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validateInput(
        fullName: String,
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
        phone: String
    ): Boolean {
        var isValid = true

        if (fullName.isEmpty() || fullName.length < 2) {
            binding.etFullname.error = "Họ và tên phải có ít nhất 2 ký tự"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Vui lòng nhập email"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email không hợp lệ"
            isValid = false
        }

        if (username.isEmpty()) {
            binding.etUsernameRegister.error = "Vui lòng nhập tên đăng nhập"
            isValid = false
        } else if (username.length < 3) {
            binding.etUsernameRegister.error = "Tên đăng nhập phải có ít nhất 3 ký tự"
            isValid = false
        } else if (username == "admin" || username == "staff") {
            binding.etUsernameRegister.error = "Tên đăng nhập đã tồn tại"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.etPasswordRegister.error = "Vui lòng nhập mật khẩu"
            isValid = false
        } else if (password.length < 6) {
            binding.etPasswordRegister.error = "Mật khẩu phải có ít nhất 6 ký tự"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.error = "Vui lòng xác nhận mật khẩu"
            isValid = false
        } else if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Mật khẩu xác nhận không khớp"
            isValid = false
        }

        if (phone.isEmpty()) {
            binding.etPhone.error = "Vui lòng nhập số điện thoại"
            isValid = false
        } else if (phone.length < 10) {
            binding.etPhone.error = "Số điện thoại phải có ít nhất 10 số"
            isValid = false
        }

        if (!binding.cbTerms.isChecked) {
            Toast.makeText(context, "Vui lòng đồng ý với điều khoản và điều kiện", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun performRegister(
        fullName: String,
        email: String,
        username: String,
        password: String,
        phone: String
    ) {
        // Show loading state
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Đang đăng ký..."

        // Disable all input fields
        setInputFieldsEnabled(false)

        // Simulate API call with delay
        Handler(Looper.getMainLooper()).postDelayed({

            // Reset UI state
            resetRegisterUI()

            // For demo, always show success
            Log.i(TAG, "Demo registration successful for: $username")
            Toast.makeText(
                context,
                "🎉 Đăng ký thành công!\nTên: $fullName\nEmail: $email\nSĐT: $phone",
                Toast.LENGTH_LONG
            ).show()

            // Clear form
            clearForm()

            // Switch back to login tab after successful registration
            Handler(Looper.getMainLooper()).postDelayed({
                (activity as? LoginActivity)?.switchToTab(0)
                Toast.makeText(context, "Vui lòng đăng nhập với tài khoản vừa tạo", Toast.LENGTH_SHORT).show()
            }, 1000)

        }, 2000) // 2 second delay
    }

    private fun setInputFieldsEnabled(enabled: Boolean) {
        binding.etFullname.isEnabled = enabled
        binding.etEmail.isEnabled = enabled
        binding.etUsernameRegister.isEnabled = enabled
        binding.etPasswordRegister.isEnabled = enabled
        binding.etConfirmPassword.isEnabled = enabled
        binding.etPhone.isEnabled = enabled
        binding.cbTerms.isEnabled = enabled
    }

    private fun resetRegisterUI() {
        binding.btnRegister.text = "Đăng ký"
        binding.btnRegister.isEnabled = binding.cbTerms.isChecked
        setInputFieldsEnabled(true)
    }

    private fun clearForm() {
        binding.etFullname.text?.clear()
        binding.etEmail.text?.clear()
        binding.etUsernameRegister.text?.clear()
        binding.etPasswordRegister.text?.clear()
        binding.etConfirmPassword.text?.clear()
        binding.etPhone.text?.clear()
        binding.cbTerms.isChecked = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}