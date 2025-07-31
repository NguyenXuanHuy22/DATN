package com.example.datn

data class CartItemDto(
    val itemId: String,
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
        itemId = itemId,
        productId = productId,
        image = image,
        name = name,
        price = price,
        size = size,
        color = color,
        quantity = quantity,
        maxQuantity = 0
    )
}

