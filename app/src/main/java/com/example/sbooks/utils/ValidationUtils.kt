package com.example.sbooks.utils
import android.util.Patterns
import java.util.regex.Pattern

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidUsername(username: String): Boolean {
        return username.isNotEmpty() &&
                username.length >= 3 &&
                username.length <= Constants.MAX_USERNAME_LENGTH &&
                Pattern.matches("^[a-zA-Z0-9_]+$", username)
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= Constants.MIN_PASSWORD_LENGTH
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.isNotEmpty() &&
                phone.length >= 10 &&
                phone.length <= Constants.MAX_PHONE_LENGTH &&
                Pattern.matches("^[0-9+\\-\\s()]+$", phone)
    }

    fun isValidPrice(price: String): Boolean {
        return try {
            val priceValue = price.toDouble()
            priceValue > 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun isValidStock(stock: String): Boolean {
        return try {
            val stockValue = stock.toInt()
            stockValue >= 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun isValidYear(year: String): Boolean {
        return try {
            val yearValue = year.toInt()
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            yearValue in 1900..currentYear
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun isValidPages(pages: String): Boolean {
        return try {
            val pagesValue = pages.toInt()
            pagesValue > 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun validateUserInput(
        username: String,
        email: String,
        password: String,
        fullName: String,
        phone: String = ""
    ): ValidationResult {
        val errors = mutableListOf<String>()

        if (fullName.isEmpty()) {
            errors.add("Họ tên không được để trống")
        }

        if (!isValidUsername(username)) {
            errors.add("Tên đăng nhập không hợp lệ (3-50 ký tự, chỉ chứa chữ, số và dấu gạch dưới)")
        }

        if (!isValidEmail(email)) {
            errors.add("Email không hợp lệ")
        }

        if (!isValidPassword(password)) {
            errors.add("Mật khẩu phải có ít nhất ${Constants.MIN_PASSWORD_LENGTH} ký tự")
        }

        if (phone.isNotEmpty() && !isValidPhone(phone)) {
            errors.add("Số điện thoại không hợp lệ")
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateBookInput(
        title: String,
        author: String,
        price: String,
        stock: String,
        categoryId: Int,
        publicationYear: String = "",
        pages: String = ""
    ): ValidationResult {
        val errors = mutableListOf<String>()

        if (title.trim().isEmpty()) {
            errors.add("Tên sách không được để trống")
        }

        if (author.trim().isEmpty()) {
            errors.add("Tác giả không được để trống")
        }

        if (!isValidPrice(price)) {
            errors.add("Giá sách không hợp lệ")
        }

        if (!isValidStock(stock)) {
            errors.add("Số lượng tồn kho không hợp lệ")
        }

        if (categoryId <= 0) {
            errors.add("Vui lòng chọn danh mục")
        }

        if (publicationYear.isNotEmpty() && !isValidYear(publicationYear)) {
            errors.add("Năm xuất bản không hợp lệ")
        }

        if (pages.isNotEmpty() && !isValidPages(pages)) {
            errors.add("Số trang không hợp lệ")
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    fun validateCategoryInput(name: String, description: String): ValidationResult {
        val errors = mutableListOf<String>()

        if (name.trim().isEmpty()) {
            errors.add("Tên danh mục không được để trống")
        }

        if (name.length > 100) {
            errors.add("Tên danh mục không được quá 100 ký tự")
        }

        if (description.length > 500) {
            errors.add("Mô tả không được quá 500 ký tự")
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )
}
