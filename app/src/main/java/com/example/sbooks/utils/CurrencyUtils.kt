package com.example.sbooks.utils
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object CurrencyUtils {

    private val vndFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }

    private val numberFormat = DecimalFormat("#,###")

    fun formatCurrency(amount: Double): String {
        return String.format("%,.0f VNĐ", amount)
    }

    fun formatCurrencyShort(amount: Double): String {
        return when {
            amount >= 1_000_000_000 -> String.format("%.1fB VNĐ", amount / 1_000_000_000)
            amount >= 1_000_000 -> String.format("%.1fM VNĐ", amount / 1_000_000)
            amount >= 1_000 -> String.format("%.0fK VNĐ", amount / 1_000)
            else -> String.format("%.0f VNĐ", amount)
        }
    }

    fun formatNumber(number: Int): String {
        return numberFormat.format(number)
    }

    fun formatNumber(number: Long): String {
        return numberFormat.format(number)
    }

    fun formatNumber(number: Double): String {
        return numberFormat.format(number)
    }

    fun parseCurrency(currencyString: String): Double {
        return try {
            currencyString.replace("[^\\d.]".toRegex(), "").toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    fun calculateDiscount(originalPrice: Double, discountPercent: Double): Double {
        return originalPrice * (discountPercent / 100)
    }

    fun calculateDiscountedPrice(originalPrice: Double, discountPercent: Double): Double {
        return originalPrice - calculateDiscount(originalPrice, discountPercent)
    }

    fun calculateTax(amount: Double, taxPercent: Double): Double {
        return amount * (taxPercent / 100)
    }

    fun calculateTotal(subtotal: Double, shippingFee: Double, discount: Double, tax: Double = 0.0): Double {
        return subtotal + shippingFee - discount + tax
    }
}