package com.example.datn

data class Wishlist(
    val id: String,
    val userId: String,
    val items: List<WishlistItem>
)

data class WishlistItem(
    val productId: String,
    val image: String,
    val price: Int,
    val name: String
)