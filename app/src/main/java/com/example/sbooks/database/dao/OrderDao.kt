package com.example.sbooks.database.dao
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.models.OrderModel
import com.example.sbooks.models.OrderItemModel

class OrderDao(private val db: SQLiteDatabase) {

    fun insertOrder(order: OrderModel): Long {
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("order_code", order.orderCode)
                put("customer_id", order.customerId)
                put("customer_name", order.customerName)
                put("customer_email", order.customerEmail)
                put("customer_phone", order.customerPhone)
                put("customer_address", order.customerAddress)
                put("total_amount", order.totalAmount)
                put("shipping_fee", order.shippingFee)
                put("discount_amount", order.discountAmount)
                put("final_amount", order.finalAmount)
                put("status", order.status.value)
                put("payment_method", order.paymentMethod.value)
                put("payment_status", order.paymentStatus.value)
                put("notes", order.notes)
                put("staff_id", order.staffId)
                put("staff_name", order.staffName)
            }

            val orderId = db.insert(DatabaseHelper.TABLE_ORDERS, null, values)

            // Insert order items
            order.items.forEach { item ->
                val itemValues = ContentValues().apply {
                    put("order_id", orderId)
                    put("book_id", item.bookId)
                    put("book_title", item.bookTitle)
                    put("book_author", item.bookAuthor)
                    put("book_image", item.bookImage)
                    put("price", item.price)
                    put("quantity", item.quantity)
                    put("total_price", item.totalPrice)
                }
                db.insert(DatabaseHelper.TABLE_ORDER_ITEMS, null, itemValues)
            }

            db.setTransactionSuccessful()
            return orderId
        } finally {
            db.endTransaction()
        }
    }

    fun updateOrderStatus(orderId: Int, status: OrderModel.OrderStatus, staffId: Int? = null, staffName: String = ""): Int {
        val values = ContentValues().apply {
            put("status", status.value)
            put("updated_at", "datetime('now')")
            if (staffId != null) {
                put("staff_id", staffId)
                put("staff_name", staffName)
            }
            if (status == OrderModel.OrderStatus.DELIVERED) {
                put("delivery_date", "datetime('now')")
            }
        }
        return db.update(DatabaseHelper.TABLE_ORDERS, values, "id = ?", arrayOf(orderId.toString()))
    }

    fun updatePaymentStatus(orderId: Int, paymentStatus: OrderModel.PaymentStatus): Int {
        val values = ContentValues().apply {
            put("payment_status", paymentStatus.value)
            put("updated_at", "datetime('now')")
        }
        return db.update(DatabaseHelper.TABLE_ORDERS, values, "id = ?", arrayOf(orderId.toString()))
    }

    fun deleteOrder(orderId: Int): Int {
        return db.delete(DatabaseHelper.TABLE_ORDERS, "id = ?", arrayOf(orderId.toString()))
    }

    fun getOrderById(orderId: Int): OrderModel? {
        val cursor = db.query(
            DatabaseHelper.TABLE_ORDERS,
            null,
            "id = ?",
            arrayOf(orderId.toString()),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                val order = cursorToOrder(it)
                order.copy(items = getOrderItems(orderId))
            } else null
        }
    }

    fun getOrderByCode(orderCode: String): OrderModel? {
        val cursor = db.query(
            DatabaseHelper.TABLE_ORDERS,
            null,
            "order_code = ?",
            arrayOf(orderCode),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                val order = cursorToOrder(it)
                order.copy(items = getOrderItems(order.id))
            } else null
        }
    }

    fun getAllOrders(): List<OrderModel> {
        val orders = mutableListOf<OrderModel>()
        val cursor = db.query(
            DatabaseHelper.TABLE_ORDERS,
            null,
            null, null, null, null,
            "created_at DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val order = cursorToOrder(it)
                orders.add(order.copy(items = getOrderItems(order.id)))
            }
        }
        return orders
    }

    fun getOrdersByCustomer(customerId: Int): List<OrderModel> {
        val orders = mutableListOf<OrderModel>()
        val cursor = db.query(
            DatabaseHelper.TABLE_ORDERS,
            null,
            "customer_id = ?",
            arrayOf(customerId.toString()),
            null, null,
            "created_at DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val order = cursorToOrder(it)
                orders.add(order.copy(items = getOrderItems(order.id)))
            }
        }
        return orders
    }

    fun getOrdersByStatus(status: OrderModel.OrderStatus): List<OrderModel> {
        val orders = mutableListOf<OrderModel>()
        val cursor = db.query(
            DatabaseHelper.TABLE_ORDERS,
            null,
            "status = ?",
            arrayOf(status.value),
            null, null,
            "created_at DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val order = cursorToOrder(it)
                orders.add(order.copy(items = getOrderItems(order.id)))
            }
        }
        return orders
    }

    fun getOrdersByStaff(staffId: Int): List<OrderModel> {
        val orders = mutableListOf<OrderModel>()
        val cursor = db.query(
            DatabaseHelper.TABLE_ORDERS,
            null,
            "staff_id = ?",
            arrayOf(staffId.toString()),
            null, null,
            "created_at DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val order = cursorToOrder(it)
                orders.add(order.copy(items = getOrderItems(order.id)))
            }
        }
        return orders
    }

    fun searchOrders(query: String, status: String? = null, dateFrom: String? = null, dateTo: String? = null): List<OrderModel> {
        val orders = mutableListOf<OrderModel>()
        val whereConditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        // Search query
        if (query.isNotEmpty()) {
            whereConditions.add("(order_code LIKE ? OR customer_name LIKE ? OR customer_phone LIKE ?)")
            val searchQuery = "%$query%"
            args.addAll(listOf(searchQuery, searchQuery, searchQuery))
        }

        // Status filter
        if (status != null && status != "all") {
            whereConditions.add("status = ?")
            args.add(status)
        }

        // Date range filter
        if (dateFrom != null) {
            whereConditions.add("DATE(created_at) >= ?")
            args.add(dateFrom)
        }
        if (dateTo != null) {
            whereConditions.add("DATE(created_at) <= ?")
            args.add(dateTo)
        }

        val whereClause = if (whereConditions.isNotEmpty()) {
            "WHERE ${whereConditions.joinToString(" AND ")}"
        } else ""

        val cursor = db.rawQuery("""
            SELECT * FROM ${DatabaseHelper.TABLE_ORDERS}
            $whereClause
            ORDER BY created_at DESC
        """, args.toTypedArray())

        cursor.use {
            while (it.moveToNext()) {
                val order = cursorToOrder(it)
                orders.add(order.copy(items = getOrderItems(order.id)))
            }
        }
        return orders
    }

    fun getOrderItems(orderId: Int): List<OrderItemModel> {
        val items = mutableListOf<OrderItemModel>()
        val cursor = db.query(
            DatabaseHelper.TABLE_ORDER_ITEMS,
            null,
            "order_id = ?",
            arrayOf(orderId.toString()),
            null, null, null
        )

        cursor.use {
            while (it.moveToNext()) {
                items.add(OrderItemModel(
                    id = it.getInt(it.getColumnIndexOrThrow("id")),
                    orderId = it.getInt(it.getColumnIndexOrThrow("order_id")),
                    bookId = it.getInt(it.getColumnIndexOrThrow("book_id")),
                    bookTitle = it.getString(it.getColumnIndexOrThrow("book_title")),
                    bookAuthor = it.getString(it.getColumnIndexOrThrow("book_author")) ?: "",
                    bookImage = it.getString(it.getColumnIndexOrThrow("book_image")) ?: "",
                    price = it.getDouble(it.getColumnIndexOrThrow("price")),
                    quantity = it.getInt(it.getColumnIndexOrThrow("quantity")),
                    totalPrice = it.getDouble(it.getColumnIndexOrThrow("total_price"))
                ))
            }
        }
        return items
    }

    fun generateOrderCode(): String {
        val cursor = db.rawQuery("""
            SELECT COUNT(*) as count FROM ${DatabaseHelper.TABLE_ORDERS} 
            WHERE DATE(created_at) = DATE('now')
        """, null)

        val todayCount = cursor.use {
            if (it.moveToFirst()) it.getInt(it.getColumnIndexOrThrow("count")) else 0
        }

        val dateFormat = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
        val today = dateFormat.format(java.util.Date())
        return "ORD$today${String.format("%03d", todayCount + 1)}"
    }

    private fun cursorToOrder(cursor: Cursor): OrderModel {
        return OrderModel(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            orderCode = cursor.getString(cursor.getColumnIndexOrThrow("order_code")),
            customerId = cursor.getInt(cursor.getColumnIndexOrThrow("customer_id")),
            customerName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name")),
            customerEmail = cursor.getString(cursor.getColumnIndexOrThrow("customer_email")) ?: "",
            customerPhone = cursor.getString(cursor.getColumnIndexOrThrow("customer_phone")) ?: "",
            customerAddress = cursor.getString(cursor.getColumnIndexOrThrow("customer_address")) ?: "",
            totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")),
            shippingFee = cursor.getDouble(cursor.getColumnIndexOrThrow("shipping_fee")),
            discountAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("discount_amount")),
            finalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("final_amount")),
            status = OrderModel.OrderStatus.fromValue(cursor.getString(cursor.getColumnIndexOrThrow("status"))),
            paymentMethod = OrderModel.PaymentMethod.fromValue(cursor.getString(cursor.getColumnIndexOrThrow("payment_method"))),
            paymentStatus = OrderModel.PaymentStatus.fromValue(cursor.getString(cursor.getColumnIndexOrThrow("payment_status"))),
            orderDate = cursor.getString(cursor.getColumnIndexOrThrow("order_date")),
            deliveryDate = cursor.getString(cursor.getColumnIndexOrThrow("delivery_date")) ?: "",
            notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")) ?: "",
            staffId = cursor.getInt(cursor.getColumnIndexOrThrow("staff_id")),
            staffName = cursor.getString(cursor.getColumnIndexOrThrow("staff_name")) ?: "",
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
            updatedAt = cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
        )
    }
}