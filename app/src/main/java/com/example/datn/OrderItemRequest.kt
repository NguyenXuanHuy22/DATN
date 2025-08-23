package com.example.datn

data class OrderItemRequest(
    val orderDetailId: String,
    val productId: String,
    val name: String,
    val image: String,
    val price: Int,
    val quantity: Int,
    val size: String,
    val color: String,
    val subtotal: Int
)
