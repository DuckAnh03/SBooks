package com.example.sbooks.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.sbooks.models.UserModel

object SessionManager {
    private const val PREF_NAME = "SBooksSession"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_PHONE = "phone"
    private const val KEY_ADDRESS = "address"
    private const val KEY_ROLE = "role"
    private const val KEY_AVATAR = "avatar"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserSession(context: Context, user: UserModel) {
        val editor = getPreferences(context).edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putInt(KEY_USER_ID, user.id)
        editor.putString(KEY_USERNAME, user.username)
        editor.putString(KEY_EMAIL, user.email)
        editor.putString(KEY_FULL_NAME, user.fullName)
        editor.putString(KEY_PHONE, user.phone)
        editor.putString(KEY_ADDRESS, user.address)
        editor.putString(KEY_ROLE, user.role.value)
        editor.putString(KEY_AVATAR, user.avatar)
        editor.apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserId(context: Context): Int {
        return getPreferences(context).getInt(KEY_USER_ID, 0)
    }

    fun getUsername(context: Context): String {
        return getPreferences(context).getString(KEY_USERNAME, "") ?: ""
    }

    fun getEmail(context: Context): String {
        return getPreferences(context).getString(KEY_EMAIL, "") ?: ""
    }

    fun getFullName(context: Context): String {
        return getPreferences(context).getString(KEY_FULL_NAME, "") ?: ""
    }

    fun getPhone(context: Context): String {
        return getPreferences(context).getString(KEY_PHONE, "") ?: ""
    }

    fun getAddress(context: Context): String {
        return getPreferences(context).getString(KEY_ADDRESS, "") ?: ""
    }

    fun getRole(context: Context): String {
        return getPreferences(context).getString(KEY_ROLE, "") ?: ""
    }

    fun getAvatar(context: Context): String {
        return getPreferences(context).getString(KEY_AVATAR, "") ?: ""
    }

    fun getCurrentUser(context: Context): UserModel? {
        if (!isLoggedIn(context)) return null

        return UserModel(
            id = getUserId(context),
            username = getUsername(context),
            email = getEmail(context),
            phone = getPhone(context),
            fullName = getFullName(context),
            address = getAddress(context),
            password = "", // Don't store password
            role = UserModel.UserRole.fromValue(getRole(context)),
            status = UserModel.UserStatus.ACTIVE,
            avatar = getAvatar(context),
            createdAt = "",
            updatedAt = ""
        )
    }

    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }

    fun logout(context: Context) {
        clearSession(context)
    }
}