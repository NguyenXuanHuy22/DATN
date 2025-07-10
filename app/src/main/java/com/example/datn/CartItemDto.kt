package com.example.datn

data class CartItemDto(
    val productId: String,
    val image: String,
    val name: String,
    val price: Int,
    val size: String,
    val color: String,
    val quantity: Int
)

fun CartItemDto.toCartItem(): CartItem {
    return CartItem(
        productId = productId,
        image = image,
        name = name,
        price = price,
        size = size,
        color = color,
        quantity = quantity
    )
}

