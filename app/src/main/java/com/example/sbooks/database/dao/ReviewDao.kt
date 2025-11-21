package com.example.sbooks.database.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.sbooks.models.ReviewModel
import com.example.sbooks.utils.DateUtils

class ReviewDao(private val db: SQLiteDatabase) {

    companion object {
        const val TABLE_NAME = "reviews"
        const val COLUMN_ID = "id"
        const val COLUMN_BOOK_ID = "book_id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_USER_NAME = "user_name"
        const val COLUMN_USER_AVATAR = "user_avatar"
        const val COLUMN_RATING = "rating"
        const val COLUMN_COMMENT = "comment"
        const val COLUMN_IS_VERIFIED_PURCHASE = "is_verified_purchase"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"

        const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_BOOK_ID INTEGER NOT NULL,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_USER_NAME TEXT NOT NULL,
                $COLUMN_USER_AVATAR TEXT,
                $COLUMN_RATING REAL NOT NULL,
                $COLUMN_COMMENT TEXT,
                $COLUMN_IS_VERIFIED_PURCHASE INTEGER DEFAULT 0,
                $COLUMN_CREATED_AT TEXT NOT NULL,
                $COLUMN_UPDATED_AT TEXT NOT NULL,
                FOREIGN KEY($COLUMN_BOOK_ID) REFERENCES books(id) ON DELETE CASCADE,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES users(id) ON DELETE CASCADE
            )
        """
    }

    /**
     * Get all reviews for a specific book
     */
    fun getReviewsByBookId(bookId: Int): List<ReviewModel> {
        val reviews = mutableListOf<ReviewModel>()
        val cursor: Cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_BOOK_ID = ?",
            arrayOf(bookId.toString()),
            null,
            null,
            "$COLUMN_CREATED_AT DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                reviews.add(cursorToReview(it))
            }
        }

        return reviews
    }

    /**
     * Get review by ID
     */
    fun getReviewById(id: Int): ReviewModel? {
        val cursor: Cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return cursorToReview(it)
            }
        }

        return null
    }

    /**
     * Insert new review
     */
    fun insertReview(review: ReviewModel): Long {
        val values = ContentValues().apply {
            put(COLUMN_BOOK_ID, review.bookId)
            put(COLUMN_USER_ID, review.userId)
            put(COLUMN_USER_NAME, review.userName)
            put(COLUMN_USER_AVATAR, review.userAvatar)
            put(COLUMN_RATING, review.rating)
            put(COLUMN_COMMENT, review.comment)
            put(COLUMN_IS_VERIFIED_PURCHASE, if (review.isVerifiedPurchase) 1 else 0)
            put(COLUMN_CREATED_AT, review.createdAt.ifEmpty { DateUtils.getCurrentDate() })
            put(COLUMN_UPDATED_AT, review.updatedAt.ifEmpty { DateUtils.getCurrentDate() })
        }

        return db.insert(TABLE_NAME, null, values)
    }

    /**
     * Update existing review
     */
    fun updateReview(review: ReviewModel): Int {
        val values = ContentValues().apply {
            put(COLUMN_RATING, review.rating)
            put(COLUMN_COMMENT, review.comment)
            put(COLUMN_UPDATED_AT, DateUtils.getCurrentDate())
        }

        return db.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ?",
            arrayOf(review.id.toString())
        )
    }

    /**
     * Delete review by ID
     */
    fun deleteReview(id: Int): Int {
        return db.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    /**
     * Get average rating for a book
     */
    fun getAverageRating(bookId: Int): Float {
        val cursor = db.rawQuery(
            "SELECT AVG($COLUMN_RATING) as avg_rating FROM $TABLE_NAME WHERE $COLUMN_BOOK_ID = ?",
            arrayOf(bookId.toString())
        )

        cursor.use {
            if (it.moveToFirst()) {
                return it.getFloat(it.getColumnIndexOrThrow("avg_rating"))
            }
        }

        return 0f
    }

    /**
     * Get review count for a book
     */
    fun getReviewCount(bookId: Int): Int {
        val cursor = db.rawQuery(
            "SELECT COUNT(*) as count FROM $TABLE_NAME WHERE $COLUMN_BOOK_ID = ?",
            arrayOf(bookId.toString())
        )

        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(it.getColumnIndexOrThrow("count"))
            }
        }

        return 0
    }

    /**
     * Get rating breakdown (count for each star rating)
     */
    fun getRatingBreakdown(bookId: Int): Map<Int, Int> {
        val breakdown = mutableMapOf<Int, Int>()

        for (star in 1..5) {
            val cursor = db.rawQuery(
                """SELECT COUNT(*) as count FROM $TABLE_NAME 
                   WHERE $COLUMN_BOOK_ID = ? AND CAST($COLUMN_RATING AS INTEGER) = ?""",
                arrayOf(bookId.toString(), star.toString())
            )

            cursor.use {
                if (it.moveToFirst()) {
                    breakdown[star] = it.getInt(it.getColumnIndexOrThrow("count"))
                } else {
                    breakdown[star] = 0
                }
            }
        }

        return breakdown
    }

    /**
     * Check if user has reviewed this book
     */
    fun hasUserReviewedBook(userId: Int, bookId: Int): Boolean {
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID),
            "$COLUMN_USER_ID = ? AND $COLUMN_BOOK_ID = ?",
            arrayOf(userId.toString(), bookId.toString()),
            null,
            null,
            null
        )

        cursor.use {
            return it.count > 0
        }
    }

    /**
     * Get reviews with comments only
     */
    fun getReviewsWithComments(bookId: Int): List<ReviewModel> {
        val reviews = mutableListOf<ReviewModel>()
        val cursor: Cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_BOOK_ID = ? AND $COLUMN_COMMENT != ''",
            arrayOf(bookId.toString()),
            null,
            null,
            "$COLUMN_CREATED_AT DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                reviews.add(cursorToReview(it))
            }
        }

        return reviews
    }

    /**
     * Get reviews by rating
     */
    fun getReviewsByRating(bookId: Int, rating: Int): List<ReviewModel> {
        val reviews = mutableListOf<ReviewModel>()
        val cursor: Cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_BOOK_ID = ? AND CAST($COLUMN_RATING AS INTEGER) = ?",
            arrayOf(bookId.toString(), rating.toString()),
            null,
            null,
            "$COLUMN_CREATED_AT DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                reviews.add(cursorToReview(it))
            }
        }

        return reviews
    }

    /**
     * Convert cursor to ReviewModel
     */
    private fun cursorToReview(cursor: Cursor): ReviewModel {
        return ReviewModel(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            bookId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOK_ID)),
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
            userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
            userAvatar = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_AVATAR)) ?: "",
            rating = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_RATING)),
            comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT)) ?: "",
            isVerifiedPurchase = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_VERIFIED_PURCHASE)) == 1,
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
            updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT))
        )
    }
}