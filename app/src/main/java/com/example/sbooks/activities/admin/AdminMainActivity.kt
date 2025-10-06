package com.example.sbooks.activities.admin

import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
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
import com.example.sbooks.fragments.admin.BookManagementFragment
import com.example.sbooks.fragments.admin.CategoryManagementFragment
import com.example.sbooks.fragments.admin.UserManagementFragment
import com.example.sbooks.utils.Constants
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.SharedPrefsHelper
import com.example.sbooks.utils.SampleDataManager
import com.example.sbooks.database.DatabaseHelper

class AdminMainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private companion object {
        private const val TAG = "AdminMainActivity"
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sharedPrefs: SharedPrefsHelper
    private lateinit var sampleDataManager: SampleDataManager
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting AdminMainActivity")

        // Initialize SharedPrefs first to check authorization
        sharedPrefs = SharedPrefsHelper(this)

        // Initialize SampleDataManager
        sampleDataManager = SampleDataManager(this)

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

            // Check and create sample data if needed
            /*checkAndCreateSampleData()*/

            Log.d(TAG, "onCreate: AdminMainActivity setup complete.")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Critical error during initialization", e)
            handleInitializationError(e)
        }
    }

    /**
     * Kiểm tra và tạo dữ liệu mẫu nếu cần
     */
    private fun checkAndCreateSampleData() {
        if (!sampleDataManager.hasSampleData()) {
            showSampleDataDialog()
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
                val ivAdminAvatar = headerView.findViewById<com.example.sbooks.utils.CircleImageView>(R.id.iv_admin_avatar)
                val statusIndicator = headerView.findViewById<android.view.View>(R.id.status_indicator)

                if (tvAdminName != null && tvAdminEmail != null) {
                    val fullName = sharedPrefs.getString("full_name", "Administrator")
                    val email = sharedPrefs.getString("email", "admin@sbooks.com")

                    tvAdminName.text = fullName
                    tvAdminEmail.text = email

                    // Load admin avatar if available
                    ivAdminAvatar?.let { avatarView ->
                        val avatarPath = sharedPrefs.getString("avatar", "")
                        if (avatarPath.isNotEmpty()) {
                            val bitmap = com.example.sbooks.utils.ImageUtils.loadImageFromInternalStorage(avatarPath)
                            if (bitmap != null) {
                                avatarView.setImageBitmap(bitmap)
                                Log.d(TAG, "Loaded admin avatar from: $avatarPath")
                            } else {
                                avatarView.setImageResource(com.example.sbooks.R.drawable.ic_users)
                                Log.w(TAG, "Failed to load admin avatar from: $avatarPath")
                            }
                        } else {
                            avatarView.setImageResource(com.example.sbooks.R.drawable.ic_users)
                            Log.d(TAG, "No admin avatar path found, using default")
                        }

                        // Set border color based on admin role
                        avatarView.setBorderColor(getColor(com.example.sbooks.R.color.error_color))
                    }

                    // Show online status indicator
                    statusIndicator?.visibility = android.view.View.VISIBLE

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
        Log.d(TAG, "handleFabClick: Current destination ID: $currentDestId")

        when (currentDestId) {
            R.id.nav_admin_users -> {
                // Get the current fragment and call showAddUserDialog
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)

                if (currentFragment is UserManagementFragment) {
                    Log.d(TAG, "Calling showAddUserDialog on UserManagementFragment")
                    currentFragment.showAddUserDialog()
                } else {
                    Log.w(TAG, "Current fragment is not UserManagementFragment: ${currentFragment?.javaClass?.simpleName}")
                    DialogUtils.showToast(this, "Thêm người dùng")
                }
            }

            R.id.nav_admin_books -> {
                // Get the current fragment and call showAddBookDialog
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)

                if (currentFragment is BookManagementFragment) {
                    Log.d(TAG, "Calling showAddBookDialog on BookManagementFragment")
                    currentFragment.showAddBookDialog()
                } else {
                    Log.w(TAG, "Current fragment is not BookManagementFragment: ${currentFragment?.javaClass?.simpleName}")
                    DialogUtils.showToast(this, "Thêm sách mới")
                }
            }

            R.id.nav_admin_categories -> {
                // Get the current fragment and call showAddCategoryDialog
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)

                if (currentFragment is CategoryManagementFragment) {
                    Log.d(TAG, "Calling showAddCategoryDialog on CategoryManagementFragment")
                    currentFragment.showAddCategoryDialog()
                } else {
                    Log.w(TAG, "Current fragment is not CategoryManagementFragment: ${currentFragment?.javaClass?.simpleName}")
                    DialogUtils.showToast(this, "Thêm danh mục mới")
                }
            }

            else -> {
                Log.d(TAG, "FAB clicked but no action for destination: $currentDestId")
                fab.hide()
            }
        }
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
        val options = arrayOf(
            "Quản lý dữ liệu mẫu",
            "Thông tin hệ thống",
            "Cài đặt chung",
            "Thông tin phiên bản",
            "🔧 SQLite Debug Tool" // Thêm option mới
        )

        AlertDialog.Builder(this)
            .setTitle("Cài đặt hệ thống")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showDataManagementMenu()
                    1 -> showSystemInfo()
                    2 -> showGeneralSettings()
                    3 -> showVersionInfo()
                    4 -> showSqliteDebugTool() // Thêm case mới
                }
            }
            .show()
    }

    /**
     * Hiển thị menu quản lý dữ liệu (Debug Menu)
     */
    private fun showDataManagementMenu() {
        val options = arrayOf(
            "Xem thống kê dữ liệu",
            "Tạo dữ liệu mẫu",
            "Reset toàn bộ database",
            "Xóa tất cả dữ liệu",
            "Force tạo dữ liệu mẫu",
            "🔍 SQL Query Tool" // Thêm option SQL Query
        )

        AlertDialog.Builder(this)
            .setTitle("Quản lý dữ liệu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showDataStatistics()
                    1 -> createSampleData()
                    2 -> resetDatabase()
                    3 -> clearAllData()
                    4 -> forceCreateSampleData()
                    5 -> showSqlQueryTool() // Thêm case mới
                }
            }
            .show()
    }

    /**
     * Hiển thị dialog hỏi có muốn tạo dữ liệu mẫu không
     */
    private fun showSampleDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Tạo dữ liệu mẫu")
            .setMessage("Hệ thống chưa có đủ dữ liệu để test. Bạn có muốn tạo dữ liệu mẫu không?\n\nDữ liệu mẫu bao gồm:\n• Tài khoản test\n• Sách mẫu\n• Đơn hàng mẫu\n• Đánh giá mẫu")
            .setPositiveButton("Tạo ngay") { _, _ ->
                sampleDataManager.insertSampleDataAsync { success ->
                    if (success) {
                        // Có thể refresh UI nếu cần
                        DialogUtils.showToast(this, "Dữ liệu mẫu đã sẵn sàng!")
                    }
                }
            }
            .setNegativeButton("Bỏ qua", null)
            .setCancelable(false)
            .show()
    }

    /**
     * Hiển thị thống kê dữ liệu
     */
    private fun showDataStatistics() {
        val stats = sampleDataManager.getDataStatistics()
        val message = """
            📊 THỐNG KÊ DỮ LIỆU HIỆN TẠI
            
            👥 Users: ${stats.userCount}
            📂 Categories: ${stats.categoryCount}  
            📚 Books: ${stats.bookCount}
            🛒 Orders: ${stats.orderCount}
            ⭐ Reviews: ${stats.reviewCount}
            🛍️ Cart Items: ${stats.cartItemCount}
            🔔 Notifications: ${stats.notificationCount}
            
            📈 Tổng cộng: ${stats.getTotalRecords()} bản ghi
            
            ${if (stats.getTotalRecords() > 100) "✅ Dữ liệu đầy đủ cho test" else "⚠️ Cần thêm dữ liệu"}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Thống kê Database")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Refresh") { _, _ ->
                showDataStatistics() // Refresh
            }
            .setNeutralButton("🔍 SQL Tool") { _, _ ->
                showSqlQueryTool() // Thêm nút truy cập nhanh
            }
            .show()

        // In ra log để debug
        sampleDataManager.printStatistics()
    }

    /**
     * Tạo dữ liệu mẫu an toàn
     */
    private fun createSampleData() {
        AlertDialog.Builder(this)
            .setTitle("Tạo dữ liệu mẫu")
            .setMessage("Tạo dữ liệu mẫu an toàn (không ghi đè dữ liệu có sẵn).\n\nDữ liệu sẽ được thêm vào:\n• Sách mẫu\n• Đơn hàng test\n• Đánh giá mẫu\n• Thông báo")
            .setPositiveButton("Tạo") { _, _ ->
                sampleDataManager.insertSampleDataAsync { success ->
                    if (success) {
                        showDataStatistics() // Show updated stats
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    /**
     * Force tạo dữ liệu mẫu (có thể ghi đè)
     */
    private fun forceCreateSampleData() {
        AlertDialog.Builder(this)
            .setTitle("Force tạo dữ liệu mẫu")
            .setMessage("⚠️ CẢNH BÁO: Force tạo dữ liệu có thể ghi đè một số dữ liệu trùng lặp.\n\nChỉ sử dụng khi:\n• Database bị lỗi\n• Cần reset dữ liệu test\n• Dữ liệu không đầy đủ")
            .setPositiveButton("Force tạo") { _, _ ->
                sampleDataManager.forceInsertSampleDataAsync { success ->
                    if (success) {
                        showDataStatistics()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    /**
     * Reset toàn bộ database
     */
    private fun resetDatabase() {
        AlertDialog.Builder(this)
            .setTitle("Reset Database")
            .setMessage("🔄 RESET TOÀN BỘ DATABASE\n\n⚠️ Tất cả dữ liệu hiện tại sẽ bị XÓA và thay thế bằng dữ liệu mẫu mới.\n\nBao gồm:\n• Tài khoản (trừ admin)\n• Sách và danh mục\n• Đơn hàng và reviews\n• Giỏ hàng và thông báo")
            .setPositiveButton("Reset ngay") { _, _ ->
                sampleDataManager.resetDatabaseAsync { success ->
                    if (success) {
                        DialogUtils.showToast(this, "Database đã được reset hoàn toàn!")
                        showDataStatistics()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    /**
     * Xóa tất cả dữ liệu
     */
    private fun clearAllData() {
        AlertDialog.Builder(this)
            .setTitle("Xóa tất cả dữ liệu")
            .setMessage("🗑️ XÓA TẤT CẢ DỮ LIỆU\n\n❌ NGUY HIỂM: Hành động này sẽ xóa TOÀN BỘ dữ liệu trong database và KHÔNG THỂ hoàn tác!\n\nSau khi xóa, bạn sẽ cần:\n• Tạo lại dữ liệu mẫu\n• Hoặc nhập dữ liệu thực")
            .setPositiveButton("XÓA TẤT CẢ") { _, _ ->
                // Double confirmation for dangerous action
                AlertDialog.Builder(this)
                    .setTitle("XÁC NHẬN CUỐI CÙNG")
                    .setMessage("Bạn có CHẮC CHẮN muốn xóa tất cả dữ liệu?")
                    .setPositiveButton("Chắc chắn xóa") { _, _ ->
                        sampleDataManager.clearAllDataAsync { success ->
                            if (success) {
                                showDataStatistics()
                            }
                        }
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    /**
     * Hiển thị thông tin hệ thống
     */
    private fun showSystemInfo() {
        val message = """
            📱 THÔNG TIN HỆ THỐNG
            
            🏢 Ứng dụng: StarBooks Management
            👤 Người dùng: ${sharedPrefs.getString("full_name", "Administrator")}
            🔑 Quyền: ${sharedPrefs.getUserRole().uppercase()}
            🆔 User ID: ${sharedPrefs.getUserId()}
            
            📊 Database: SQLite
            🗂️ Phiên bản DB: 1
            💾 Tình trạng: ${if (sampleDataManager.hasSampleData()) "Có dữ liệu" else "Trống"}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Thông tin hệ thống")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Hiển thị cài đặt chung
     */
    private fun showGeneralSettings() {
        DialogUtils.showInfoDialog(this, "Cài đặt chung", "Tính năng cài đặt chung đang phát triển")
    }

    /**
     * Hiển thị thông tin phiên bản
     */
    private fun showVersionInfo() {
        val message = """
            📋 THÔNG TIN PHIÊN BẢN
            
            🏷️ Tên: StarBooks 
            🔢 Version: 1.0.0
            📅 Build: ${java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date())}
            
            👨‍💻 Developer: Đặng Ngọc Đức - Nguyễn Đức Anh
            📧 Support: dangngocduc542@gmail.com - nguyenducanh@gmail.com
            
            🔧 Tính năng mới:
            • Export báo cáo PDF/CSV
            • SQLite Debug Tool
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Phiên bản")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * 🔧 TÍNH NĂNG MỚI: SQLite Debug Tool
     */
    private fun showSqliteDebugTool() {
        val options = arrayOf(
            "🔍 SQL Query Tool",
            "📊 Quick Table Stats",
            "⚡ Common Queries",
            "🛠️ Database Info",
            "📝 Insert Test Data"
        )

        AlertDialog.Builder(this)
            .setTitle("🔧 SQLite Debug Tool")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSqlQueryTool()
                    1 -> showQuickTableStats()
                    2 -> showCommonQueries()
                    3 -> showDatabaseInfo()
                    4 -> insertTestData()
                }
            }
            .show()
    }

    /**
     * 🔍 SQL Query Tool - Giao diện nhập và thực thi SQL
     */
    private fun showSqlQueryTool() {
        val editText = EditText(this).apply {
            hint = "Nhập câu lệnh SQL (SELECT, INSERT, UPDATE, DELETE...)"
            setText("SELECT name FROM sqlite_master WHERE type='table';")
            setSingleLine(false)
            minLines = 4
            maxLines = 8
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        AlertDialog.Builder(this)
            .setTitle("🔍 SQL Query Tool")
            .setView(editText)
            .setPositiveButton("Thực thi") { _, _ ->
                val sql = editText.text.toString().trim()
                if (sql.isNotEmpty()) {
                    executeSqlQuery(sql)
                } else {
                    DialogUtils.showToast(this, "Vui lòng nhập câu lệnh SQL")
                }
            }
            .setNegativeButton("Hủy", null)
            .setNeutralButton("Mẫu Query") { _, _ ->
                showQueryTemplates()
            }
            .show()
    }

    /**
     * Thực thi câu lệnh SQL và hiển thị kết quả
     */
    private fun executeSqlQuery(sql: String) {
        try {
            val dbHelper = DatabaseHelper(this)
            val db = dbHelper.readableDatabase

            Log.d(TAG, "Executing SQL: $sql")

            if (sql.trim().uppercase().startsWith("SELECT")) {
                // Query - hiển thị kết quả
                val cursor: Cursor = db.rawQuery(sql, null)
                displayQueryResults(cursor, sql)
            } else {
                // Non-query (INSERT, UPDATE, DELETE) - thực thi và hiển thị số dòng ảnh hưởng
                db.execSQL(sql)
                val changes = getDatabaseChanges(db)
                showSqlResult("✅ Thực thi thành công!\n\nCâu lệnh: $sql\n\n$changes")
            }

            db.close()
        } catch (e: Exception) {
            Log.e(TAG, "SQL Execution Error: ${e.message}", e)
            showSqlResult("❌ Lỗi SQL: ${e.message}\n\nCâu lệnh: $sql")
        }
    }

    /**
     * Hiển thị kết quả query dạng bảng
     */
    private fun displayQueryResults(cursor: Cursor, sql: String) {
        if (cursor.count == 0) {
            showSqlResult("📭 Không có dữ liệu\n\nCâu lệnh: $sql")
            cursor.close()
            return
        }

        val columnCount = cursor.columnCount
        val result = StringBuilder()
        result.append("✅ Tìm thấy ${cursor.count} dòng\n\n")
        result.append("Câu lệnh: $sql\n\n")

        // Header
        result.append("| ")
        for (i in 0 until columnCount) {
            result.append("${cursor.getColumnName(i)} | ")
        }
        result.append("\n")

        // Separator
        result.append("|")
        for (i in 0 until columnCount) {
            result.append("---|")
        }
        result.append("\n")

        // Data
        cursor.moveToFirst()
        do {
            result.append("| ")
            for (i in 0 until columnCount) {
                val value = when (cursor.getType(i)) {
                    Cursor.FIELD_TYPE_NULL -> "NULL"
                    Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(i).toString()
                    Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(i).toString()
                    Cursor.FIELD_TYPE_STRING -> cursor.getString(i) ?: "NULL"
                    else -> "?"
                }
                result.append("$value | ")
            }
            result.append("\n")
        } while (cursor.moveToNext())

        cursor.close()
        showSqlResult(result.toString())
    }

    /**
     * Hiển thị kết quả SQL trong dialog scrollable
     */
    private fun showSqlResult(result: String) {
        val textView = TextView(this).apply {
            text = result
            textSize = 12f
            setPadding(16, 16, 16, 16)
        }

        val scrollView = ScrollView(this).apply {
            addView(textView)
        }

        AlertDialog.Builder(this)
            .setTitle("📊 Kết quả SQL")
            .setView(scrollView)
            .setPositiveButton("OK", null)
            .setNeutralButton("Sao chép") { _, _ ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("SQL Result", result)
                clipboard.setPrimaryClip(clip)
                DialogUtils.showToast(this, "Đã sao chép kết quả")
            }
            .show()
    }

    /**
     * 📊 Quick Table Stats - Thống kê nhanh các bảng
     */
    private fun showQuickTableStats() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val tables = arrayOf(
            "users", "books", "categories", "orders",
            "order_items", "reviews", "cart_items", "notifications"
        )

        val stats = StringBuilder()
        stats.append("📊 THỐNG KÊ NHANH\n\n")

        tables.forEach { tableName ->
            try {
                val cursor = db.rawQuery("SELECT COUNT(*) FROM $tableName", null)
                cursor.moveToFirst()
                val count = cursor.getInt(0)
                cursor.close()
                stats.append("• $tableName: $count bản ghi\n")
            } catch (e: Exception) {
                stats.append("• $tableName: Lỗi\n")
            }
        }

        db.close()

        AlertDialog.Builder(this)
            .setTitle("📊 Quick Table Stats")
            .setMessage(stats.toString())
            .setPositiveButton("OK", null)
            .setNeutralButton("Chi tiết") { _, _ -> showDataStatistics() }
            .show()
    }

    /**
     * ⚡ Common Queries - Các query thông dụng
     */
    private fun showCommonQueries() {
        val queries = arrayOf(
            "👥 Top 5 users mới nhất" to "SELECT id, username, email, created_at FROM users ORDER BY created_at DESC LIMIT 5",
            "📚 Sách bán chạy" to "SELECT id, title, author, sold_count FROM books ORDER BY sold_count DESC LIMIT 5",
            "🛒 Đơn hàng gần đây" to "SELECT id, order_code, customer_name, final_amount, status FROM orders ORDER BY order_date DESC LIMIT 5",
            "⭐ Sách đánh giá cao" to "SELECT id, title, author, rating, review_count FROM books WHERE rating >= 4.0 ORDER BY rating DESC LIMIT 5",
            "📂 Danh mục và số sách" to "SELECT c.name, COUNT(b.id) as book_count FROM categories c LEFT JOIN books b ON c.id = b.category_id GROUP BY c.id, c.name"
        )

        val options = queries.map { it.first }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("⚡ Common Queries")
            .setItems(options) { _, which ->
                val (_, query) = queries[which]
                executeSqlQuery(query)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    /**
     * 🛠️ Database Info - Thông tin database
     */
    private fun showDatabaseInfo() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val info = StringBuilder()
        info.append("🛠️ DATABASE INFO\n\n")

        // Tables info
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name",
            null
        )

        info.append("📋 Tables (${cursor.count}):\n")
        cursor.moveToFirst()
        do {
            val tableName = cursor.getString(0)
            val countCursor = db.rawQuery("SELECT COUNT(*) FROM $tableName", null)
            countCursor.moveToFirst()
            val count = countCursor.getInt(0)
            countCursor.close()
            info.append("• $tableName: $count records\n")
        } while (cursor.moveToNext())
        cursor.close()

        // Database file info
        val dbFile = getDatabasePath(DatabaseHelper.DATABASE_NAME)
        info.append("\n💾 File: ${dbFile.absolutePath}\n")
        info.append("📏 Size: ${dbFile.length() / 1024} KB\n")

        db.close()

        AlertDialog.Builder(this)
            .setTitle("🛠️ Database Info")
            .setMessage(info.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * 📝 Insert Test Data - Chèn dữ liệu test
     */
    private fun insertTestData() {
        val options = arrayOf(
            "👥 Thêm user test",
            "📚 Thêm sách test",
            "🛒 Thêm đơn hàng test",
            "⭐ Thêm review test"
        )

        AlertDialog.Builder(this)
            .setTitle("📝 Insert Test Data")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> executeSqlQuery("INSERT INTO users (username, email, full_name, password, role) VALUES ('test_user', 'test@email.com', 'Test User', 'password123', 'customer')")
                    1 -> executeSqlQuery("INSERT INTO books (title, author, price, stock, category_id) VALUES ('Sách Test', 'Tác giả Test', 100000, 10, 1)")
                    2 -> executeSqlQuery("INSERT INTO orders (order_code, customer_id, customer_name, total_amount, final_amount) VALUES ('TEST001', 1, 'Test Customer', 100000, 100000)")
                    3 -> executeSqlQuery("INSERT INTO reviews (book_id, user_id, user_name, rating, comment) VALUES (1, 1, 'Test User', 5.0, 'Review test')")
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    /**
     * Hiển thị các mẫu query template
     */
    private fun showQueryTemplates() {
        val templates = arrayOf(
            "SELECT * FROM users LIMIT 10",
            "SELECT * FROM books WHERE price > 100000",
            "SELECT name, COUNT(*) as count FROM categories GROUP BY name",
            "UPDATE users SET status='active' WHERE status='inactive'",
            "DELETE FROM cart_items WHERE user_id=1"
        )

        AlertDialog.Builder(this)
            .setTitle("📝 Query Templates")
            .setItems(templates) { _, which ->
                showSqlQueryToolWithTemplate(templates[which])
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showSqlQueryToolWithTemplate(template: String) {
        val editText = EditText(this).apply {
            setText(template)
            setSingleLine(false)
            minLines = 4
            maxLines = 8
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        AlertDialog.Builder(this)
            .setTitle("🔍 SQL Query Tool")
            .setView(editText)
            .setPositiveButton("Thực thi") { _, _ ->
                val sql = editText.text.toString().trim()
                if (sql.isNotEmpty()) {
                    executeSqlQuery(sql)
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    /**
     * Lấy thông tin thay đổi database
     */
    private fun getDatabaseChanges(db: SQLiteDatabase): String {
        return try {
            val changesCursor = db.rawQuery("SELECT changes(), total_changes()", null)
            changesCursor.moveToFirst()
            val changes = changesCursor.getInt(0)
            val totalChanges = changesCursor.getInt(1)
            changesCursor.close()
            "📈 Thay đổi: $changes dòng (Tổng: $totalChanges)"
        } catch (e: Exception) {
            "📈 Không thể lấy thông tin thay đổi"
        }
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