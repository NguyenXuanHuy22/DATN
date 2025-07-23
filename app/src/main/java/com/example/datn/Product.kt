package com.example.datn

data class Product(
    val id: String = "",
    val category: String,
    val name: String,
    val price: Int,
    val image: String,
    val description: String,
    val sizes: List<String>,
    val colors: List<String>
)