package com.example.datn

data class Comment(
    val userId: String,
    val productId: String,
    val orderId: String,
    val avatar: String,
    val username: String,
    val ratingStar: Int,
    val commentDes: String
)
