package com.example.sbooks.database
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "starbooks.db"
        private const val DATABASE_VERSION = 1

        // Table names
        const val TABLE_USERS = "users"
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_BOOKS = "books"
        const val TABLE_ORDERS = "orders"
        const val TABLE_ORDER_ITEMS = "order_items"
        const val TABLE_REVIEWS = "reviews"
        const val TABLE_CART_ITEMS = "cart_items"
        const val TABLE_NOTIFICATIONS = "notifications"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create Users table
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                email TEXT UNIQUE NOT NULL,
                phone TEXT,
                full_name TEXT,
                address TEXT,
                password TEXT NOT NULL,
                role TEXT DEFAULT 'customer',
                status TEXT DEFAULT 'active',
                avatar TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """)

        // Create Categories table
        db.execSQL("""
            CREATE TABLE $TABLE_CATEGORIES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL,
                description TEXT,
                icon TEXT,
                status TEXT DEFAULT 'active',
                sort_order INTEGER DEFAULT 0,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """)

        // Create Books table
        db.execSQL("""
            CREATE TABLE $TABLE_BOOKS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                author TEXT NOT NULL,
                publisher TEXT,
                category_id INTEGER,
                price REAL NOT NULL,
                stock INTEGER DEFAULT 0,
                description TEXT,
                image TEXT,
                isbn TEXT,
                pages INTEGER,
                language TEXT DEFAULT 'Tiếng Việt',
                publication_year INTEGER,
                rating REAL DEFAULT 0.0,
                review_count INTEGER DEFAULT 0,
                sold_count INTEGER DEFAULT 0,
                status TEXT DEFAULT 'active',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (category_id) REFERENCES $TABLE_CATEGORIES(id)
            )
        """)

        // Create Orders table
        db.execSQL("""
            CREATE TABLE $TABLE_ORDERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_code TEXT UNIQUE NOT NULL,
                customer_id INTEGER NOT NULL,
                customer_name TEXT NOT NULL,
                customer_email TEXT,
                customer_phone TEXT,
                customer_address TEXT,
                total_amount REAL NOT NULL,
                shipping_fee REAL DEFAULT 0.0,
                discount_amount REAL DEFAULT 0.0,
                final_amount REAL NOT NULL,
                status TEXT DEFAULT 'pending',
                payment_method TEXT DEFAULT 'cod',
                payment_status TEXT DEFAULT 'unpaid',
                order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                delivery_date DATETIME,
                notes TEXT,
                staff_id INTEGER,
                staff_name TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (customer_id) REFERENCES $TABLE_USERS(id),
                FOREIGN KEY (staff_id) REFERENCES $TABLE_USERS(id)
            )
        """)

        // Create Order Items table
        db.execSQL("""
            CREATE TABLE $TABLE_ORDER_ITEMS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                book_id INTEGER NOT NULL,
                book_title TEXT NOT NULL,
                book_author TEXT,
                book_image TEXT,
                price REAL NOT NULL,
                quantity INTEGER NOT NULL,
                total_price REAL NOT NULL,
                FOREIGN KEY (order_id) REFERENCES $TABLE_ORDERS(id) ON DELETE CASCADE,
                FOREIGN KEY (book_id) REFERENCES $TABLE_BOOKS(id)
            )
        """)

        // Create Reviews table
        db.execSQL("""
            CREATE TABLE $TABLE_REVIEWS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id INTEGER NOT NULL,
                user_id INTEGER NOT NULL,
                user_name TEXT NOT NULL,
                user_avatar TEXT,
                rating REAL NOT NULL CHECK (rating >= 1.0 AND rating <= 5.0),
                comment TEXT,
                is_verified_purchase INTEGER DEFAULT 0,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (book_id) REFERENCES $TABLE_BOOKS(id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES $TABLE_USERS(id)
            )
        """)

        // Create Cart Items table
        db.execSQL("""
            CREATE TABLE $TABLE_CART_ITEMS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                book_id INTEGER NOT NULL,
                quantity INTEGER DEFAULT 1,
                is_selected INTEGER DEFAULT 1,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES $TABLE_USERS(id) ON DELETE CASCADE,
                FOREIGN KEY (book_id) REFERENCES $TABLE_BOOKS(id) ON DELETE CASCADE,
                UNIQUE(user_id, book_id)
            )
        """)

        // Create Notifications table
        db.execSQL("""
            CREATE TABLE $TABLE_NOTIFICATIONS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                type TEXT DEFAULT 'info',
                is_read INTEGER DEFAULT 0,
                related_id INTEGER,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES $TABLE_USERS(id) ON DELETE CASCADE
            )
        """)

        // Insert default admin user
        insertDefaultData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop all tables
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CART_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REVIEWS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDER_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")

        // Recreate tables
        onCreate(db)
    }

    private fun insertDefaultData(db: SQLiteDatabase) {
        // Insert default admin user
        db.execSQL("""
            INSERT INTO $TABLE_USERS (username, email, full_name, password, role, status) 
            VALUES ('admin', 'admin@starbooks.com', 'Administrator', 'admin123', 'admin', 'active')
        """)

        // Insert default staff user
        db.execSQL("""
            INSERT INTO $TABLE_USERS (username, email, full_name, password, role, status) 
            VALUES ('staff', 'staff@starbooks.com', 'Staff User', 'staff123', 'staff', 'active')
        """)

        // Insert default categories
        val categories = listOf(
            "Văn học" to "Các tác phẩm văn học trong và ngoài nước",
            "Khoa học" to "Sách về khoa học và công nghệ",
            "Lịch sử" to "Sách về lịch sử và nhân vật lịch sử",
            "Kinh tế" to "Sách về kinh tế và kinh doanh",
            "Giáo dục" to "Sách giáo khoa và tài liệu học tập",
            "Tâm lý" to "Sách về tâm lý học và phát triển bản thân",
            "Y học" to "Sách về y học và sức khỏe",
            "Nghệ thuật" to "Sách về nghệ thuật và văn hóa"
        )

        categories.forEachIndexed { index, (name, description) ->
            db.execSQL("""
                INSERT INTO $TABLE_CATEGORIES (name, description, sort_order, status) 
                VALUES ('$name', '$description', $index, 'active')
            """)
        }
    }
}