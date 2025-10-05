package com.example.sbooks

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
        setupAutoComplete() // GỌI HÀM NÀY
        Log.d(TAG, "onCreate: HomeActivity setup complete")
    }

    private fun setupAutoComplete() {
        // SỬ DỤNG BINDING thay vì findViewById
        val searchView = binding.searchView // Đảm bảo searchView có trong layout

        // Danh sách gợi ý tĩnh
        val suggestions = arrayOf(
            "Android Development",
            "Kotlin Programming",
            "Java Programming",
            "Flutter Development",
            "React Native",
            "Mobile App Development"
        )

        // Tạo adapter cho gợi ý
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            suggestions
        )

        searchView.setAdapter(adapter)

        // Xử lý sự kiện khi chọn gợi ý
        searchView.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String
            // Xử lý khi user chọn gợi ý
            handleSearchSelection(selectedItem)
        }

        // Xử lý sự kiện thay đổi text (tùy chọn)
        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Có thể filter gợi ý động ở đây
                filterSuggestions(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun handleSearchSelection(selectedItem: String) {
        // Xử lý khi user chọn một gợi ý
        Log.d(TAG, "handleSearchSelection: Selected item: $selectedItem")
        Toast.makeText(this, "Đã chọn: $selectedItem", Toast.LENGTH_SHORT).show()

        // Có thể thực hiện search hoặc navigate đến kết quả tìm kiếm
        performSearch(selectedItem)
    }

    private fun filterSuggestions(query: String) {
        // Logic để filter gợi ý động dựa trên input
        Log.d(TAG, "filterSuggestions: Query: $query")

        // Ví dụ: có thể gọi API để lấy gợi ý dựa trên query
        if (query.length >= 2) {
            // Thực hiện filter hoặc call API
        }
    }

    private fun performSearch(query: String) {
        // Thực hiện tìm kiếm với query
        Log.d(TAG, "performSearch: Searching for: $query")

        // Ví dụ: navigate đến search results activity
        // val intent = Intent(this, SearchResultsActivity::class.java)
        // intent.putExtra("query", query)
        // startActivity(intent)
    }

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

    override fun onDestroy() {
        super.onDestroy()
        // Unregister broadcast receiver
        try {
            unregisterReceiver(br)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
    }
}