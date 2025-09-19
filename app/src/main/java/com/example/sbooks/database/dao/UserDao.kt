package com.example.sbooks.database.dao
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.models.UserModel

class UserDao(private val db: SQLiteDatabase) {

    fun insertUser(user: UserModel): Long {
        val values = ContentValues().apply {
            put("username", user.username)
            put("email", user.email)
            put("phone", user.phone)
            put("full_name", user.fullName)
            put("address", user.address)
            put("password", user.password)
            put("role", user.role.value)
            put("status", user.status.value)
            put("avatar", user.avatar)
        }
        return db.insert(DatabaseHelper.TABLE_USERS, null, values)
    }

    fun updateUser(user: UserModel): Int {
        val values = ContentValues().apply {
            put("username", user.username)
            put("email", user.email)
            put("phone", user.phone)
            put("full_name", user.fullName)
            put("address", user.address)
            put("role", user.role.value)
            put("status", user.status.value)
            put("avatar", user.avatar)
            put("updated_at", "datetime('now')")
        }
        return db.update(DatabaseHelper.TABLE_USERS, values, "id = ?", arrayOf(user.id.toString()))
    }

    fun deleteUser(userId: Int): Int {
        return db.delete(DatabaseHelper.TABLE_USERS, "id = ?", arrayOf(userId.toString()))
    }

    fun getUserById(userId: Int): UserModel? {
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "id = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) cursorToUser(it) else null
        }
    }

    fun getUserByUsername(username: String): UserModel? {
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "username = ?",
            arrayOf(username),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) cursorToUser(it) else null
        }
    }

    fun getUserByEmail(email: String): UserModel? {
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "email = ?",
            arrayOf(email),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) cursorToUser(it) else null
        }
    }

    fun getAllUsers(): List<UserModel> {
        val users = mutableListOf<UserModel>()
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            null, null, null, null,
            "created_at DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                users.add(cursorToUser(it))
            }
        }
        return users
    }

    fun getUsersByRole(role: UserModel.UserRole): List<UserModel> {
        val users = mutableListOf<UserModel>()
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "role = ?",
            arrayOf(role.value),
            null, null,
            "created_at DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                users.add(cursorToUser(it))
            }
        }
        return users
    }

    fun searchUsers(query: String, role: String? = null): List<UserModel> {
        val users = mutableListOf<UserModel>()
        val selection = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        selection.add("(username LIKE ? OR email LIKE ? OR full_name LIKE ?)")
        val searchQuery = "%$query%"
        selectionArgs.addAll(listOf(searchQuery, searchQuery, searchQuery))

        if (role != null && role != "all") {
            selection.add("role = ?")
            selectionArgs.add(role)
        }

        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            selection.joinToString(" AND "),
            selectionArgs.toTypedArray(),
            null, null,
            "created_at DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                users.add(cursorToUser(it))
            }
        }
        return users
    }

    fun validateLogin(username: String, password: String): UserModel? {
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "(username = ? OR email = ?) AND password = ? AND status = 'active'",
            arrayOf(username, username, password),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) cursorToUser(it) else null
        }
    }

    private fun cursorToUser(cursor: Cursor): UserModel {
        return UserModel(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
            email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
            phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")) ?: "",
            fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name")) ?: "",
            address = cursor.getString(cursor.getColumnIndexOrThrow("address")) ?: "",
            password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
            role = UserModel.UserRole.fromValue(cursor.getString(cursor.getColumnIndexOrThrow("role"))),
            status = UserModel.UserStatus.fromValue(cursor.getString(cursor.getColumnIndexOrThrow("status"))),
            avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatar")) ?: "",
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
            updatedAt = cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
        )
    }
}