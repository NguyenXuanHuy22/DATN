package com.example.datn

data class CartCreateRequest(
    val userId: String,
    val items: List<CartItemDto>
)
