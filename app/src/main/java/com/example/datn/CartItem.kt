package com.example.datn

data class CartItem(
    val itemId: String, // _id từ MongoDB
    val productId: String,
    val name: String,
    val image: String,
    val price: Int,
    val quantity: Int,
    val size: String,
    val color: String,
    val maxQuantity: Int,
    val userId: String
)

fun CartItem.toDtoForCreate(): CartItemDto {
    return CartItemDto(
        _id = null,
        productId = productId,
        name = name,
        image = image,
        price = price,
        quantity = quantity,
        size = size,
        color = color,
        userId = userId
    )
}

fun CartItem.toDtoForUpdate(): CartItemDto {
    return CartItemDto(
        _id = itemId,
        productId = productId,
        name = name,
        image = image,
        price = price,
        quantity = quantity,
        size = size,
        color = color,
        userId = userId
    )
}
