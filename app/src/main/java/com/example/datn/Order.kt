package com.example.datn

data class Order(
    val id: String,
    val userId: String,
    val date: String,
    val items: List<OrderItem>,
    val total: Int,
    val status: String
)

data class OrderItem(
    val productId: String,
    val quantity: Int,
    val size: String,
    val color: String
)