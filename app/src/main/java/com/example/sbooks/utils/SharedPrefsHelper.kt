package com.example.sbooks.utils
import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SharedPrefsHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    private companion object {
        private const val TAG = "SharedPrefsHelper"
    }

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

    fun clearUserSession() {
        Log.d(TAG, "Clearing user session")
        editor.apply {
            remove(Constants.KEY_USER_ID)
            remove(Constants.KEY_USERNAME)
            remove(Constants.KEY_USER_ROLE)
            putBoolean(Constants.KEY_IS_LOGGED_IN, false)
            // Consider removing other user-specific data like full_name, email here too
            remove("full_name")
            remove("email")
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        val loggedIn = prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false)
        // Log.d(TAG, "isLoggedIn: $loggedIn") // Can be noisy, enable if needed
        return loggedIn
    }

    fun getUserId(): Int = prefs.getInt(Constants.KEY_USER_ID, -1)
    fun getUsername(): String = prefs.getString(Constants.KEY_USERNAME, "") ?: ""

    fun getUserRole(): String {
        val role = prefs.getString(Constants.KEY_USER_ROLE, "customer") ?: "customer" // Default to "customer"
        // Log.d(TAG, "getUserRole: $role") // Can be noisy
        return role
    }


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
        editor.clear().apply()
    }
}