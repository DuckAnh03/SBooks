package com.example.sbooks

import android.content.Intent
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
import com.example.sbooks.activities.admin.AdminMainActivity
import com.example.sbooks.activities.staff.StaffMainActivity
import com.example.sbooks.databinding.FragmentLoginBinding

// LoginFragment.kt - Demo version
class LoginFragment : Fragment() {

    private companion object {
        private const val TAG = "LoginFragment"
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

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
        Log.d(TAG, "LoginFragment created")
        setupViews()
    }

    private fun setupViews() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            Log.d(TAG, "Login attempt for username: $username")

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

        // Disable input fields during login
        binding.etUsername.isEnabled = false
        binding.etPassword.isEnabled = false

        // Simulate API call with delay
        Handler(Looper.getMainLooper()).postDelayed({

            // Reset UI state first
            resetLoginUI()

            // Check demo accounts
            when {
                username == "admin" && password == "admin123" -> {
                    Log.i(TAG, "Demo admin login successful")
                    Toast.makeText(context, "✅ Đăng nhập thành công với tài khoản Admin", Toast.LENGTH_SHORT).show()
                    navigateToActivity(AdminMainActivity::class.java, "admin")
                }
                username == "staff" && password == "staff123" -> {
                    Log.i(TAG, "Demo staff login successful")
                    Toast.makeText(context, "✅ Đăng nhập thành công với tài khoản Staff", Toast.LENGTH_SHORT).show()
                    navigateToActivity(StaffMainActivity::class.java, "staff")
                }
                // Add more demo accounts for testing
                username.startsWith("test") && password == "123456" -> {
                    Toast.makeText(context, "✅ Đăng nhập thành công với tài khoản Test", Toast.LENGTH_SHORT).show()
                    // For demo, just show success without navigation
                }
                else -> {
                    Log.w(TAG, "Login failed for username: $username")
                    Toast.makeText(context, "❌ Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()

                    // Clear password and focus on it
                    binding.etPassword.text?.clear()
                    binding.etPassword.requestFocus()

                    // Shake animation effect (optional)
                    binding.etUsername.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left))
                }
            }
        }, 1500) // 1.5 second delay to simulate network call
    }

    private fun resetLoginUI() {
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = "Đăng nhập"
        binding.etUsername.isEnabled = true
        binding.etPassword.isEnabled = true
    }

    private fun navigateToActivity(activityClass: Class<*>, userType: String) {
        try {
            val intent = Intent(activity, activityClass)
            intent.putExtra("user_type", userType)
            intent.putExtra("demo_mode", true)
            startActivity(intent)
            activity?.finish()
        } catch (e: Exception) {
            Log.e(TAG, "Navigation failed", e)
            Toast.makeText(context, "Chức năng đang được phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}