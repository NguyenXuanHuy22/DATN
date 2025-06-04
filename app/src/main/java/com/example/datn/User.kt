package com.example.datn

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val address: String,
    val cartId: String,
    val wishlistId: String,
    val orderHistoryId: String
)
