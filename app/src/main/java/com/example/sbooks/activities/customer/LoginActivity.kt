package com.example.sbooks.activities.customer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.fragment.app.Fragment
// Xóa import com.example.sbooks.utils.SessionManager
import com.example.sbooks.utils.SharedPrefsHelper // Import SharedPrefsHelper mới
import com.example.sbooks.fragments.customer.LoginFragment
import com.example.sbooks.fragments.customer.RegisterFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.example.sbooks.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPrefsHelper: SharedPrefsHelper // Khai báo SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Khởi tạo SharedPrefsHelper
        sharedPrefsHelper = SharedPrefsHelper(this)

        // 2. Kiểm tra nếu người dùng đã đăng nhập (sử dụng sharedPrefsHelper.isLoggedIn())
        if (sharedPrefsHelper.isLoggedIn()) {
            Log.d(TAG, "User already logged in, redirecting to AccountActivity")
            navigateToAccount()
            return
        }

        Log.d(TAG, "onCreate: Starting LoginActivity with fragments")

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
    }

    private fun setupViewPager() {
        Log.d(TAG, "Setting up ViewPager with fragments")
        val adapter = AuthPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Đăng nhập"
                1 -> "Đăng ký"
                else -> null
            }
        }.attach()
    }

    // Public method for fragments to switch tabs
    fun switchToTab(position: Int) {
        if (position in 0..1) {
            binding.viewPager.currentItem = position
        }
    }

    private fun navigateToAccount() {
        try {
            val targetActivityClass = com.example.sbooks.activities.customer.AccountActivity::class.java

            val intent = Intent(this, targetActivityClass)
            startActivity(intent)
            finish()
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "Failed to find AccountActivity class. Make sure the path is correct.", e)
            // Xử lý hoặc thông báo lỗi cho người dùng
        } catch (e: Exception) {
            Log.e(TAG, "Failed to navigate to AccountActivity", e)
        }
    }

    // Call this method from LoginFragment after successful login
    fun onLoginSuccess() {
        navigateToAccount()
    }
}

// AuthPagerAdapter.kt (Không thay đổi)
class AuthPagerAdapter(private val activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LoginFragment()
            1 -> RegisterFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}