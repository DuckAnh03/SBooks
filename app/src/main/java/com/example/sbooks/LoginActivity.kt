package com.example.sbooks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.sbooks.activities.admin.AdminMainActivity
import com.example.sbooks.activities.staff.StaffMainActivity
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.UserDao
import com.example.sbooks.utils.Constants
import com.example.sbooks.utils.DialogUtils
import com.example.sbooks.utils.SharedPrefsHelper
import com.example.sbooks.utils.ValidationUtils

class LoginActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "LoginActivity_Back"
    }

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    private lateinit var userDao: UserDao
    private lateinit var sharedPrefs: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting LoginActivity. Intent: $intent")
        setContentView(R.layout.activity_login)

        initializeViews()
        setupDatabase()
        setupLoginButton()
        setupRegisterButton()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "OnBackPressedCallback.handleOnBackPressed called. isTaskRoot: $isTaskRoot")
                if (isTaskRoot) {
                    Log.d(TAG, "LoginActivity is the root of the task. Finishing activity.")
                    finish()
                } else {
                    Log.d(TAG, "LoginActivity is not root. Finishing to reveal activity below.")
                    finish()
                }
            }
        })
        Log.d(TAG, "onCreate: LoginActivity setup complete.")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Checking existing session.")
        checkExistingSession()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d(TAG, "onNewIntent: Received new intent. Intent: $intent")
    }

    private fun initializeViews() {
        Log.d(TAG, "initializeViews")
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        btnRegister = findViewById(R.id.btnRegister)
    }

    private fun setupDatabase() {
        Log.d(TAG, "setupDatabase")
        val dbHelper = DatabaseHelper(this)
        userDao = UserDao(dbHelper.writableDatabase)
        sharedPrefs = SharedPrefsHelper(this)
    }

    private fun checkExistingSession() {
        if (sharedPrefs.isLoggedIn()) {
            Log.d(TAG, "checkExistingSession: User is logged in. Role: ${sharedPrefs.getUserRole()}")
            if (!isFinishing) {
                redirectToAppropriateActivity(sharedPrefs.getUserRole() ?: "")
            } else {
                Log.d(TAG, "checkExistingSession: Activity is already finishing, skipping redirect.")
            }
        } else {
            Log.d(TAG, "checkExistingSession: User is not logged in.")
        }
    }

    private fun setupLoginButton() {
        btnLogin.setOnClickListener {
            Log.d(TAG, "Login button clicked")
            performLogin()
        }
    }

    private fun setupRegisterButton() {
        btnRegister.setOnClickListener {
            DialogUtils.showInfoDialog(this, "Thông báo", "Chức năng đăng ký đang được phát triển.")
        }
    }

    private fun performLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        Log.d(TAG, "performLogin for username: $username")

        if (username.isEmpty()) {
            DialogUtils.showErrorDialog(this, "Vui lòng nhập tên đăng nhập") {
                finish()
            }
            return
        }
        if (password.isEmpty()) {
            DialogUtils.showErrorDialog(this, "Vui lòng nhập mật khẩu") {
                finish()
            }
            return
        }
        if (!ValidationUtils.isValidPassword(password)) {
            DialogUtils.showErrorDialog(
                this,
                "Mật khẩu phải có ít nhất ${Constants.MIN_PASSWORD_LENGTH} ký tự"
            ) {
                finish()
            }
            return
        }

        try {
            val user = userDao.validateLogin(username, password)
            if (user != null) {
                Log.i(TAG, "Login successful for user: ${user.username}, role: ${user.role.value}")
                sharedPrefs.saveUserSession(user.id, user.username, user.role.value)
                sharedPrefs.saveString("full_name", user.fullName)
                sharedPrefs.saveString("email", user.email)
                sharedPrefs.saveString("avatar", user.avatar)

                DialogUtils.showToast(this, Constants.SUCCESS_LOGIN)
                if (!isFinishing) {
                    redirectToAppropriateActivity(user.role.value)
                } else {
                    Log.d(TAG, "performLogin: Activity is already finishing, skipping redirect.")
                }
            } else {
                Log.w(TAG, "Login failed: Invalid username or password for $username")
                DialogUtils.showErrorDialog(this, "Tên đăng nhập hoặc mật khẩu không đúng") {
                    finish()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login exception for $username", e)
            DialogUtils.showErrorDialog(this, "Lỗi đăng nhập: ${e.message}") {
                finish()
            }
        }
    }

    private fun redirectToAppropriateActivity(userRole: String) {
        Log.d(TAG, "redirectToAppropriateActivity for role: $userRole. Current intent: $intent")

        if (isFinishing) {
            Log.w(TAG, "Activity is already finishing. Aborting redirect for role: $userRole")
            return
        }

        val targetIntent = when (userRole.lowercase()) {
            "admin" -> Intent(this, AdminMainActivity::class.java)
            "staff" -> Intent(this, StaffMainActivity::class.java)
            "customer" -> {
                Log.d(TAG, "Redirecting customer to HomeActivity")
                Intent(this, HomeActivity::class.java)
            }
            else -> {
                Log.e(TAG, "Invalid user role for redirection: '$userRole'")
                DialogUtils.showErrorDialog(this, "Vai trò người dùng không hợp lệ: '$userRole'") {
                    finish()
                }
                return
            }
        }

        Log.i(TAG, "Redirecting to ${targetIntent.component?.className} for role $userRole")
        targetIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(targetIntent)
        finish()
        Log.d(TAG, "LoginActivity finished after redirecting for role $userRole")
    }
}