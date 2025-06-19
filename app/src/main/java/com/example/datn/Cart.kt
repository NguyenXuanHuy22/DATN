package com.example.datn

data class Cart(
    val id: String,
    val userId: String,
    val items: List<CartItem> = emptyList()
)

data class CartItem(
    val productId: String,
    val image: String,
    val name: String,
    val price: Int,
    val size: String,
    val color: String,
    val quantity: Int
)