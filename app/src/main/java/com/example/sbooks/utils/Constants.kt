package com.example.sbooks.utils
import com.example.sbooks.R
object Constants {
    // Database
    const val DATABASE_NAME = "starbooks.db"
    const val DATABASE_VERSION = 1

    // SharedPreferences
    const val PREFS_NAME = "StarBooksPrefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_USER_ROLE = "user_role"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    const val KEY_REMEMBER_LOGIN = "remember_login"

    // User Session Keys
    const val PREF_USER_ID = "user_id"
    const val PREF_USERNAME = "username"
    const val PREF_EMAIL = "email"
    const val PREF_FULL_NAME = "full_name"
    const val PREF_ROLE = "role"
    const val PREF_IS_LOGGED_IN = "is_logged_in"

    // Request codes
    const val REQUEST_CODE_PICK_IMAGE = 1001
    const val REQUEST_CODE_CAMERA = 1002
    const val REQUEST_CODE_PERMISSIONS = 1003

    // Stock thresholds
    const val LOW_STOCK_THRESHOLD = 10
    const val OUT_OF_STOCK_THRESHOLD = 0

    // Pagination
    const val PAGE_SIZE = 20
    const val INITIAL_LOAD_SIZE = 40

    // Animation durations
    const val ANIMATION_DURATION_SHORT = 200L
    const val ANIMATION_DURATION_MEDIUM = 300L
    const val ANIMATION_DURATION_LONG = 500L

    // Date formats
    const val DATE_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT_DISPLAY = "dd/MM/yyyy HH:mm"
    const val DATE_FORMAT_SIMPLE = "dd/MM/yyyy"

    // Order status colors
    val ORDER_STATUS_COLORS = mapOf(
        "pending" to R.color.statusPending,
        "processing" to R.color.statusProcessing,
        "shipping" to R.color.statusProcessing,
        "delivered" to R.color.statusDelivered,
        "cancelled" to R.color.statusCancelled
    )

    // User role colors
    val USER_ROLE_COLORS = mapOf(
        "admin" to R.color.colorAccent,
        "staff" to R.color.colorInfo,
        "customer" to R.color.colorPrimary
    )

    // Default values
    const val DEFAULT_SHIPPING_FEE = 30000.0
    const val MIN_ORDER_VALUE_FREE_SHIPPING = 200000.0

    // Validation
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_USERNAME_LENGTH = 50
    const val MAX_EMAIL_LENGTH = 100
    const val MAX_PHONE_LENGTH = 15

    // File paths
    const val IMAGE_DIRECTORY = "StarBooks/Images"
    const val BACKUP_DIRECTORY = "StarBooks/Backups"

    // Network timeouts (in seconds)
    const val NETWORK_TIMEOUT = 30
    const val CONNECTION_TIMEOUT = 10

    // Error messages
    const val ERROR_NETWORK = "Lỗi kết nối mạng"
    const val ERROR_SERVER = "Lỗi máy chủ"
    const val ERROR_UNKNOWN = "Lỗi không xác định"
    const val ERROR_VALIDATION = "Dữ liệu không hợp lệ"
    const val ERROR_PERMISSION_DENIED = "Không có quyền truy cập"
    const val ERROR_FILE_NOT_FOUND = "Không tìm thấy tệp"

    // Success messages
    const val SUCCESS_SAVE = "Lưu thành công"
    const val SUCCESS_DELETE = "Xóa thành công"
    const val SUCCESS_UPDATE = "Cập nhật thành công"
    const val SUCCESS_LOGIN = "Đăng nhập thành công"
    const val SUCCESS_LOGOUT = "Đăng xuất thành công"

    // App Constants
    const val APP_NAME = "SBooks"
    const val SHARED_PREFS_NAME = "SBooksPrefs"

    // Messages


    const val SUCCESS_REGISTER = "Đăng ký thành công"
    const val ERROR_LOGIN_FAILED = "Đăng nhập thất bại"
    const val ERROR_INVALID_CREDENTIALS = "Tên đăng nhập hoặc mật khẩu không đúng"
    const val ERROR_USER_EXISTS = "Người dùng đã tồn tại"


    // Validation

    const val MIN_USERNAME_LENGTH = 3

    // Image handling
    const val MAX_IMAGE_SIZE = 2 * 1024 * 1024 // 2MB
    const val IMAGE_QUALITY = 80
    const val MAX_IMAGE_WIDTH = 800
    const val MAX_IMAGE_HEIGHT = 600

    // Database

    // Request codes

    const val REQUEST_CODE_GALLERY = 101

    // User roles
    const val ROLE_ADMIN = "admin"
    const val ROLE_STAFF = "staff"
    const val ROLE_CUSTOMER = "customer"

    // User status
    const val STATUS_ACTIVE = "active"
    const val STATUS_INACTIVE = "inactive"
    const val STATUS_SUSPENDED = "suspended"

    // Order status
    const val ORDER_STATUS_PENDING = "pending"
    const val ORDER_STATUS_CONFIRMED = "confirmed"
    const val ORDER_STATUS_PROCESSING = "processing"
    const val ORDER_STATUS_SHIPPING = "shipping"
    const val ORDER_STATUS_DELIVERED = "delivered"
    const val ORDER_STATUS_CANCELLED = "cancelled"

    // Payment methods
    const val PAYMENT_COD = "cod"
    const val PAYMENT_BANK_TRANSFER = "bank_transfer"
    const val PAYMENT_MOMO = "momo"
    const val PAYMENT_ZALOPAY = "zalopay"

    // Currency
    const val CURRENCY_SYMBOL = "₫"

    // Date formats
    const val DATE_FORMAT_DATABASE = "yyyy-MM-dd HH:mm:ss"
    const val TIME_FORMAT_DISPLAY = "HH:mm"
}