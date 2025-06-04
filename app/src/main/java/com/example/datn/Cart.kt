package com.example.datn

data class Cart(
    val id: String,
    val userId: String,
    val items: List<CartItem>
)

data class CartItem(
    val productId: String,
    val quantity: Int,
    val size: String,
    val color: String
)
