package com.example.datn

data class Wishlist(
    val id: String,
    val userId: String,
    val items: List<WishlistItem> = emptyList()
)

data class WishlistItem(
    val productId: String,
    val image: String,
    val name: String,
    val price: Int
)