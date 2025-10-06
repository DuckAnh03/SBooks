package com.example.sbooks.database
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "starbooks.db"
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

        // Insert sample data
        insertSampleData(db)
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

    private fun insertSampleData(db: SQLiteDatabase) {
        // Kiểm tra xem đã có dữ liệu chưa, nếu có thì không insert nữa
        if (hasExistingData(db)) {
            return
        }

        // 1. Insert Users (Admin, Staff, and Customers)
        insertUsers(db)

        // 2. Insert Categories
        insertCategories(db)

        // 3. Insert Books
        insertBooks(db)

        // 4. Insert Orders
        insertOrders(db)

        // 5. Insert Order Items
        insertOrderItems(db)

        // 6. Insert Reviews
        insertReviews(db)

        // 7. Insert Cart Items
        insertCartItems(db)

        // 8. Insert Notifications
        insertNotifications(db)

        // 9. Update book statistics (rating, review_count, sold_count)
        updateBookStatistics(db)
    }

    /**
     * Kiểm tra xem database đã có dữ liệu cơ bản chưa
     */
    private fun hasExistingData(db: SQLiteDatabase): Boolean {
        return try {
            val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_USERS WHERE role = 'admin'", null)
            cursor.moveToFirst()
            val adminCount = cursor.getInt(0)
            cursor.close()

            val cursor2 = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CATEGORIES", null)
            cursor2.moveToFirst()
            val categoryCount = cursor2.getInt(0)
            cursor2.close()

            // Nếu đã có admin user và categories thì coi như đã có dữ liệu
            adminCount > 0 && categoryCount > 0
        } catch (e: Exception) {
            false
        }
    }

    private fun insertUsers(db: SQLiteDatabase) {
        // Admin and Staff users - sử dụng INSERT OR IGNORE để tránh lỗi trùng lặp
        db.execSQL("""
            INSERT OR IGNORE INTO $TABLE_USERS (username, email, full_name, password, role, status, phone, address) 
            VALUES ('admin', 'admin@starbooks.com', 'Administrator', 'admin123', 'admin', 'active', '0901234567', 'Hà Nội')
        """)

        db.execSQL("""
            INSERT OR IGNORE INTO $TABLE_USERS (username, email, full_name, password, role, status, phone, address) 
            VALUES ('staff', 'staff@starbooks.com', 'Staff User', 'staff123', 'staff', 'active', '0907654321', 'TP HCM')
        """)

        // Customer users
        data class CustomerData(val username: String, val email: String, val fullName: String)

        val customers = listOf(
            CustomerData("nguyentragiang", "nguyentragiang@email.com", "Nguyễn Trà Giang"),
            CustomerData("nltv", "nguyenletuanvub@email.com", "Nguyễn Lê Tuấn Vũ"),
            CustomerData("sigmaduck", "nguyenducanh@email.com", "Nguyễn Đức Anh"),
            CustomerData("nhpdung", "nghanhphuongdung@email.com", "Nguyễn Hạnh Phương Dung"),
            CustomerData("phuthodien", "phienthodu@email.com", "Phú Thợ Điện"),
            CustomerData("dnduc", "duckz2434@email.com", "Đặng Ngọc Đức"),
            CustomerData("thaianhtu", "thuanhtai@email.com", "Thú Anh Tái"),
            CustomerData("leduongthai", "laiduongthe@email.com", "Lai Dương Thế")
        )

        customers.forEachIndexed { index, customer ->
            db.execSQL("""
                INSERT INTO $TABLE_USERS (username, email, full_name, password, role, status, phone, address) 
                VALUES ('${customer.username}', '${customer.email}', '${customer.fullName}', 'password123', 'customer', 'active', 
                        '090${1000000 + index}', 'Địa chỉ ${index + 1}, Quận ${(index % 12) + 1}, TP HCM')
            """)
        }
    }

    private fun insertCategories(db: SQLiteDatabase) {
        data class CategoryData(val name: String, val description: String)

        val categories = listOf(
            CategoryData("Văn học", "Các tác phẩm văn học trong và ngoài nước"),
            CategoryData("Khoa học", "Sách về khoa học và công nghệ"),
            CategoryData("Lịch sử", "Sách về lịch sử và nhân vật lịch sử"),
            CategoryData("Kinh tế", "Sách về kinh tế và kinh doanh"),
            CategoryData("Giáo dục", "Sách giáo khoa và tài liệu học tập"),
            CategoryData("Tâm lý", "Sách về tâm lý học và phát triển bản thân"),
            CategoryData("Y học", "Sách về y học và sức khỏe"),
            CategoryData("Nghệ thuật", "Sách về nghệ thuật và văn hóa")
        )

        categories.forEachIndexed { index, category ->
            db.execSQL("""
                INSERT OR IGNORE INTO $TABLE_CATEGORIES (name, description, sort_order, status) 
                VALUES ('${category.name}', '${category.description}', $index, 'active')
            """)
        }
    }

    private fun insertBooks(db: SQLiteDatabase) {
        data class BookData(
            val title: String,
            val author: String,
            val publisher: String,
            val price: Double,
            val stock: Int,
            val isbn: String,
            val pages: Int,
            val year: Int
        )

        val booksByCategory = mapOf(
            1 to listOf( // Văn học
                BookData("Số đỏ", "Vũ Trọng Phụng", "NXB Văn học", 120000.0, 25, "978604123456789", 320, 1935),
                BookData("Chí Phèo", "Nam Cao", "NXB Văn học", 85000.0, 30, "978604123456790", 180, 1941),
                BookData("Lão Hạc", "Nam Cao", "NXB Văn học", 75000.0, 20, "978604123456791", 150, 1943),
                BookData("Tắt đèn", "Ngô Tất Tố", "NXB Văn học", 95000.0, 15, "978604123456792", 200, 1937),
                BookData("Truyện Kiều", "Nguyễn Du", "NXB Văn học", 150000.0, 40, "978604123456793", 400, 1820)
            ),
            2 to listOf( // Khoa học
                BookData("Vũ trụ trong vỏ hạt dẻ", "Stephen Hawking", "NXB Khoa học", 280000.0, 35, "978604123456794", 250, 2001),
                BookData("Lược sử thời gian", "Stephen Hawking", "NXB Khoa học", 320000.0, 20, "978604123456795", 300, 1988),
                BookData("Sapiens", "Yuval Noah Harari", "NXB Tri thức", 350000.0, 45, "978604123456796", 450, 2011),
                BookData("Homo Deus", "Yuval Noah Harari", "NXB Tri thức", 380000.0, 30, "978604123456797", 400, 2015)
            ),
            3 to listOf( // Lịch sử
                BookData("Đại Việt sử ký toàn thư", "Ngô Sĩ Liên", "NXB Sử học", 450000.0, 15, "978604123456798", 800, 1697),
                BookData("Lịch sử Việt Nam", "Trần Trọng Kim", "NXB Sử học", 180000.0, 25, "978604123456799", 350, 1920),
                BookData("Churchill", "Andrew Roberts", "NXB Thế giới", 420000.0, 20, "978604123456800", 1200, 2018)
            ),
            4 to listOf( // Kinh tế
                BookData("Nghĩ giàu làm giàu", "Napoleon Hill", "NXB Lao động", 250000.0, 50, "978604123456801", 350, 1937),
                BookData("Đắc nhân tâm", "Dale Carnegie", "NXB Lao động", 180000.0, 60, "978604123456802", 320, 1936),
                BookData("Rich Dad Poor Dad", "Robert Kiyosaki", "NXB Trẻ", 220000.0, 40, "978604123456803", 280, 1997),
                BookData("Kinh tế học vĩ mô", "Gregory Mankiw", "NXB Kinh tế", 380000.0, 25, "978604123456804", 600, 2020)
            ),
            5 to listOf( // Giáo dục
                BookData("Toán cao cấp A1", "Nguyễn Đình Trí", "NXB ĐHQGHN", 120000.0, 100, "978604123456805", 250, 2019),
                BookData("Vật lý đại cương", "Halliday Resnick", "NXB Giáo dục", 350000.0, 80, "978604123456806", 800, 2018),
                BookData("Hóa học đại cương", "Petrucci", "NXB Giáo dục", 320000.0, 70, "978604123456807", 750, 2017)
            ),
            6 to listOf( // Tâm lý
                BookData("Tâm lý học đại cương", "David G. Myers", "NXB Đại học QG", 420000.0, 35, "978604123456808", 650, 2019),
                BookData("Thuật thao túng", "Kevin Dutton", "NXB Trẻ", 180000.0, 45, "978604123456809", 300, 2014),
                BookData("Dám bị ghét", "Fumitake Koga", "NXB Trẻ", 160000.0, 55, "978604123456810", 250, 2013)
            ),
            7 to listOf( // Y học
                BookData("Giải phẫu học", "Frank H. Netter", "NXB Y học", 850000.0, 20, "978604123456811", 900, 2018),
                BookData("Sinh lý học", "Guyton Hall", "NXB Y học", 780000.0, 25, "978604123456812", 1100, 2016)
            ),
            8 to listOf( // Nghệ thuật
                BookData("Lịch sử nghệ thuật", "E.H. Gombrich", "NXB Mỹ thuật", 450000.0, 30, "978604123456813", 500, 1950),
                BookData("Âm nhạc và cuộc sống", "Aaron Copland", "NXB Âm nhạc", 280000.0, 25, "978604123456814", 320, 1957)
            )
        )

        booksByCategory.forEach { (categoryId, books) ->
            books.forEach { book ->
                val description = "Mô tả chi tiết về cuốn sách ${book.title} của tác giả ${book.author}. Đây là một tác phẩm xuất sắc trong lĩnh vực của mình."

                db.execSQL("""
                    INSERT INTO $TABLE_BOOKS (title, author, publisher, category_id, price, stock, 
                                            description, isbn, pages, publication_year, status) 
                    VALUES ('${book.title}', '${book.author}', '${book.publisher}', $categoryId, ${book.price}, ${book.stock}, 
                            '$description', '${book.isbn}', ${book.pages}, ${book.year}, 'active')
                """)
            }
        }
    }

    private fun insertOrders(db: SQLiteDatabase) {
        val orderStatuses = listOf("pending", "confirmed", "shipping", "delivered", "cancelled")
        val paymentMethods = listOf("cod", "bank_transfer", "momo", "vnpay")
        val paymentStatuses = listOf("unpaid", "paid", "refunded")

        // Tạo 20 đơn hàng mẫu
        for (i in 1..20) {
            val customerId = (3..10).random() // Customer IDs từ 3-10
            val orderCode = "ORD${System.currentTimeMillis() + i}"
            val totalAmount = (100000..1000000).random().toDouble()
            val shippingFee = if (totalAmount > 500000) 0.0 else 30000.0
            val discountAmount = if (totalAmount > 300000) totalAmount * 0.1 else 0.0
            val finalAmount = totalAmount + shippingFee - discountAmount
            val status = orderStatuses.random()
            val paymentMethod = paymentMethods.random()
            val paymentStatus = if (status == "delivered") "paid" else paymentStatuses.random()
            val staffId = if ((1..3).random() == 1) 2 else null // 1/3 chance có staff xử lý

            db.execSQL("""
                INSERT INTO $TABLE_ORDERS (order_code, customer_id, customer_name, customer_email, 
                                         customer_phone, customer_address, total_amount, shipping_fee, 
                                         discount_amount, final_amount, status, payment_method, 
                                         payment_status, staff_id, staff_name, notes,
                                         order_date) 
                VALUES ('$orderCode', $customerId, 'Khách hàng $i', 'customer$i@email.com', 
                        '090${1000000 + i}', 'Địa chỉ giao hàng $i', $totalAmount, $shippingFee, 
                        $discountAmount, $finalAmount, '$status', '$paymentMethod', '$paymentStatus', 
                        ${staffId ?: "NULL"}, ${if (staffId != null) "'Staff User'" else "NULL"}, 
                        'Ghi chú đơn hàng $i',
                        datetime('now', '-${(0..30).random()} days'))
            """)
        }
    }

    private fun insertOrderItems(db: SQLiteDatabase) {
        // Tạo order items cho mỗi đơn hàng
        for (orderId in 1..20) {
            val itemCount = (1..4).random() // Mỗi đơn hàng có 1-4 sản phẩm
            val usedBookIds = mutableSetOf<Int>()

            repeat(itemCount) {
                var bookId = (1..25).random()
                while (usedBookIds.contains(bookId)) {
                    bookId = (1..25).random()
                }
                usedBookIds.add(bookId)

                val quantity = (1..3).random()
                // Lấy thông tin sách (giả sử giá trung bình)
                val price = when ((bookId - 1) / 5) { // Theo category
                    0 -> (75000..150000).random().toDouble() // Văn học
                    1 -> (280000..380000).random().toDouble() // Khoa học
                    2 -> (180000..450000).random().toDouble() // Lịch sử
                    3 -> (180000..380000).random().toDouble() // Kinh tế
                    4 -> (120000..350000).random().toDouble() // Giáo dục
                    5 -> (160000..420000).random().toDouble() // Tâm lý
                    6 -> (780000..850000).random().toDouble() // Y học
                    else -> (280000..450000).random().toDouble() // Nghệ thuật
                }
                val totalPrice = price * quantity

                db.execSQL("""
                    INSERT INTO $TABLE_ORDER_ITEMS (order_id, book_id, book_title, book_author, 
                                                   price, quantity, total_price) 
                    VALUES ($orderId, $bookId, 'Sách $bookId', 'Tác giả $bookId', 
                            $price, $quantity, $totalPrice)
                """)
            }
        }
    }

    private fun insertReviews(db: SQLiteDatabase) {
        val reviewComments = listOf(
            "Cuốn sách rất hay và bổ ích!",
            "Nội dung phong phú, dễ hiểu",
            "Tác phẩm kinh điển, đáng đọc",
            "Chất lượng in ấn tốt, giá hợp lý",
            "Giao hàng nhanh, đóng gói cẩn thận",
            "Sách có nhiều kiến thức mới mẻ",
            "Phù hợp cho người mới bắt đầu",
            "Nội dung sâu sắc, có tính thực tiễn cao",
            "Đọc rất cuốn hút, không thể bỏ xuống",
            "Giá cả phải chăng, chất lượng tốt"
        )

        // Tạo reviews cho các sách
        for (bookId in 1..25) {
            val reviewCount = (3..15).random() // Mỗi sách có 3-15 reviews
            repeat(reviewCount) {
                val userId = (3..10).random() // Customer users
                val rating = listOf(3.5, 4.0, 4.5, 5.0, 3.0, 4.5, 5.0, 4.0).random()
                val comment = reviewComments.random()
                val isVerified = (1..3).random() == 1 // 1/3 chance verified purchase

                db.execSQL("""
                    INSERT INTO $TABLE_REVIEWS (book_id, user_id, user_name, rating, comment, 
                                               is_verified_purchase, created_at) 
                    VALUES ($bookId, $userId, 'User $userId', $rating, '$comment', 
                            ${if (isVerified) 1 else 0},
                            datetime('now', '-${(0..60).random()} days'))
                """)
            }
        }
    }

    private fun insertCartItems(db: SQLiteDatabase) {
        // Thêm một số sản phẩm vào giỏ hàng của customers
        val customers = listOf(3, 4, 5, 6) // Một số customer có items trong cart

        customers.forEach { customerId ->
            val cartItemCount = (2..5).random()
            val usedBookIds = mutableSetOf<Int>()

            repeat(cartItemCount) {
                var bookId = (1..25).random()
                while (usedBookIds.contains(bookId)) {
                    bookId = (1..25).random()
                }
                usedBookIds.add(bookId)

                val quantity = (1..3).random()
                val isSelected = (1..4).random() != 1 // 3/4 chance selected

                db.execSQL("""
                    INSERT INTO $TABLE_CART_ITEMS (user_id, book_id, quantity, is_selected) 
                    VALUES ($customerId, $bookId, $quantity, ${if (isSelected) 1 else 0})
                """)
            }
        }
    }

    private fun insertNotifications(db: SQLiteDatabase) {
        val notificationTypes = listOf("info", "success", "warning", "error")
        val notifications = listOf(
            "Đơn hàng mới" to "Bạn có đơn hàng mới cần xử lý",
            "Thanh toán thành công" to "Đơn hàng của bạn đã được thanh toán thành công",
            "Giao hàng thành công" to "Đơn hàng đã được giao thành công",
            "Sách mới" to "Có sách mới trong danh mục yêu thích của bạn",
            "Khuyến mãi" to "Chương trình giảm giá 20% cho tất cả sách văn học",
            "Cập nhật hệ thống" to "Hệ thống sẽ bảo trì từ 2h-4h sáng ngày mai",
            "Đánh giá mới" to "Sách của bạn có đánh giá mới",
            "Tồn kho thấp" to "Một số sách sắp hết hàng, cần nhập thêm"
        )

        // Tạo notifications cho users
        for (userId in 1..10) {
            val notifCount = (3..8).random()
            repeat(notifCount) {
                val (title, message) = notifications.random()
                val type = notificationTypes.random()
                val isRead = (1..3).random() == 1 // 1/3 chance đã đọc

                db.execSQL("""
                    INSERT INTO $TABLE_NOTIFICATIONS (user_id, title, message, type, is_read, created_at) 
                    VALUES ($userId, '$title', '$message', '$type', ${if (isRead) 1 else 0},
                            datetime('now', '-${(0..10).random()} days'))
                """)
            }
        }
    }

    private fun updateBookStatistics(db: SQLiteDatabase) {
        // Cập nhật rating và review_count cho các sách
        db.execSQL("""
            UPDATE $TABLE_BOOKS 
            SET rating = (
                SELECT AVG(rating) 
                FROM $TABLE_REVIEWS 
                WHERE book_id = $TABLE_BOOKS.id
            ),
            review_count = (
                SELECT COUNT(*) 
                FROM $TABLE_REVIEWS 
                WHERE book_id = $TABLE_BOOKS.id
            )
        """)

        // Cập nhật sold_count từ order_items (chỉ tính đơn hàng delivered)
        db.execSQL("""
            UPDATE $TABLE_BOOKS 
            SET sold_count = (
                SELECT COALESCE(SUM(oi.quantity), 0)
                FROM $TABLE_ORDER_ITEMS oi
                JOIN $TABLE_ORDERS o ON oi.order_id = o.id
                WHERE oi.book_id = $TABLE_BOOKS.id 
                AND o.status = 'delivered'
            )
        """)
    }

    /**
     * Phương thức để insert thêm dữ liệu mẫu khi cần (gọi từ code)
     * An toàn hơn - kiểm tra trùng lặp trước khi insert
     */
    fun insertAdditionalSampleData() {
        val db = writableDatabase
        try {
            db.beginTransaction()

            // Chỉ insert nếu chưa có dữ liệu hoặc force insert
            insertSampleDataSafe(db)

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    /**
     * Force insert dữ liệu mẫu (ghi đè nếu trùng)
     * Chỉ sử dụng khi muốn reset dữ liệu hoàn toàn
     */
    fun forceInsertSampleData() {
        val db = writableDatabase
        try {
            db.beginTransaction()
            insertSampleData(db)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    /**
     * Insert dữ liệu mẫu an toàn - kiểm tra từng loại dữ liệu
     */
    private fun insertSampleDataSafe(db: SQLiteDatabase) {
        // 1. Kiểm tra và insert Users
        val userCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_USERS", null)
        userCursor.moveToFirst()
        val userCount = userCursor.getInt(0)
        userCursor.close()

        if (userCount < 3) { // Ít nhất cần admin, staff, 1 customer
            insertUsers(db)
        }

        // 2. Kiểm tra và insert Categories
        val catCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CATEGORIES", null)
        catCursor.moveToFirst()
        val catCount = catCursor.getInt(0)
        catCursor.close()

        if (catCount < 5) { // Ít nhất cần 5 category
            insertCategories(db)
        }

        // 3. Kiểm tra và insert Books
        val bookCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_BOOKS", null)
        bookCursor.moveToFirst()
        val bookCount = bookCursor.getInt(0)
        bookCursor.close()

        if (bookCount < 10) { // Ít nhất cần 10 sách
            insertBooks(db)
        }

        // 4. Kiểm tra và insert Orders (chỉ insert nếu chưa có)
        val orderCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ORDERS", null)
        orderCursor.moveToFirst()
        val orderCount = orderCursor.getInt(0)
        orderCursor.close()

        if (orderCount < 5) { // Ít nhất cần 5 orders
            insertOrders(db)
            insertOrderItems(db)
        }

        // 5. Kiểm tra và insert Reviews
        val reviewCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_REVIEWS", null)
        reviewCursor.moveToFirst()
        val reviewCount = reviewCursor.getInt(0)
        reviewCursor.close()

        if (reviewCount < 20) { // Ít nhất cần 20 reviews
            insertReviews(db)
        }

        // 6. Insert Cart Items và Notifications (có thể insert thêm)
        insertCartItems(db)
        insertNotifications(db)

        // 7. Cập nhật thống kê
        updateBookStatistics(db)
    }

    /**
     * Xóa tất cả dữ liệu mẫu (giữ lại cấu trúc bảng)
     */
    fun clearAllData() {
        val db = writableDatabase
        try {
            db.beginTransaction()
            db.execSQL("DELETE FROM $TABLE_NOTIFICATIONS")
            db.execSQL("DELETE FROM $TABLE_CART_ITEMS")
            db.execSQL("DELETE FROM $TABLE_REVIEWS")
            db.execSQL("DELETE FROM $TABLE_ORDER_ITEMS")
            db.execSQL("DELETE FROM $TABLE_ORDERS")
            db.execSQL("DELETE FROM $TABLE_BOOKS")
            db.execSQL("DELETE FROM $TABLE_CATEGORIES")
            db.execSQL("DELETE FROM $TABLE_USERS")

            // Reset auto increment
            db.execSQL("DELETE FROM sqlite_sequence WHERE name IN ('$TABLE_USERS', '$TABLE_CATEGORIES', '$TABLE_BOOKS', '$TABLE_ORDERS', '$TABLE_ORDER_ITEMS', '$TABLE_REVIEWS', '$TABLE_CART_ITEMS', '$TABLE_NOTIFICATIONS')")

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            db.close()
        }
    }
}