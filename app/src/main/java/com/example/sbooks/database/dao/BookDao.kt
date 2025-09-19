package com.example.sbooks.database.dao
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.models.BookModel
import com.example.sbooks.models.BestSellerBookModel
import com.example.sbooks.models.SearchFilter
import com.example.sbooks.models.TopRatedBookModel

class BookDao(private val db: SQLiteDatabase) {

    fun insertBook(book: BookModel): Long {
        val values = ContentValues().apply {
            put("title", book.title)
            put("author", book.author)
            put("publisher", book.publisher)
            put("category_id", book.categoryId)
            put("price", book.price)
            put("stock", book.stock)
            put("description", book.description)
            put("image", book.image)
            put("isbn", book.isbn)
            put("pages", book.pages)
            put("language", book.language)
            put("publication_year", book.publicationYear)
            put("status", book.status.value)
        }
        return db.insert(DatabaseHelper.TABLE_BOOKS, null, values)
    }

    fun updateBook(book: BookModel): Int {
        val values = ContentValues().apply {
            put("title", book.title)
            put("author", book.author)
            put("publisher", book.publisher)
            put("category_id", book.categoryId)
            put("price", book.price)
            put("stock", book.stock)
            put("description", book.description)
            put("image", book.image)
            put("isbn", book.isbn)
            put("pages", book.pages)
            put("language", book.language)
            put("publication_year", book.publicationYear)
            put("status", book.status.value)
            put("updated_at", "datetime('now')")
        }
        return db.update(DatabaseHelper.TABLE_BOOKS, values, "id = ?", arrayOf(book.id.toString()))
    }

    fun updateBookStock(bookId: Int, newStock: Int): Int {
        val values = ContentValues().apply {
            put("stock", newStock)
            put("updated_at", "datetime('now')")
        }
        return db.update(DatabaseHelper.TABLE_BOOKS, values, "id = ?", arrayOf(bookId.toString()))
    }

    fun updateBookRating(bookId: Int, rating: Float, reviewCount: Int): Int {
        val values = ContentValues().apply {
            put("rating", rating)
            put("review_count", reviewCount)
            put("updated_at", "datetime('now')")
        }
        return db.update(DatabaseHelper.TABLE_BOOKS, values, "id = ?", arrayOf(bookId.toString()))
    }

    fun incrementSoldCount(bookId: Int, quantity: Int): Int {
        db.execSQL("""
            UPDATE ${DatabaseHelper.TABLE_BOOKS} 
            SET sold_count = sold_count + ?, 
                stock = stock - ?,
                updated_at = datetime('now')
            WHERE id = ?
        """, arrayOf(quantity, quantity, bookId))
        return 1
    }

    fun deleteBook(bookId: Int): Int {
        return db.delete(DatabaseHelper.TABLE_BOOKS, "id = ?", arrayOf(bookId.toString()))
    }

    fun getBookById(bookId: Int): BookModel? {
        val cursor = db.rawQuery("""
            SELECT b.*, c.name as category_name
            FROM ${DatabaseHelper.TABLE_BOOKS} b
            LEFT JOIN ${DatabaseHelper.TABLE_CATEGORIES} c ON b.category_id = c.id
            WHERE b.id = ?
        """, arrayOf(bookId.toString()))

        return cursor.use {
            if (it.moveToFirst()) cursorToBook(it) else null
        }
    }

    fun getAllBooks(): List<BookModel> {
        val books = mutableListOf<BookModel>()
        val cursor = db.rawQuery("""
            SELECT b.*, c.name as category_name
            FROM ${DatabaseHelper.TABLE_BOOKS} b
            LEFT JOIN ${DatabaseHelper.TABLE_CATEGORIES} c ON b.category_id = c.id
            ORDER BY b.created_at DESC
        """, null)

        cursor.use {
            while (it.moveToNext()) {
                books.add(cursorToBook(it))
            }
        }
        return books
    }

    fun getBooksByCategory(categoryId: Int): List<BookModel> {
        val books = mutableListOf<BookModel>()
        val cursor = db.rawQuery("""
            SELECT b.*, c.name as category_name
            FROM ${DatabaseHelper.TABLE_BOOKS} b
            LEFT JOIN ${DatabaseHelper.TABLE_CATEGORIES} c ON b.category_id = c.id
            WHERE b.category_id = ? AND b.status = 'active'
            ORDER BY b.title
        """, arrayOf(categoryId.toString()))

        cursor.use {
            while (it.moveToNext()) {
                books.add(cursorToBook(it))
            }
        }
        return books
    }

    fun searchBooks(filter: SearchFilter): List<BookModel> {
        val books = mutableListOf<BookModel>()
        val whereConditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        // Search query
        if (filter.query.isNotEmpty()) {
            whereConditions.add("(b.title LIKE ? OR b.author LIKE ? OR b.description LIKE ?)")
            val searchQuery = "%${filter.query}%"
            args.addAll(listOf(searchQuery, searchQuery, searchQuery))
        }

        // Category filter
        if (filter.categoryId != null && filter.categoryId > 0) {
            whereConditions.add("b.category_id = ?")
            args.add(filter.categoryId.toString())
        }

        // Price range filter
        if (filter.minPrice != null) {
            whereConditions.add("b.price >= ?")
            args.add(filter.minPrice.toString())
        }
        if (filter.maxPrice != null) {
            whereConditions.add("b.price <= ?")
            args.add(filter.maxPrice.toString())
        }

        // Author filter
        if (filter.authorFilter.isNotEmpty()) {
            whereConditions.add("b.author LIKE ?")
            args.add("%${filter.authorFilter}%")
        }

        // Publisher filter
        if (filter.publisherFilter.isNotEmpty()) {
            whereConditions.add("b.publisher LIKE ?")
            args.add("%${filter.publisherFilter}%")
        }

        // Stock filters
        if (filter.showLowStockOnly) {
            whereConditions.add("b.stock <= 10")
        }
        if (filter.showOutOfStockOnly) {
            whereConditions.add("b.stock = 0")
        }

        // Build WHERE clause
        val whereClause = if (whereConditions.isNotEmpty()) {
            "WHERE ${whereConditions.joinToString(" AND ")}"
        } else ""

        // Build ORDER BY clause
        // Build ORDER BY clause
        val orderBy = when (filter.sortBy) {
            SearchFilter.SortOption.NAME_ASC -> "ORDER BY b.title ASC"
            SearchFilter.SortOption.NAME_DESC -> "ORDER BY b.title DESC"
            SearchFilter.SortOption.PRICE_ASC -> "ORDER BY b.price ASC"
            SearchFilter.SortOption.PRICE_DESC -> "ORDER BY b.price DESC"
            SearchFilter.SortOption.STOCK_ASC -> "ORDER BY b.stock ASC"
            SearchFilter.SortOption.DATE_DESC -> "ORDER BY b.created_at DESC"
        }

        val query = """
            SELECT b.*, c.name as category_name
            FROM ${DatabaseHelper.TABLE_BOOKS} b
            LEFT JOIN ${DatabaseHelper.TABLE_CATEGORIES} c ON b.category_id = c.id
            $whereClause
            $orderBy
        """

        val cursor = db.rawQuery(query, args.toTypedArray())

        cursor.use {
            while (it.moveToNext()) {
                books.add(cursorToBook(it))
            }
        }
        return books
    }

    fun getLowStockBooks(threshold: Int = 10): List<BookModel> {
        val books = mutableListOf<BookModel>()
        val cursor = db.rawQuery("""
            SELECT b.*, c.name as category_name
            FROM ${DatabaseHelper.TABLE_BOOKS} b
            LEFT JOIN ${DatabaseHelper.TABLE_CATEGORIES} c ON b.category_id = c.id
            WHERE b.stock <= ? AND b.status = 'active'
            ORDER BY b.stock ASC
        """, arrayOf(threshold.toString()))

        cursor.use {
            while (it.moveToNext()) {
                books.add(cursorToBook(it))
            }
        }
        return books
    }

    fun getBestSellingBooks(limit: Int = 10): List<BestSellerBookModel> {
        val books = mutableListOf<BestSellerBookModel>()
        val cursor = db.rawQuery("""
            SELECT b.id, b.title, b.author, b.image, b.price, b.sold_count,
                   (b.price * b.sold_count) as revenue,
                   ROW_NUMBER() OVER (ORDER BY b.sold_count DESC) as rank
            FROM ${DatabaseHelper.TABLE_BOOKS} b
            WHERE b.sold_count > 0 AND b.status = 'active'
            ORDER BY b.sold_count DESC
            LIMIT ?
        """, arrayOf(limit.toString()))

        cursor.use {
            while (it.moveToNext()) {
                books.add(BestSellerBookModel(
                    bookId = it.getInt(it.getColumnIndexOrThrow("id")),
                    title = it.getString(it.getColumnIndexOrThrow("title")),
                    author = it.getString(it.getColumnIndexOrThrow("author")),
                    image = it.getString(it.getColumnIndexOrThrow("image")) ?: "",
                    price = it.getDouble(it.getColumnIndexOrThrow("price")),
                    soldQuantity = it.getInt(it.getColumnIndexOrThrow("sold_count")),
                    revenue = it.getDouble(it.getColumnIndexOrThrow("revenue")),
                    rank = it.getInt(it.getColumnIndexOrThrow("rank"))
                ))
            }
        }
        return books
    }

    fun getTopRatedBooks(limit: Int = 10): List<TopRatedBookModel> {
        val books = mutableListOf<TopRatedBookModel>()
        val cursor = db.rawQuery("""
            SELECT b.id, b.title, b.author, b.image, b.price, b.rating, b.review_count, b.stock
            FROM ${DatabaseHelper.TABLE_BOOKS} b
            WHERE b.rating >= 4.0 AND b.review_count >= 5 AND b.status = 'active'
            ORDER BY b.rating DESC, b.review_count DESC
            LIMIT ?
        """, arrayOf(limit.toString()))

        cursor.use {
            while (it.moveToNext()) {
                books.add(TopRatedBookModel(
                    bookId = it.getInt(it.getColumnIndexOrThrow("id")),
                    title = it.getString(it.getColumnIndexOrThrow("title")),
                    author = it.getString(it.getColumnIndexOrThrow("author")),
                    image = it.getString(it.getColumnIndexOrThrow("image")) ?: "",
                    price = it.getDouble(it.getColumnIndexOrThrow("price")),
                    rating = it.getFloat(it.getColumnIndexOrThrow("rating")),
                    reviewCount = it.getInt(it.getColumnIndexOrThrow("review_count")),
                    stock = it.getInt(it.getColumnIndexOrThrow("stock"))
                ))
            }
        }
        return books
    }

    private fun cursorToBook(cursor: Cursor): BookModel {
        return BookModel(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
            author = cursor.getString(cursor.getColumnIndexOrThrow("author")),
            publisher = cursor.getString(cursor.getColumnIndexOrThrow("publisher")) ?: "",
            categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
            categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name")) ?: "",
            price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
            stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock")),
            description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: "",
            image = cursor.getString(cursor.getColumnIndexOrThrow("image")) ?: "",
            isbn = cursor.getString(cursor.getColumnIndexOrThrow("isbn")) ?: "",
            pages = cursor.getInt(cursor.getColumnIndexOrThrow("pages")),
            language = cursor.getString(cursor.getColumnIndexOrThrow("language")) ?: "Tiếng Việt",
            publicationYear = cursor.getInt(cursor.getColumnIndexOrThrow("publication_year")),
            rating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating")),
            reviewCount = cursor.getInt(cursor.getColumnIndexOrThrow("review_count")),
            soldCount = cursor.getInt(cursor.getColumnIndexOrThrow("sold_count")),
            status = BookModel.BookStatus.fromValue(cursor.getString(cursor.getColumnIndexOrThrow("status"))),
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
            updatedAt = cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
        )
    }
}
