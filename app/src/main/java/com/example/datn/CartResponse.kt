package com.example.datn

import com.google.gson.annotations.SerializedName

data class CartResponse(
    @SerializedName("_id")
    val _id: String,
    val userId: String,
    val items: List<CartItemDto>
)
