package com.example.datn

import com.google.gson.annotations.SerializedName

data class Wishlist(
    @SerializedName("_id")
    val _id: String? = null,
    val userId: String,
    val items: List<WishlistItem> = emptyList()
)

data class WishlistItem(
    val productId: String,
    val image: String,
    val name: String,
    val price: Int
)

// Dùng để gửi request toggle wishlist
data class ToggleWishlistRequest(
    val userId: String,
    val product: WishlistItem
)
