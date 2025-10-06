package com.example.sbooks

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class AccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        // Load fragment đầu tiên khi activity được tạo
        if (savedInstanceState == null) {
            loadDefaultFragment()
        }
    }

    private fun loadDefaultFragment() {
        val fragment = AccountOverviewFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Method để chuyển đổi giữa các fragment khác
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // Thêm vào back stack để có thể quay lại
            .commit()
    }
}