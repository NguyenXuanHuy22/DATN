package com.example.datn

data class Product(
    val _id: String,
    val category: String,
    val name: String,
    val originalPrice: Int,
    val salePrice: Int,
    val image: String,
    val description: String,
    val status: String,
    val extraImages: List<String> = emptyList(),
    val variants: List<ProductVariant>
)

data class ProductVariant(
    val size: String,
    val color: String,
    val quantity: Int
)

