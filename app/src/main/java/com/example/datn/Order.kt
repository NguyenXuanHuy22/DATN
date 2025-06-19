package com.example.datn


data class Order(
    val id: String,
    val userId: String,
    val items: List<OrderItem> = emptyList(), // ✅ thêm mặc định
    val total: Int,
    val status: String,


    )

data class OrderItem(
    val productId: String,
    val image: String,
    val price: Int,
    val quantity: Int,
    val size: String,
    val color: String,
    val date: String,
)
