package com.example.sbooks.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.sbooks.models.UserModel

class SharedPrefsHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    private companion object {
        private const val TAG = "SharedPrefsHelper"

        // Additional user info keys
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_ADDRESS = "address"
        private const val KEY_AVATAR = "avatar"
        private const val KEY_STATUS = "status"
    }

    /**
     * Save complete user session with all information
     */
    fun saveUserSession(userId: Int, username: String, userRole: String) {
        Log.d(TAG, "Saving user session: id=$userId, username=$username, role=$userRole")
        editor.apply {
            putInt(Constants.KEY_USER_ID, userId)
            putString(Constants.KEY_USERNAME, username)
            putString(Constants.KEY_USER_ROLE, userRole)
            putBoolean(Constants.KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    /**
     * Save full user session from UserModel
     */
    fun saveUserSession(user: UserModel) {
        Log.d(TAG, "Saving full user session: id=${user.id}, username=${user.username}")
        editor.apply {
            putInt(Constants.KEY_USER_ID, user.id)
            putString(Constants.KEY_USERNAME, user.username)
            putString(Constants.KEY_USER_ROLE, user.role.value)
            putString(KEY_EMAIL, user.email)
            putString(KEY_PHONE, user.phone)
            putString(KEY_FULL_NAME, user.fullName)
            putString(KEY_ADDRESS, user.address)
            putString(KEY_AVATAR, user.avatar)
            putString(KEY_STATUS, user.status.value)
            putBoolean(Constants.KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    /**
     * Update user information (without password)
     */
    fun updateUserInfo(user: UserModel) {
        Log.d(TAG, "Updating user info: id=${user.id}, email=${user.email}")
        editor.apply {
            putString(KEY_EMAIL, user.email)
            putString(KEY_PHONE, user.phone)
            putString(KEY_FULL_NAME, user.fullName)
            putString(KEY_ADDRESS, user.address)
            putString(KEY_AVATAR, user.avatar)
            apply()
        }
    }

    /**
     * Update user email
     */
    fun updateEmail(email: String) {
        Log.d(TAG, "Updating email: $email")
        editor.putString(KEY_EMAIL, email).apply()
    }

    /**
     * Update user phone
     */
    fun updatePhone(phone: String) {
        Log.d(TAG, "Updating phone: $phone")
        editor.putString(KEY_PHONE, phone).apply()
    }

    /**
     * Update user full name
     */
    fun updateFullName(fullName: String) {
        Log.d(TAG, "Updating full name: $fullName")
        editor.putString(KEY_FULL_NAME, fullName).apply()
    }

    /**
     * Update user address
     */
    fun updateAddress(address: String) {
        Log.d(TAG, "Updating address: $address")
        editor.putString(KEY_ADDRESS, address).apply()
    }

    /**
     * Update user avatar path
     */
    fun updateAvatar(avatarPath: String) {
        Log.d(TAG, "Updating avatar: $avatarPath")
        editor.putString(KEY_AVATAR, avatarPath).apply()
    }

    /**
     * Get current user as UserModel
     */
    fun getCurrentUser(): UserModel? {
        if (!isLoggedIn()) {
            return null
        }

        return try {
            UserModel(
                id = getUserId(),
                username = getUsername(),
                email = getEmail(),
                phone = getPhone(),
                fullName = getFullName(),
                address = getAddress(),
                password = "", // Don't store password in SharedPreferences
                role = UserModel.UserRole.fromValue(getUserRole()),
                status = UserModel.UserStatus.fromValue(getStatus()),
                avatar = getAvatar()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user", e)
            null
        }
    }

    fun clearUserSession() {
        Log.d(TAG, "Clearing user session")
        editor.apply {
            remove(Constants.KEY_USER_ID)
            remove(Constants.KEY_USERNAME)
            remove(Constants.KEY_USER_ROLE)
            remove(KEY_EMAIL)
            remove(KEY_PHONE)
            remove(KEY_FULL_NAME)
            remove(KEY_ADDRESS)
            remove(KEY_AVATAR)
            remove(KEY_STATUS)
            putBoolean(Constants.KEY_IS_LOGGED_IN, false)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        val loggedIn = prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false)
        return loggedIn
    }

    fun getUserId(): Int = prefs.getInt(Constants.KEY_USER_ID, -1)

    fun getUsername(): String = prefs.getString(Constants.KEY_USERNAME, "") ?: ""

    fun getUserRole(): String {
        val role = prefs.getString(Constants.KEY_USER_ROLE, "customer") ?: "customer"
        return role
    }

    /**
     * Get user email
     */
    fun getEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""

    /**
     * Get user phone
     */
    fun getPhone(): String = prefs.getString(KEY_PHONE, "") ?: ""

    /**
     * Get user full name
     */
    fun getFullName(): String = prefs.getString(KEY_FULL_NAME, "") ?: ""

    /**
     * Get user address
     */
    fun getAddress(): String = prefs.getString(KEY_ADDRESS, "") ?: ""

    /**
     * Get user avatar path
     */
    fun getAvatar(): String = prefs.getString(KEY_AVATAR, "") ?: ""

    /**
     * Get user status
     */
    fun getStatus(): String = prefs.getString(KEY_STATUS, "active") ?: "active"

    /**
     * Check if user is admin
     */
    fun isAdmin(): Boolean = getUserRole() == "admin"

    /**
     * Check if user is staff
     */
    fun isStaff(): Boolean = getUserRole() == "staff"

    /**
     * Check if user is customer
     */
    fun isCustomer(): Boolean = getUserRole() == "customer"

    fun saveString(key: String, value: String) {
        Log.d(TAG, "Saving string: key=$key, value=$value")
        editor.putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    fun saveInt(key: String, value: Int) {
        editor.putInt(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return prefs.getInt(key, defaultValue)
    }

    fun saveBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun remove(key: String) {
        editor.remove(key).apply()
    }

    fun clear() {
        Log.d(TAG, "Clearing all preferences")
        editor.clear().apply()
    }
}