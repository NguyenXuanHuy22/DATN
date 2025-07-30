package com.example.datn

data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val phone: String,
    val address: String,
    val avatar: String = "", // mặc định là rỗng
    val role: String
)


