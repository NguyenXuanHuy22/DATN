package com.example.datn

data class Product(
    val _id: String,
    val category: String,
    val name: String,
    val price: Int,
    val image: String,
    val description: String,
    val variants: List<ProductVariant>
)

data class ProductVariant(
    val size: String,
    val color: String,
    val quantity: Int
)
