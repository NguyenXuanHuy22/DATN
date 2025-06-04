package com.example.datn

data class Wishlist(
    val id: String,
    val userId: String,
    val productIds: List<String>
)
