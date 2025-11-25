package com.example.sbooks.models
data class OrderModel(
    val id: Int = 0,
    val orderCode: String = "",
    val customerId: Int = 0,
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val totalAmount: Double = 0.0,
    val shippingFee: Double = 0.0,
    val discountAmount: Double = 0.0,
    val finalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentMethod: PaymentMethod = PaymentMethod.COD,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val orderDate: String = "",
    val deliveryDate: String = "",
    val notes: String = "",
    val staffId: Int? = null,
    val staffName: String = "",
    val items: List<OrderItemModel> = emptyList(),
    val createdAt: String = "",
    val customerAvatar: String = "",
    val updatedAt: String = ""

) {
    enum class OrderStatus(val value: String, val displayName: String) {
        PENDING("pending", "Chờ xử lý"),
        PROCESSING("processing", "Đang xử lý"),
        SHIPPING("shipping", "Đang giao"),
        DELIVERED("delivered", "Đã giao"),
        CANCELLED("cancelled", "Đã hủy");

        companion object {
            fun fromValue(value: String): OrderStatus {
                return values().find { it.value == value } ?: PENDING
            }
        }
    }

    enum class PaymentMethod(val value: String, val displayName: String) {
        COD("cod", "Thanh toán khi nhận hàng"),
        BANK_TRANSFER("bank_transfer", "Chuyển khoản"),
        CREDIT_CARD("credit_card", "Thẻ tín dụng"),
        E_WALLET("e_wallet", "Ví điện tử");

        companion object {
            fun fromValue(value: String): PaymentMethod {
                return values().find { it.value == value } ?: COD
            }
        }
    }

    enum class PaymentStatus(val value: String, val displayName: String) {
        UNPAID("unpaid", "Chưa thanh toán"),
        PAID("paid", "Đã thanh toán"),
        REFUNDED("refunded", "Đã hoàn tiền");

        companion object {
            fun fromValue(value: String): PaymentStatus {
                return values().find { it.value == value } ?: UNPAID
            }
        }
    }

    fun getDisplayStatus(): String = status.displayName
    fun getDisplayPaymentMethod(): String = paymentMethod.displayName
    fun getDisplayPaymentStatus(): String = paymentStatus.displayName
    fun getFormattedTotal(): String = String.format("%,.0f VNĐ", finalAmount)
    fun getItemCount(): Int = items.sumOf { it.quantity }
    fun getItemSummary(): String = items.joinToString(", ") { "${it.bookTitle} (${it.quantity})" }
    fun canBeCancelled(): Boolean = status in listOf(OrderStatus.PENDING, OrderStatus.PROCESSING)
    fun isPending(): Boolean = status == OrderStatus.PENDING
    fun isCompleted(): Boolean = status == OrderStatus.DELIVERED
}