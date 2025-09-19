package com.example.sbooks.activities.admin

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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.example.sbooks.R
import com.example.sbooks.LoginActivity
import com.example.sbooks.utils.Constants
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.SharedPrefsHelper

class AdminMainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private companion object {
        private const val TAG = "AdminMainActivity"
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sharedPrefs: SharedPrefsHelper
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting AdminMainActivity")

        // Initialize SharedPrefs first to check authorization
        sharedPrefs = SharedPrefsHelper(this)

        // Check if user is authorized before doing anything else
        if (!isUserAdmin()) {
            Log.w(TAG, "onCreate: User is not admin or not logged in. Redirecting to login.")
            redirectToLogin()
            return
        }

        try {
            // Set Content View
            setContentView(R.layout.activity_admin_main)
            Log.d(TAG, "onCreate: setContentView successful")

            // Initialize all components
            initializeComponents()
            setupBackPressHandler()

            Log.d(TAG, "onCreate: AdminMainActivity setup complete.")
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
            setupFab()
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
            fab = findViewById(R.id.fab)
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
                    R.id.nav_admin_dashboard,
                    R.id.nav_admin_users,
                    R.id.nav_admin_books,
                    R.id.nav_admin_categories,
                    R.id.nav_admin_orders,
                    R.id.nav_admin_reports
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

    private fun setupFab() {
        Log.d(TAG, "setupFab")
        try {
            fab.setOnClickListener {
                Log.d(TAG, "FAB clicked. Current destination: ${navController?.currentDestination?.id}")
                handleFabClick()
            }

            // Show/hide FAB based on current destination
            navController?.addOnDestinationChangedListener { _, destination, _ ->
                Log.d(TAG, "Destination changed to: ${destination.label} (ID: ${destination.id})")
                when (destination.id) {
                    R.id.nav_admin_users,
                    R.id.nav_admin_books,
                    R.id.nav_admin_categories -> fab.show()
                    else -> fab.hide()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupFab: FAB setup failed", e)
            // Don't throw here as FAB is not critical
        }
    }

    private fun setupNavigationHeader() {
        Log.d(TAG, "setupNavigationHeader")
        try {
            val headerView = navigationView.getHeaderView(0)
            if (headerView != null) {
                val tvAdminName = headerView.findViewById<android.widget.TextView>(R.id.tv_admin_name)
                val tvAdminEmail = headerView.findViewById<android.widget.TextView>(R.id.tv_admin_email)

                if (tvAdminName != null && tvAdminEmail != null) {
                    tvAdminName.text = sharedPrefs.getString("full_name", "Administrator")
                    tvAdminEmail.text = sharedPrefs.getString("email", "admin@sbooks.com")
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

        DialogUtils.showErrorDialog(this, "Admin interface initialization failed: $errorMessage") {
            finish()
        }
    }

    private fun isUserAdmin(): Boolean {
        val isAdmin = sharedPrefs.isLoggedIn() && sharedPrefs.getUserRole() == "admin"
        Log.d(TAG, "isUserAdmin: $isAdmin, LoggedIn: ${sharedPrefs.isLoggedIn()}, Role: ${sharedPrefs.getUserRole()}")
        return isAdmin
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
                R.id.nav_admin_dashboard -> {
                    navController?.navigate(R.id.nav_admin_dashboard) ?: showNavigationError()
                }
                R.id.nav_admin_users -> {
                    navController?.navigate(R.id.nav_admin_users) ?: showNavigationError()
                }
                R.id.nav_admin_books -> {
                    navController?.navigate(R.id.nav_admin_books) ?: showNavigationError()
                }
                R.id.nav_admin_categories -> {
                    navController?.navigate(R.id.nav_admin_categories) ?: showNavigationError()
                }
                R.id.nav_admin_orders -> {
                    navController?.navigate(R.id.nav_admin_orders) ?: showNavigationError()
                }
                R.id.nav_admin_reports -> {
                    navController?.navigate(R.id.nav_admin_reports) ?: showNavigationError()
                }
                R.id.nav_admin_profile -> {
                    showProfileDialog()
                }
                R.id.nav_admin_logout -> {
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
                // Don't finish, just close drawer
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
            menuInflater.inflate(R.menu.admin_toolbar_menu, menu)
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
                R.id.action_add -> { handleFabClick(); true }
                R.id.action_refresh -> { handleRefreshAction(); true }
                R.id.action_settings -> { showSettingsDialog(); true }
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

    private fun handleFabClick() {
        val currentDestId = navController?.currentDestination?.id
        when (currentDestId) {
            R.id.nav_admin_users -> showAddUserDialog()
            R.id.nav_admin_books -> showAddBookDialog()
            R.id.nav_admin_categories -> showAddCategoryDialog()
            else -> {
                Log.d(TAG, "FAB clicked but no action for destination: $currentDestId")
                fab.hide()
            }
        }
    }

    private fun showAddUserDialog() {
        DialogUtils.showToast(this, "Thêm người dùng")
    }

    private fun showAddBookDialog() {
        DialogUtils.showToast(this, "Thêm sách mới")
    }

    private fun showAddCategoryDialog() {
        DialogUtils.showToast(this, "Thêm danh mục mới")
    }

    private fun showProfileDialog() {
        val message = buildString {
            appendLine("Tên: ${sharedPrefs.getString("full_name", "Administrator")}")
            appendLine("Email: ${sharedPrefs.getString("email", "admin@sbooks.com")}")
            appendLine("Vai trò: Quản trị viên")
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

    private fun showSettingsDialog() {
        DialogUtils.showInfoDialog(this, "Cài đặt", "Tính năng đang phát triển")
    }

    private fun handleSearchAction() {
        val currentDestId = navController?.currentDestination?.id
        Log.d(TAG, "handleSearchAction on destination: $currentDestId")
        when (currentDestId) {
            R.id.nav_admin_users -> DialogUtils.showToast(this, "Tìm kiếm người dùng")
            R.id.nav_admin_books -> DialogUtils.showToast(this, "Tìm kiếm sách")
            R.id.nav_admin_orders -> DialogUtils.showToast(this, "Tìm kiếm đơn hàng")
            else -> DialogUtils.showToast(this, "Tính năng tìm kiếm")
        }
    }

    private fun handleFilterAction() {
        val currentDestId = navController?.currentDestination?.id
        Log.d(TAG, "handleFilterAction on destination: $currentDestId")
        when (currentDestId) {
            R.id.nav_admin_users -> DialogUtils.showToast(this, "Lọc người dùng")
            R.id.nav_admin_books -> DialogUtils.showToast(this, "Lọc sách")
            R.id.nav_admin_orders -> DialogUtils.showToast(this, "Lọc đơn hàng")
            else -> DialogUtils.showToast(this, "Tính năng lọc")
        }
    }

    private fun handleRefreshAction() {
        DialogUtils.showToast(this, "Làm mới dữ liệu")
    }

    private fun performLogout() {
        Log.d(TAG, "performLogout: Logging out admin")
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