package com.example.sbooks.activities.customer

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sbooks.R
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.BookDao
import com.example.sbooks.database.dao.OrderDao
import com.example.sbooks.database.dao.UserDao
import com.example.sbooks.models.OrderItemModel
import com.example.sbooks.models.OrderModel
import com.example.sbooks.utils.CartManager
import com.example.sbooks.utils.ImageUtils
import com.example.sbooks.utils.SharedPrefsHelper
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "PaymentActivity"
    }
    private lateinit var sharedPrefsHelper:SharedPrefsHelper
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var userDao: UserDao
    private lateinit var btnBack: ImageButton
    private lateinit var orderItemsContainer: LinearLayout
    private lateinit var edtName: TextInputEditText
    private lateinit var edtAddress: TextInputEditText
    private lateinit var edtPhone: TextInputEditText
    private lateinit var edtEmail: TextInputEditText
    private lateinit var txtSubtotal: TextView
    private lateinit var txtShippingFee: TextView
    private lateinit var txtTotal: TextView
    private lateinit var btnConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val rootView = findViewById<android.view.View>(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPrefsHelper = SharedPrefsHelper(this)
        //data
        dbHelper = DatabaseHelper(this)
        userDao = UserDao(dbHelper.writableDatabase)
        // Initialize views
        initViews()

        // Setup listeners
        setupListeners()

        // Load user info
        loadUserInfo()

        // Load cart items
        loadOrderItems()

        // Update total
        updateTotal()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        orderItemsContainer = findViewById(R.id.orderItemsContainer)
        edtName = findViewById(R.id.edtName)
        edtAddress = findViewById(R.id.edtAddress)
        edtPhone = findViewById(R.id.edtPhone)
        edtEmail = findViewById(R.id.edtEmail)
        txtSubtotal = findViewById(R.id.txtSubtotal)
        txtShippingFee = findViewById(R.id.txtShippingFee)
        txtTotal = findViewById(R.id.txtTotal)
        btnConfirm = findViewById(R.id.btnConfirm)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnConfirm.setOnClickListener {
            processCheckout()
        }
    }

    private fun loadUserInfo() {
        val user = userDao.getUserById(sharedPrefsHelper.getUserId())
        // Pre-fill with user info if available
        if(user != null){
            val fullName = user.fullName
            val phone = user.phone
            val email = user.email
            val address = user.address

            if (fullName.isNotEmpty()) {
                edtName.setText(fullName)
            }
            if (phone.isNotEmpty()) {
                edtPhone.setText(phone)
            }
            if (email.isNotEmpty()) {
                edtEmail.setText(email)
            }
            if (address.isNotEmpty()) {
                edtAddress.setText(address)
            }
        }
    }

    private fun loadOrderItems() {
        val cartItems = CartManager.getCartItems()

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        orderItemsContainer.removeAllViews()

        cartItems.forEach { cartItem ->
            val itemView = createOrderItemView(cartItem)
            orderItemsContainer.addView(itemView)
        }
    }

    private fun createOrderItemView(cartItem: com.example.sbooks.utils.CartItem): CardView {
        val book = cartItem.book

        // Reuse item_cart.xml layout
        val cardView = layoutInflater.inflate(
            R.layout.item_cart,
            orderItemsContainer,
            false
        ) as CardView

        // Find views
        val ivBookCover = cardView.findViewById<ImageView>(R.id.iv_book_cover)
        val tvBookTitle = cardView.findViewById<TextView>(R.id.tv_book_title)
        val tvBookAuthor = cardView.findViewById<TextView>(R.id.tv_book_author)
        val tvBookPrice = cardView.findViewById<TextView>(R.id.tv_book_price)
        val txtQuantity = cardView.findViewById<TextView>(R.id.txt_quantity)

        // Hide control buttons (decrease, increase, delete)
        val btnDecrease = cardView.findViewById<ImageButton>(R.id.btn_decrease)
        val btnIncrease = cardView.findViewById<ImageButton>(R.id.btn_increase)
        val btnDelete = cardView.findViewById<ImageButton>(R.id.btn_delete)

        btnDecrease.visibility = View.GONE
        btnIncrease.visibility = View.GONE
        btnDelete.visibility = View.GONE

        // Bind data
        tvBookTitle.text = book.title
        tvBookAuthor.text = "Tác giả: ${book.author}"
        tvBookPrice.text = String.format("%,.0fđ", book.price)
        txtQuantity.text = "x${cartItem.quantity}"

        // Load image
        if (book.image.isNotEmpty()) {
            val bitmap = ImageUtils.loadImageFromInternalStorage(book.image)
            if (bitmap != null) {
                ivBookCover.setImageBitmap(bitmap)
            } else {
                ivBookCover.setImageResource(R.drawable.ic_book_24)
            }
        } else {
            ivBookCover.setImageResource(R.drawable.ic_book_24)
        }

        // Disable click on card
        cardView.isClickable = false

        return cardView
    }

    private fun updateTotal() {
        val subtotal = CartManager.getTotalPrice()
        val shippingFee = 0.0 // Miễn phí ship
        val total = subtotal + shippingFee

        txtSubtotal.text = String.format("%,.0fđ", subtotal)
        txtShippingFee.text = if (shippingFee > 0) {
            String.format("%,.0fđ", shippingFee)
        } else {
            "Miễn phí"
        }
        txtTotal.text = String.format("%,.0fđ", total)
    }

    private fun processCheckout() {
        val name = edtName.text.toString().trim()
        val address = edtAddress.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val email = edtEmail.text.toString().trim()

        // Validate inputs
        if (!validateInputs(name, address, phone, email)) {
            return
        }

        // Show confirmation dialog
        showConfirmationDialog(name, address, phone, email)
    }

    private fun validateInputs(name: String, address: String, phone: String, email: String): Boolean {
        when {
            name.isEmpty() -> {
                edtName.error = "Vui lòng nhập họ tên"
                edtName.requestFocus()
                return false
            }
            name.length < 2 -> {
                edtName.error = "Họ tên phải có ít nhất 2 ký tự"
                edtName.requestFocus()
                return false
            }
            address.isEmpty() -> {
                edtAddress.error = "Vui lòng nhập địa chỉ"
                edtAddress.requestFocus()
                return false
            }
            address.length < 10 -> {
                edtAddress.error = "Địa chỉ quá ngắn"
                edtAddress.requestFocus()
                return false
            }
            phone.isEmpty() -> {
                edtPhone.error = "Vui lòng nhập số điện thoại"
                edtPhone.requestFocus()
                return false
            }
            !isValidPhone(phone) -> {
                edtPhone.error = "Số điện thoại không hợp lệ (10-11 chữ số)"
                edtPhone.requestFocus()
                return false
            }
            email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                edtEmail.error = "Email không hợp lệ"
                edtEmail.requestFocus()
                return false
            }
        }
        return true
    }

    private fun isValidPhone(phone: String): Boolean {
        val phoneDigits = phone.replace(Regex("[^0-9]"), "")
        return phoneDigits.length in 10..11 && phoneDigits.startsWith("0")
    }

    private fun showConfirmationDialog(name: String, address: String, phone: String, email: String) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận đơn hàng")
            .setMessage(
                """
                Thông tin đơn hàng:
                
                Họ tên: $name
                Địa chỉ: $address
                Số điện thoại: $phone
                ${if (email.isNotEmpty()) "Email: $email\n" else ""}
                Tổng tiền: ${txtTotal.text}
                
                Bạn có chắc muốn đặt hàng?
                """.trimIndent()
            )
            .setPositiveButton("Xác nhận") { _, _ ->
                completeCheckout(name, address, phone, email)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun completeCheckout(name: String, address: String, phone: String, email: String) {
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Đang xử lý đơn hàng...")
            setCancelable(false)
            show()
        }

        Thread {
            try {
                val dbHelper = DatabaseHelper(this)
                val orderDao = OrderDao(dbHelper.writableDatabase)
                val bookDao = BookDao(dbHelper.writableDatabase)

                val cartItems = CartManager.getCartItems()
                if (cartItems.isEmpty()) {
                    runOnUiThread {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                // Get current user ID
                val userId = sharedPrefsHelper.getUserId()

                if (userId <= 0) {
                    runOnUiThread {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                // Calculate amounts
                val totalAmount = cartItems.sumOf { it.book.price * it.quantity }
                val shippingFee = 0.0 // Miễn phí ship
                val discountAmount = 0.0 // Không giảm giá
                val finalAmount = totalAmount + shippingFee - discountAmount

                // Generate order code
                val orderCode = orderDao.generateOrderCode()

                // Create order items
                val orderItems = cartItems.map { cartItem ->
                    OrderItemModel(
                        id = 0,
                        orderId = 0,
                        bookId = cartItem.book.id,
                        bookTitle = cartItem.book.title,
                        bookAuthor = cartItem.book.author,
                        bookImage = cartItem.book.image,
                        price = cartItem.book.price,
                        quantity = cartItem.quantity,
                        totalPrice = cartItem.book.price * cartItem.quantity
                    )
                }

                // Create order
                val order = OrderModel(
                    id = 0,
                    orderCode = orderCode,
                    customerId = userId,
                    customerName = name,
                    customerEmail = email,
                    customerPhone = phone,
                    customerAddress = address,
                    totalAmount = totalAmount,
                    shippingFee = shippingFee,
                    discountAmount = discountAmount,
                    finalAmount = finalAmount,
                    status = OrderModel.OrderStatus.PENDING,
                    paymentMethod = OrderModel.PaymentMethod.COD,
                    paymentStatus = OrderModel.PaymentStatus.UNPAID,
                    orderDate = getCurrentDateTime(),
                    deliveryDate = "",
                    notes = "",
                    staffId = 0,
                    staffName = "",
                    createdAt = getCurrentDateTime(),
                    updatedAt = getCurrentDateTime(),
                    items = orderItems
                )

                // Insert order with transaction
                val orderId = orderDao.insertOrder(order)

                if (orderId > 0) {
                    // Update book stock and sold count
                    var stockUpdateSuccess = true

                    for (cartItem in cartItems) {
                        try {
                            val currentBook = bookDao.getBookById(cartItem.book.id)
                            if (currentBook != null) {
                                val newStock = currentBook.stock - cartItem.quantity
                                bookDao.updateBookStock(cartItem.book.id, newStock)
                                bookDao.incrementSoldCount(cartItem.book.id, cartItem.quantity)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error updating book stock", e)
                            stockUpdateSuccess = false
                        }
                    }

                    runOnUiThread {
                        progressDialog.dismiss()

                        // Clear cart
                        CartManager.clearCart()

                        // Show success dialog
                        showSuccessDialog(orderCode)
                    }
                } else {
                    runOnUiThread {
                        progressDialog.dismiss()
                        Toast.makeText(
                            this,
                            "Không thể tạo đơn hàng. Vui lòng thử lại.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error completing checkout", e)
                runOnUiThread {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Lỗi: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun showSuccessDialog(orderCode: String) {
        AlertDialog.Builder(this)
            .setTitle("Đặt hàng thành công! 🎉")
            .setMessage(
                "Mã đơn hàng: $orderCode\n\n" +
                        "Đơn hàng của bạn đã được tiếp nhận và sẽ được giao đến bạn trong 3-5 ngày.\n\n" +
                        "Bạn có thể theo dõi đơn hàng trong mục 'Đơn hàng'."
            )
            .setPositiveButton("Xem đơn hàng") { dialog, _ ->
                dialog.dismiss()
                navigateToOrderList()
                finish()
            }
            .setNegativeButton("Tiếp tục mua sắm") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToOrderList() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("open_orders", true)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}