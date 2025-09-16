package com.example.datn

import com.google.gson.annotations.SerializedName

data class Comment(
    val userId: String,
    val productId: String,
    val orderId: String,
    val avatar: String,
    val username: String,
    val ratingStar: Int,
    val commentDes: String
)

