package com.example.sbooks.database.dao
import com.example.sbooks.models.CategoryModel
import com.example.sbooks.database.DatabaseHelper
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class CategoryDao(private val db: SQLiteDatabase) {

    fun insertCategory(category: CategoryModel): Long {
        val values = ContentValues().apply {
            put("name", category.name)
            put("description", category.description)
            put("icon", category.icon)
            put("status", category.status.value)
            put("sort_order", category.sortOrder)
        }
        return db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values)
    }

    fun updateCategory(category: CategoryModel): Int {
        val values = ContentValues().apply {
            put("name", category.name)
            put("description", category.description)
            put("icon", category.icon)
            put("status", category.status.value)
            put("sort_order", category.sortOrder)
            put("updated_at", "datetime('now')")
        }
        return db.update(DatabaseHelper.TABLE_CATEGORIES, values, "id = ?", arrayOf(category.id.toString()))
    }

    fun deleteCategory(categoryId: Int): Int {
        return db.delete(DatabaseHelper.TABLE_CATEGORIES, "id = ?", arrayOf(categoryId.toString()))
    }

    fun getCategoryById(categoryId: Int): CategoryModel? {
        val cursor = db.rawQuery("""
            SELECT c.*, 
                   COUNT(b.id) as book_count
            FROM ${DatabaseHelper.TABLE_CATEGORIES} c
            LEFT JOIN ${DatabaseHelper.TABLE_BOOKS} b ON c.id = b.category_id
            WHERE c.id = ?
            GROUP BY c.id
        """, arrayOf(categoryId.toString()))

        return cursor.use {
            if (it.moveToFirst()) cursorToCategory(it) else null
        }
    }

    fun getAllCategories(): List<CategoryModel> {
        val categories = mutableListOf<CategoryModel>()
        val cursor = db.rawQuery("""
            SELECT c.*, 
                   COUNT(b.id) as book_count
            FROM ${DatabaseHelper.TABLE_CATEGORIES} c
            LEFT JOIN ${DatabaseHelper.TABLE_BOOKS} b ON c.id = b.category_id
            GROUP BY c.id
            ORDER BY c.sort_order, c.name
        """, null)

        cursor.use {
            while (it.moveToNext()) {
                categories.add(cursorToCategory(it))
            }
        }
        return categories
    }

    fun getActiveCategories(): List<CategoryModel> {
        val categories = mutableListOf<CategoryModel>()
        val cursor = db.rawQuery("""
            SELECT c.*, 
                   COUNT(b.id) as book_count
            FROM ${DatabaseHelper.TABLE_CATEGORIES} c
            LEFT JOIN ${DatabaseHelper.TABLE_BOOKS} b ON c.id = b.category_id
            WHERE c.status = 'active'
            GROUP BY c.id
            ORDER BY c.sort_order, c.name
        """, null)

        cursor.use {
            while (it.moveToNext()) {
                categories.add(cursorToCategory(it))
            }
        }
        return categories
    }

    fun searchCategories(query: String): List<CategoryModel> {
        val categories = mutableListOf<CategoryModel>()
        val cursor = db.rawQuery("""
            SELECT c.*, 
                   COUNT(b.id) as book_count
            FROM ${DatabaseHelper.TABLE_CATEGORIES} c
            LEFT JOIN ${DatabaseHelper.TABLE_BOOKS} b ON c.id = b.category_id
            WHERE c.name LIKE ? OR c.description LIKE ?
            GROUP BY c.id
            ORDER BY c.sort_order, c.name
        """, arrayOf("%$query%", "%$query%"))

        cursor.use {
            while (it.moveToNext()) {
                categories.add(cursorToCategory(it))
            }
        }
        return categories
    }

    private fun cursorToCategory(cursor: Cursor): CategoryModel {
        return CategoryModel(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: "",
            icon = cursor.getString(cursor.getColumnIndexOrThrow("icon")) ?: "",
            bookCount = cursor.getInt(cursor.getColumnIndexOrThrow("book_count")),
            status = CategoryModel.CategoryStatus.fromValue(cursor.getString(cursor.getColumnIndexOrThrow("status"))),
            sortOrder = cursor.getInt(cursor.getColumnIndexOrThrow("sort_order")),
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
            updatedAt = cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
        )
    }
}