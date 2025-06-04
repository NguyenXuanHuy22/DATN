package com.example.datn

data class Product(
    val id: String,
    val name: String,
    val price: Int,
    val sizes: List<String>,
    val colors: List<String>,
    val image: String,
    val description: String
)