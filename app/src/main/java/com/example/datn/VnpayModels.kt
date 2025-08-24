package com.example.datn

data class PaymentUrlResponse(
    val orderId: String,
    val paymentUrl: String
)

data class VnpCreatePaymentRequest(
    val userId: String,
    val items: List<OrderItem>,           // Dùng lại model OrderItem bạn đã có
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val addressId: String? = null,        // nếu chọn địa chỉ đã lưu
    val date: String? = null              // optional
)
