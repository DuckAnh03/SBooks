package com.example.sbooks.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.sbooks.database.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Utility class để quản lý dữ liệu mẫu
 * Sử dụng để test các chức năng của app
 */
class SampleDataManager(private val context: Context) {

    private val TAG = "SampleDataManager"
    private val dbHelper = DatabaseHelper(context)

    /**
     * Kiểm tra xem đã có dữ liệu mẫu chưa
     */
    fun hasSampleData(): Boolean {
        val db = dbHelper.readableDatabase
        return try {
            val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_BOOKS}", null)
            cursor.moveToFirst()
            val count = cursor.getInt(0)
            cursor.close()
            count > 10 // Nếu có hơn 10 sách thì coi như đã có dữ liệu mẫu
        } catch (e: Exception) {
            Log.e(TAG, "Error checking sample data", e)
            false
        } finally {
            db.close()
        }
    }

    /**
     * Insert dữ liệu mẫu (async) - An toàn, không ghi đè dữ liệu có sẵn
     */
    fun insertSampleDataAsync(onComplete: (Boolean) -> Unit = {}) {
        GlobalScope.launch(Dispatchers.IO) {
            val success = try {
                dbHelper.insertAdditionalSampleData()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting sample data", e)
                false
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "Đã tạo dữ liệu mẫu thành công!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Lỗi khi tạo dữ liệu mẫu", Toast.LENGTH_LONG).show()
                }
                onComplete(success)
            }
        }
    }

    /**
     * Force insert dữ liệu mẫu (ghi đè dữ liệu có sẵn nếu trùng)
     */
    fun forceInsertSampleDataAsync(onComplete: (Boolean) -> Unit = {}) {
        GlobalScope.launch(Dispatchers.IO) {
            val success = try {
                dbHelper.forceInsertSampleData()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error force inserting sample data", e)
                false
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "Đã force tạo dữ liệu mẫu thành công!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Lỗi khi force tạo dữ liệu mẫu", Toast.LENGTH_LONG).show()
                }
                onComplete(success)
            }
        }
    }

    /**
     * Xóa tất cả dữ liệu (async)
     */
    fun clearAllDataAsync(onComplete: (Boolean) -> Unit = {}) {
        GlobalScope.launch(Dispatchers.IO) {
            val success = try {
                dbHelper.clearAllData()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing data", e)
                false
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "Đã xóa tất cả dữ liệu!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Lỗi khi xóa dữ liệu", Toast.LENGTH_LONG).show()
                }
                onComplete(success)
            }
        }
    }

    /**
     * Reset toàn bộ database (xóa và tạo lại dữ liệu mẫu)
     */
    fun resetDatabaseAsync(onComplete: (Boolean) -> Unit = {}) {
        GlobalScope.launch(Dispatchers.IO) {
            val success = try {
                // Xóa dữ liệu cũ
                dbHelper.clearAllData()

                // Tạo dữ liệu mẫu mới
                dbHelper.insertAdditionalSampleData()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting database", e)
                false
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "Đã reset database thành công!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Lỗi khi reset database", Toast.LENGTH_LONG).show()
                }
                onComplete(success)
            }
        }
    }

    /**
     * Lấy thống kê dữ liệu hiện tại
     */
    fun getDataStatistics(): DatabaseStats {
        val db = dbHelper.readableDatabase
        return try {
            val stats = DatabaseStats()

            // Đếm users
            var cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_USERS}", null)
            if (cursor.moveToFirst()) {
                stats.userCount = cursor.getInt(0)
            }
            cursor.close()

            // Đếm categories
            cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_CATEGORIES}", null)
            if (cursor.moveToFirst()) {
                stats.categoryCount = cursor.getInt(0)
            }
            cursor.close()

            // Đếm books
            cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_BOOKS}", null)
            if (cursor.moveToFirst()) {
                stats.bookCount = cursor.getInt(0)
            }
            cursor.close()

            // Đếm orders
            cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_ORDERS}", null)
            if (cursor.moveToFirst()) {
                stats.orderCount = cursor.getInt(0)
            }
            cursor.close()

            // Đếm reviews
            cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_REVIEWS}", null)
            if (cursor.moveToFirst()) {
                stats.reviewCount = cursor.getInt(0)
            }
            cursor.close()

            // Đếm cart items
            cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_CART_ITEMS}", null)
            if (cursor.moveToFirst()) {
                stats.cartItemCount = cursor.getInt(0)
            }
            cursor.close()

            // Đếm notifications
            cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_NOTIFICATIONS}", null)
            if (cursor.moveToFirst()) {
                stats.notificationCount = cursor.getInt(0)
            }
            cursor.close()

            stats
        } catch (e: Exception) {
            Log.e(TAG, "Error getting statistics", e)
            DatabaseStats()
        } finally {
            db.close()
        }
    }

    /**
     * In thông tin thống kê ra log
     */
    fun printStatistics() {
        val stats = getDataStatistics()
        Log.i(TAG, "=== Database Statistics ===")
        Log.i(TAG, "Users: ${stats.userCount}")
        Log.i(TAG, "Categories: ${stats.categoryCount}")
        Log.i(TAG, "Books: ${stats.bookCount}")
        Log.i(TAG, "Orders: ${stats.orderCount}")
        Log.i(TAG, "Reviews: ${stats.reviewCount}")
        Log.i(TAG, "Cart Items: ${stats.cartItemCount}")
        Log.i(TAG, "Notifications: ${stats.notificationCount}")
        Log.i(TAG, "==========================")
    }
}

/**
 * Data class chứa thống kê database
 */
data class DatabaseStats(
    var userCount: Int = 0,
    var categoryCount: Int = 0,
    var bookCount: Int = 0,
    var orderCount: Int = 0,
    var reviewCount: Int = 0,
    var cartItemCount: Int = 0,
    var notificationCount: Int = 0
) {
    fun getTotalRecords(): Int {
        return userCount + categoryCount + bookCount + orderCount + reviewCount + cartItemCount + notificationCount
    }
}