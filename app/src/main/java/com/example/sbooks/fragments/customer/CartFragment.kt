package com.example.sbooks.fragments.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.sbooks.R
import com.example.sbooks.utils.CartManager
import com.example.sbooks.utils.ImageUtils
import com.example.sbooks.activities.customer.PaymentActivity
class CartFragment : Fragment(), CartManager.CartUpdateListener {

    private lateinit var txtItemCount: TextView
    private lateinit var btnClearAll: ImageButton
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var scrollCart: View
    private lateinit var cartItemsContainer: LinearLayout
    private lateinit var footerCart: View
    private lateinit var txtTotalPrice: TextView
    private lateinit var btnCheckout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_cart, container, false)

        // Initialize views
        initViews(root)

        // Setup listeners
        setupListeners()

        // Register cart update listener
        CartManager.addListener(this)

        // Load cart items
        updateCartUI()

        return root
    }

    private fun initViews(root: View) {
        txtItemCount = root.findViewById(R.id.txtItemCount)
        btnClearAll = root.findViewById(R.id.btnClearAll)
        emptyStateLayout = root.findViewById(R.id.emptyStateLayout)
        scrollCart = root.findViewById(R.id.scrollCart)
        footerCart = root.findViewById(R.id.footerCart)
        txtTotalPrice = root.findViewById(R.id.txtTotalPrice)
        btnCheckout = root.findViewById(R.id.btnCheckout)

        // Get container from ScrollView
        val scrollView = scrollCart as android.widget.ScrollView
        val mainLayout = scrollView.getChildAt(0) as LinearLayout
        cartItemsContainer = mainLayout
    }

    private fun setupListeners() {
        btnClearAll.setOnClickListener {
            if (CartManager.getCartItems().isNotEmpty()) {
                showClearCartConfirmation()
            }
        }

        btnCheckout.setOnClickListener {
            if (CartManager.getCartItems().isEmpty()) {
                Toast.makeText(requireContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show()
            } else {
                // Navigate to checkout activity
                val intent = android.content.Intent(requireContext(), PaymentActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onCartUpdated() {
        updateCartUI()
    }

    private fun updateCartUI() {
        val cartItems = CartManager.getCartItems()
        val itemCount = CartManager.getItemCount()
        val totalPrice = CartManager.getTotalPrice()

        // Update item count
        txtItemCount.text = "$itemCount sản phẩm"

        // Update total price
        txtTotalPrice.text = String.format("%,.0fđ", totalPrice)

        // Show/hide empty state
        if (cartItems.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            scrollCart.visibility = View.GONE
            footerCart.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            scrollCart.visibility = View.VISIBLE
            footerCart.visibility = View.VISIBLE

            // Display cart items
            displayCartItems(cartItems)
        }
    }

    private fun displayCartItems(items: List<com.example.sbooks.utils.CartItem>) {
        // Clear existing items
        cartItemsContainer.removeAllViews()

        // Add each cart item
        items.forEach { cartItem ->
            val itemView = createCartItemView(cartItem)
            cartItemsContainer.addView(itemView)
        }
    }

    private fun createCartItemView(cartItem: com.example.sbooks.utils.CartItem): CardView {
        val book = cartItem.book
        val cardView = layoutInflater.inflate(
            R.layout.item_cart,
            cartItemsContainer,
            false
        ) as CardView

        // Find views
        val ivBookCover = cardView.findViewById<ImageView>(R.id.iv_book_cover)
        val tvBookTitle = cardView.findViewById<TextView>(R.id.tv_book_title)
        val tvBookAuthor = cardView.findViewById<TextView>(R.id.tv_book_author)
        val tvBookPrice = cardView.findViewById<TextView>(R.id.tv_book_price)
        val btnDecrease = cardView.findViewById<ImageButton>(R.id.btn_decrease)
        val txtQuantity = cardView.findViewById<TextView>(R.id.txt_quantity)
        val btnIncrease = cardView.findViewById<ImageButton>(R.id.btn_increase)
        val btnDelete = cardView.findViewById<ImageButton>(R.id.btn_delete)

        // Bind data
        tvBookTitle.text = book.title
        tvBookAuthor.text = "Tác giả: ${book.author}"
        tvBookPrice.text = book.getFormattedPrice()
        txtQuantity.text = cartItem.quantity.toString()

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

        // Setup click listeners
        btnDecrease.setOnClickListener {
            CartManager.decreaseQuantity(book.id)
        }

        btnIncrease.setOnClickListener {
            if (cartItem.quantity < book.stock) {
                CartManager.increaseQuantity(book.id)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Không thể thêm quá số lượng tồn kho (${book.stock})",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnDelete.setOnClickListener {
            CartManager.removeFromCart(book.id)
            Toast.makeText(
                requireContext(),
                "Đã xóa '${book.title}' khỏi giỏ hàng",
                Toast.LENGTH_SHORT
            ).show()
        }

        return cardView
    }

    private fun showClearCartConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xóa giỏ hàng")
            .setMessage("Bạn có chắc muốn xóa tất cả sản phẩm trong giỏ hàng?")
            .setPositiveButton("Xóa") { _, _ ->
                CartManager.clearCart()
                Toast.makeText(requireContext(), "Đã xóa giỏ hàng", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        CartManager.removeListener(this)
    }
}