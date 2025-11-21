package com.example.sbooks.utils

import com.example.sbooks.models.BookModel

data class CartItem(
    val book: BookModel,
    var quantity: Int = 1
) {
    fun getTotalPrice(): Double = book.price * quantity
}

object CartManager {
    private val cartItems = mutableListOf<CartItem>()
    private val listeners = mutableListOf<CartUpdateListener>()

    interface CartUpdateListener {
        fun onCartUpdated()
    }

    fun addListener(listener: CartUpdateListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: CartUpdateListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it.onCartUpdated() }
    }

    fun addToCart(book: BookModel, quantity: Int = 1) {
        val existingItem = cartItems.find { it.book.id == book.id }
        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            cartItems.add(CartItem(book, quantity))
        }
        notifyListeners()
    }

    fun removeFromCart(bookId: Int) {
        cartItems.removeAll { it.book.id == bookId }
        notifyListeners()
    }

    fun updateQuantity(bookId: Int, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(bookId)
            return
        }

        val item = cartItems.find { it.book.id == bookId }
        item?.let {
            it.quantity = newQuantity
            notifyListeners()
        }
    }

    fun increaseQuantity(bookId: Int) {
        val item = cartItems.find { it.book.id == bookId }
        item?.let {
            it.quantity++
            notifyListeners()
        }
    }

    fun decreaseQuantity(bookId: Int) {
        val item = cartItems.find { it.book.id == bookId }
        item?.let {
            if (it.quantity > 1) {
                it.quantity--
            } else {
                removeFromCart(bookId)
            }
            notifyListeners()
        }
    }

    fun clearCart() {
        cartItems.clear()
        notifyListeners()
    }

    fun getCartItems(): List<CartItem> = cartItems.toList()

    fun getItemCount(): Int = cartItems.sumOf { it.quantity }

    fun getTotalPrice(): Double = cartItems.sumOf { it.getTotalPrice() }

    fun isInCart(bookId: Int): Boolean = cartItems.any { it.book.id == bookId }

    fun getQuantity(bookId: Int): Int = cartItems.find { it.book.id == bookId }?.quantity ?: 0
}