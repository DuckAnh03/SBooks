package com.example.sbooks

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.example.sbooks.databinding.ActivityLoginBinding

// LoginActivity.kt - Demo version with fragment support
class LoginActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}

// AuthPagerAdapter.kt
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