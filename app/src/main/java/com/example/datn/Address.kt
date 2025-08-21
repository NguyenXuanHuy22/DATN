package com.example.datn

data class Address(
    val _id: String,
    val userId: String,
    val name: String,
    val address: String,
    val phone: String,
    val isDefault: Boolean = true
)

data class MessageResponse(
    val message: String,
    val user: User
)
