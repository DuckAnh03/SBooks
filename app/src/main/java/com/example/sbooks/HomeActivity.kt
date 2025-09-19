package com.example.sbooks

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sbooks.databinding.ActivityHomeBinding
import com.example.sbooks.utils.BroadcastAirPlane
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.SharedPrefsHelper

class HomeActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "HomeActivity"
    }

    private lateinit var binding: ActivityHomeBinding
    private lateinit var sharedPrefs: SharedPrefsHelper

    var br = BroadcastAirPlane()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting HomeActivity")

        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa được cấp thì xin quyền
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            // Nếu đã được cấp thì mở camera luôn
            openCamera()
        }*/

        val filter = IntentFilter()
        filter.addAction("android.intent.action.AIRPLANE_MODE")
        this.registerReceiver(br, filter)

        enableEdgeToEdge()

        // Initialize SharedPrefs first
        sharedPrefs = SharedPrefsHelper(this)

        // Sử dụng ViewBinding cho layout Home
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thêm padding top cho status bar (nếu muốn)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Load default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        setupBottomNavigation()
        Log.d(TAG, "onCreate: HomeActivity setup complete")
    }
    /*override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền Camera để dùng chức năng này", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivity(intent)
    }*/


    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_cart -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CartFragment())
                        .commit()
                    true
                }
                R.id.nav_order -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, OrderListFragment())
                        .commit()
                    true
                }
                R.id.nav_user -> {
                    handleUserNavigation()
                    true
                }
                else -> false
            }
        }
    }

    private fun handleUserNavigation() {
        Log.d(TAG, "handleUserNavigation: User clicked on account")

        if (sharedPrefs.isLoggedIn()) {
            // User is logged in, show account options
            val userRole = sharedPrefs.getUserRole()
            Log.d(TAG, "handleUserNavigation: User is logged in with role: $userRole")

            when (userRole) {
                "admin" -> {
                    // Navigate to admin interface
                    val intent = Intent(this, com.example.sbooks.activities.admin.AdminMainActivity::class.java)
                    startActivity(intent)
                }
                "staff" -> {
                    // Navigate to staff interface
                    val intent = Intent(this, com.example.sbooks.activities.staff.StaffMainActivity::class.java)
                    startActivity(intent)
                }
                "customer" -> {
                    // Show customer account options or fragment
                    showCustomerAccountOptions()
                }
                else -> {
                    Log.w(TAG, "handleUserNavigation: Unknown user role: $userRole")
                    navigateToLogin()
                }
            }
        } else {
            // User is not logged in, navigate to login
            Log.d(TAG, "handleUserNavigation: User not logged in, navigating to login")
            navigateToLogin()
        }
    }

    private fun showCustomerAccountOptions() {
        // For now, show a dialog with customer options
        val username = sharedPrefs.getUsername()
        val fullName = sharedPrefs.getString("full_name", username)

        val options = arrayOf("Thông tin tài khoản", "Lịch sử đơn hàng", "Đăng xuất")

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Xin chào, $fullName")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> showAccountInfo()
                1 -> showOrderHistory()
                2 -> showLogoutDialog()
            }
        }
        builder.show()
    }

    private fun showAccountInfo() {
        val username = sharedPrefs.getUsername()
        val fullName = sharedPrefs.getString("full_name", "N/A")
        val email = sharedPrefs.getString("email", "N/A")

        val message = """
            Tên đăng nhập: $username
            Họ tên: $fullName
            Email: $email
        """.trimIndent()

        DialogUtils.showInfoDialog(this, "Thông tin tài khoản", message)
    }

    private fun showOrderHistory() {
        DialogUtils.showInfoDialog(this, "Lịch sử đơn hàng", "Tính năng đang phát triển.")
    }

    private fun showLogoutDialog() {
        DialogUtils.showConfirmDialog(
            this,
            "Đăng xuất",
            "Bạn có chắc chắn muốn đăng xuất?",
            positiveAction = { performLogout() }
        )
    }

    private fun performLogout() {
        Log.d(TAG, "performLogout: Logging out user")
        sharedPrefs.clearUserSession()
        DialogUtils.showToast(this, "Đã đăng xuất thành công")

        // Refresh the current fragment or navigate to login
        navigateToLogin()
    }

    private fun navigateToLogin() {
        Log.d(TAG, "navigateToLogin: Navigating to LoginActivity")
        val intent = Intent(this, LoginActivity::class.java)
        // Don't clear task here as we want to come back to HomeActivity after login
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: HomeActivity resumed")
        // You might want to refresh user state here if needed
    }

}