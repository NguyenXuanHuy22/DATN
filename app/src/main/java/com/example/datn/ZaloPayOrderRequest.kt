package com.example.datn

data class ZaloPayOrderRequest(
    val amount: Int,
    val description: String,
    val userId: String,
    val paymentMethod: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val items: List<OrderItemRequest>
)



