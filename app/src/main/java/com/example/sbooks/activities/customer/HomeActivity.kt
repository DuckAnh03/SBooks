package com.example.sbooks.activities.customer

import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sbooks.fragments.customer.CartFragment
import com.example.sbooks.fragments.customer.HomeFragment
import com.example.sbooks.fragments.customer.OrderListFragment
import com.example.sbooks.R
import com.example.sbooks.activities.admin.AdminMainActivity
import com.example.sbooks.activities.staff.StaffMainActivity
import com.example.sbooks.databinding.ActivityHomeBinding
import com.example.sbooks.utils.BroadcastAirPlane
import com.example.sbooks.utils.BroadcastWifi
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.SharedPrefsHelper
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.BookDao
import kotlinx.coroutines.*

// Import từ code chatbot
import com.example.sbooks.models.ChatViewModel
import com.example.sbooks.ui.theme.ChatBubbleOverlay
import androidx.compose.material3.MaterialTheme

class HomeActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "HomeActivity"
    }

    // Chat ViewModel
    private val chatViewModel: ChatViewModel by viewModels()

    private val broadcastWifi = BroadcastWifi()
    private lateinit var binding: ActivityHomeBinding
    private lateinit var sharedPrefs: SharedPrefsHelper
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var bookDao: BookDao

    private val searchJob = Job()
    private val searchScope = CoroutineScope(Dispatchers.Main + searchJob)

    var br = BroadcastAirPlane()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting HomeActivity")

        val filter = IntentFilter()
        filter.addAction("android.intent.action.AIRPLANE_MODE")
        this.registerReceiver(br, filter)

        val filterWifi = IntentFilter()
        filterWifi.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION)
        this.registerReceiver(broadcastWifi, filterWifi)

        enableEdgeToEdge()

        // Initialize SharedPrefs and Database
        sharedPrefs = SharedPrefsHelper(this)
        dbHelper = DatabaseHelper(this)
        bookDao = BookDao(dbHelper.writableDatabase)

        // Khởi tạo Chat
        chatViewModel.initializeChat("") // Thay YOUR_API_KEY

        // Sử dụng ViewBinding cho layout Home
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thêm padding top cho status bar
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
        setupSearchView()

        // Setup chat bubble từ XML
        binding.chatBubbleView.setContent {
            MaterialTheme {
                ChatBubbleOverlay(viewModel = chatViewModel)
            }
        }

        Log.d(TAG, "onCreate: HomeActivity setup complete")
    }

    private fun setupSearchView() {
        val searchView = binding.searchView
        val btnClear = binding.btnClearSearch

        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                btnClear.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE

                if (query.length >= 2) {
                    searchBooksInDatabase(query)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        searchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchView.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }

        btnClear.setOnClickListener {
            searchView.text.clear()
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(searchView.windowToken, 0)
        }
    }

    private fun searchBooksInDatabase(query: String) {
        searchScope.coroutineContext.cancelChildren()

        searchScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    val filter = com.example.sbooks.models.SearchFilter(
                        query = query,
                        sortBy = com.example.sbooks.models.SearchFilter.SortOption.NAME_ASC
                    )
                    bookDao.searchBooks(filter)
                }

                Log.d(TAG, "searchBooksInDatabase: Found ${results.size} books for query: $query")

                val suggestions = results.map { it.title }.toTypedArray()

                val adapter = android.widget.ArrayAdapter(
                    this@HomeActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    suggestions
                )
                binding.searchView.setAdapter(adapter)

                if (suggestions.isNotEmpty()) {
                    binding.searchView.showDropDown()
                }

            } catch (e: Exception) {
                Log.e(TAG, "searchBooksInDatabase: Error searching books", e)
            }
        }
    }

    private fun performSearch(query: String) {
        Log.d(TAG, "performSearch: Searching for: $query")

        searchScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    val filter = com.example.sbooks.models.SearchFilter(
                        query = query,
                        sortBy = com.example.sbooks.models.SearchFilter.SortOption.NAME_ASC
                    )
                    bookDao.searchBooks(filter)
                }

                Log.d(TAG, "performSearch: Found ${results.size} books")

                if (results.isEmpty()) {
                    Toast.makeText(
                        this@HomeActivity,
                        "Không tìm thấy sách với từ khóa: $query",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val fragment = HomeFragment.newInstance(query)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()

                    Toast.makeText(
                        this@HomeActivity,
                        "Tìm thấy ${results.size} cuốn sách",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)

            } catch (e: Exception) {
                Log.e(TAG, "performSearch: Error", e)
                Toast.makeText(
                    this@HomeActivity,
                    "Lỗi khi tìm kiếm: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
            val userRole = sharedPrefs.getUserRole()
            Log.d(TAG, "handleUserNavigation: User is logged in with role: $userRole")

            when (userRole) {
                "admin" -> {
                    val intent = Intent(this, AdminMainActivity::class.java)
                    startActivity(intent)
                }
                "staff" -> {
                    val intent = Intent(this, StaffMainActivity::class.java)
                    startActivity(intent)
                }
                "customer" -> {
                    val intent = Intent(this, AccountActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    Log.w(TAG, "handleUserNavigation: Unknown user role: $userRole")
                    navigateToLogin()
                }
            }
        } else {
            Log.d(TAG, "handleUserNavigation: User not logged in, navigating to login")
            navigateToLogin()
        }
    }

    private fun showCustomerAccountOptions() {
        val username = sharedPrefs.getUsername()
        val fullName = sharedPrefs.getString("full_name", username)

        val options = arrayOf("Thông tin tài khoản", "Lịch sử đơn hàng", "Đăng xuất")

        val builder = AlertDialog.Builder(this)
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
        navigateToLogin()
    }

    private fun navigateToLogin() {
        Log.d(TAG, "navigateToLogin: Navigating to LoginActivity")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: HomeActivity resumed")
    }

    override fun onDestroy() {
        super.onDestroy()

        searchJob.cancel()

        try {
            unregisterReceiver(br)
            unregisterReceiver(broadcastWifi)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }

        try {
            dbHelper.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing database", e)
        }
    }
}