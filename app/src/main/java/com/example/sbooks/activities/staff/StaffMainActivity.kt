// StaffMainActivity.kt
package com.example.sbooks.activities.staff

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.example.sbooks.R
import com.example.sbooks.activities.customer.LoginActivity
import com.example.sbooks.utils.Constants
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.SharedPrefsHelper

class StaffMainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private companion object {
        private const val TAG = "StaffMainActivity"
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sharedPrefs: SharedPrefsHelper
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting StaffMainActivity")

        // Initialize SharedPrefs first to check authorization
        sharedPrefs = SharedPrefsHelper(this)

        // Check if user is authorized before doing anything else
        if (!isUserStaff()) {
            Log.w(TAG, "onCreate: User is not staff or not logged in. Redirecting to login.")
            redirectToLogin()
            return
        }

        try {
            // Set Content View
            setContentView(R.layout.activity_staff_main)
            Log.d(TAG, "onCreate: setContentView successful")

            // Initialize all components
            initializeComponents()
            setupBackPressHandler()

            Log.d(TAG, "onCreate: StaffMainActivity setup complete.")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Critical error during initialization", e)
            handleInitializationError(e)
        }
    }

    private fun initializeComponents() {
        try {
            initializeViews()
            initializeNavigation()
            setupToolbar()
            setupNavigationDrawer()
            setupNavigationHeader()
        } catch (e: Exception) {
            Log.e(TAG, "initializeComponents: Error during component initialization", e)
            throw e // Re-throw to be handled by onCreate
        }
    }

    private fun initializeViews() {
        Log.d(TAG, "initializeViews")
        try {
            drawerLayout = findViewById(R.id.drawer_layout)
            navigationView = findViewById(R.id.nav_view)
        } catch (e: Exception) {
            Log.e(TAG, "initializeViews: Missing required view IDs in layout", e)
            throw e
        }
    }

    private fun initializeNavigation() {
        Log.d(TAG, "initializeNavigation")
        try {
            navController = findNavController(R.id.nav_host_fragment)

            // Define top-level destinations for the drawer
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_staff_dashboard,
                    R.id.nav_staff_orders,
                    R.id.nav_staff_inventory
                ),
                drawerLayout
            )

            navController?.let { nc ->
                setupActionBarWithNavController(nc, appBarConfiguration)
                navigationView.setupWithNavController(nc)
            }
        } catch (e: Exception) {
            Log.e(TAG, "initializeNavigation: Navigation setup failed", e)
            // If navigation fails, we'll handle menu clicks manually
            Log.w(TAG, "Falling back to manual navigation handling")
        }
    }

    private fun setupToolbar() {
        Log.d(TAG, "setupToolbar")
        try {
            val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
        } catch (e: Exception) {
            Log.e(TAG, "setupToolbar: Toolbar setup failed", e)
            throw e
        }
    }

    private fun setupNavigationDrawer() {
        Log.d(TAG, "setupNavigationDrawer")
        try {
            val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
            )
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
            navigationView.setNavigationItemSelectedListener(this)
        } catch (e: Exception) {
            Log.e(TAG, "setupNavigationDrawer: Drawer setup failed", e)
            throw e
        }
    }

    private fun setupNavigationHeader() {
        Log.d(TAG, "setupNavigationHeader")
        try {
            val headerView = navigationView.getHeaderView(0)
            if (headerView != null) {
                val tvStaffName = headerView.findViewById<android.widget.TextView>(R.id.tv_staff_name)
                val tvStaffEmail = headerView.findViewById<android.widget.TextView>(R.id.tv_staff_email)

                if (tvStaffName != null && tvStaffEmail != null) {
                    tvStaffName.text = sharedPrefs.getString("full_name", "Nhân viên")
                    tvStaffEmail.text = sharedPrefs.getString("email", "staff@sbooks.com")
                } else {
                    Log.w(TAG, "setupNavigationHeader: Header text views not found")
                }
            } else {
                Log.w(TAG, "setupNavigationHeader: Navigation header view not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupNavigationHeader: Header setup failed", e)
            // Don't throw here as this is not critical
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "handleOnBackPressed: Back pressed")
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    val handled = navController?.popBackStack() ?: false
                    if (!handled) {
                        Log.d(TAG, "handleOnBackPressed: Cannot pop backstack, showing exit dialog")
                        showExitConfirmDialog()
                    }
                }
            }
        })
    }

    private fun handleInitializationError(e: Exception) {
        val errorMessage = when {
            e.message?.contains("nav_host_fragment") == true ->
                "Navigation component not found. Please check your layout files."
            e.message?.contains("drawer_layout") == true ->
                "Drawer layout not found. Please check your layout files."
            e.message?.contains("toolbar") == true ->
                "Toolbar not found. Please check your layout files."
            else -> "Unknown initialization error: ${e.message}"
        }

        DialogUtils.showErrorDialog(this, "Staff interface initialization failed: $errorMessage") {
            finish()
        }
    }

    private fun isUserStaff(): Boolean {
        val isStaff = sharedPrefs.isLoggedIn() && sharedPrefs.getUserRole() == "staff"
        Log.d(TAG, "isUserStaff: $isStaff, LoggedIn: ${sharedPrefs.isLoggedIn()}, Role: ${sharedPrefs.getUserRole()}")
        return isStaff
    }

    private fun redirectToLogin() {
        Log.d(TAG, "redirectToLogin: Redirecting to LoginActivity")
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onNavigationItemSelected: ${item.title} (ID: ${item.itemId})")

        try {
            when (item.itemId) {
                R.id.nav_staff_dashboard -> {
                    navController?.navigate(R.id.nav_staff_dashboard) ?: showNavigationError()
                }
                R.id.nav_staff_orders -> {
                    navController?.navigate(R.id.nav_staff_orders) ?: showNavigationError()
                }
                R.id.nav_staff_inventory -> {
                    navController?.navigate(R.id.nav_staff_inventory) ?: showNavigationError()
                }
                R.id.nav_staff_profile -> {
                    showProfileDialog()
                }
                R.id.nav_staff_logout -> {
                    showLogoutDialog()
                }
                else -> {
                    Log.w(TAG, "onNavigationItemSelected: Unhandled item ${item.itemId}")
                    DialogUtils.showToast(this, "Menu item: ${item.title}")
                    return false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onNavigationItemSelected: Navigation failed for ${item.title}", e)
            DialogUtils.showErrorDialog(this, "Navigation error: ${e.message}") {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            return false
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showNavigationError() {
        Log.w(TAG, "Navigation controller not available, showing placeholder")
        DialogUtils.showInfoDialog(this, "Thông báo", "Chức năng đang phát triển")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return try {
            menuInflater.inflate(R.menu.staff_toolbar_menu, menu)
            true
        } catch (e: Exception) {
            Log.e(TAG, "onCreateOptionsMenu: Failed to inflate menu", e)
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: ${item.title}")
        return try {
            when (item.itemId) {
                R.id.action_search -> { handleSearchAction(); true }
                R.id.action_filter -> { handleFilterAction(); true }
                R.id.action_refresh -> { handleRefreshAction(); true }
                else -> super.onOptionsItemSelected(item)
            }
        } catch (e: Exception) {
            Log.e(TAG, "onOptionsItemSelected: Action failed", e)
            false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return try {
            navController?.navigateUp(appBarConfiguration) ?: super.onSupportNavigateUp()
        } catch (e: Exception) {
            Log.e(TAG, "onSupportNavigateUp: Navigation up failed", e)
            super.onSupportNavigateUp()
        }
    }

    private fun showProfileDialog() {
        val message = buildString {
            appendLine("Tên: ${sharedPrefs.getString("full_name", "Nhân viên")}")
            appendLine("Email: ${sharedPrefs.getString("email", "staff@sbooks.com")}")
            appendLine("Vai trò: Nhân viên")
            appendLine("ID: ${sharedPrefs.getUserId()}")
        }
        DialogUtils.showInfoDialog(this, "Thông tin tài khoản", message)
    }

    private fun showLogoutDialog() {
        DialogUtils.showConfirmDialog(
            this,
            "Đăng xuất",
            "Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?",
            positiveAction = { performLogout() }
        )
    }

    private fun showExitConfirmDialog() {
        DialogUtils.showConfirmDialog(
            this,
            "Thoát ứng dụng",
            "Bạn có chắc chắn muốn thoát?",
            positiveAction = { finish() }
        )
    }

    private fun handleSearchAction() {
        val currentDestId = navController?.currentDestination?.id
        Log.d(TAG, "handleSearchAction on destination: $currentDestId")
        when (currentDestId) {
            R.id.nav_staff_orders -> DialogUtils.showToast(this, "Tìm kiếm đơn hàng")
            R.id.nav_staff_inventory -> DialogUtils.showToast(this, "Tìm kiếm sách trong kho")
            else -> DialogUtils.showToast(this, "Tính năng tìm kiếm")
        }
    }

    private fun handleFilterAction() {
        val currentDestId = navController?.currentDestination?.id
        Log.d(TAG, "handleFilterAction on destination: $currentDestId")
        when (currentDestId) {
            R.id.nav_staff_orders -> {
                val statuses = arrayOf("Tất cả", "Chờ xử lý", "Đang xử lý", "Hoàn thành")
                DialogUtils.showSingleChoiceDialog(this, "Lọc đơn hàng", statuses, 0) { selectedIndex ->
                    DialogUtils.showToast(this, "Đã chọn: ${statuses[selectedIndex]}")
                }
            }
            R.id.nav_staff_inventory -> {
                val stockLevels = arrayOf("Tất cả sản phẩm", "Còn hàng", "Sắp hết hàng", "Hết hàng")
                DialogUtils.showSingleChoiceDialog(this, "Lọc tồn kho", stockLevels, 0) { selectedIndex ->
                    DialogUtils.showToast(this, "Đã chọn: ${stockLevels[selectedIndex]}")
                }
            }
            else -> DialogUtils.showToast(this, "Tính năng lọc")
        }
    }

    private fun handleRefreshAction() {
        DialogUtils.showToast(this, "Làm mới dữ liệu")
    }

    private fun performLogout() {
        Log.d(TAG, "performLogout: Logging out staff")
        try {
            sharedPrefs.clearUserSession()
            DialogUtils.showToast(this, Constants.SUCCESS_LOGOUT)

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            finish() // Force finish even if there's an error
        }
    }
}