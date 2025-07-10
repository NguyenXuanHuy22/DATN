package com.example.datn

data class CartResponse(
    val id: String,
    val userId: String,
    val items: List<CartItemDto>
)
