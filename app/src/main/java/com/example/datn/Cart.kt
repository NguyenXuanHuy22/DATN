package com.example.datn

data class Cart(
    val _id: String? = null,
    val userId: String,
    val items: List<CartItem> = emptyList()
)


